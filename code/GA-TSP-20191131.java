import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.Random;

public class GATsp{
//这是一个对称TSP问题，所以。。。
    private int scale;// 种群规模
    private int cityNum; // 城市数量，染色体长度
    private int MAX_GEN; // 运行代数
    private int[][] distance; // 距离矩阵
    private int bestT;// 最佳出现代数
    private int bestLength; // 最佳长度
    private int[] bestTour; // 最佳路径

    private int[][] oldPopulation;// 初始种群，父代种群
    private int[][] newPopulation;// 新的种群，子代种群
    private int[] fitness;// 种群适应度

    private float[] Pi;// 种群中各个个体的累计概率
    private float Pc;// 交叉概率
    private float Pm;// 变异概率
    private int t;// 当前代数

    private Random random;

    public GATsp(int s, int n, int g, float c, float m) {
        scale = s;//规模
        cityNum = n;//城市数目
        MAX_GEN = g;//最大迭代代数
        Pc = c;//发生交叉的概率
        Pm = m;//发生变异的概率
    }

    private void init(String filename) throws IOException {
        // 从文件中读取数据
        int[] x;
        int[] y;
        String strbuff;
        BufferedReader data = new BufferedReader(new InputStreamReader(new FileInputStream(filename)));
        distance = new int[cityNum][cityNum];
        x = new int[cityNum];
        y = new int[cityNum];
        for (int i = 0; i < cityNum; i++) {
            // 读取一行数据，数据格式1 6734 1453
            strbuff = data.readLine();
            // 字符分割
            String[] strcol = strbuff.split(" ");
            x[i] = Integer.valueOf(strcol[1]);// x坐标
            y[i] = Integer.valueOf(strcol[2]);// y坐标
        }
        // 计算距离矩阵
        // ，针对具体问题，距离计算方法也不一样，此处用的是att48作为案例，它有48个城市，距离计算方法为伪欧氏距离，最优值为10628
        for (int i = 0; i < cityNum - 1; i++) {
            distance[i][i] = 0; // 对角线为0
            for (int j = i + 1; j < cityNum; j++) {
                double rij = Math.sqrt(((x[i] - x[j]) * (x[i] - x[j]) + (y[i] - y[j])
                                * (y[i] - y[j])) / 10.0);
                // 四舍五入，取整
                int tij = (int) Math.round(rij);
                if (tij < rij) {
                    distance[i][j] = tij + 1;
                    distance[j][i] = distance[i][j];
                } else {
                    distance[i][j] = tij;
                    distance[j][i] = distance[i][j];
                }
            }
        }
        distance[cityNum - 1][cityNum - 1] = 0;//最后一个元素

        bestLength = Integer.MAX_VALUE;
        bestTour = new int[cityNum + 1];//路径数组
        bestT = 0;
        t = 0;

        newPopulation = new int[scale][cityNum];//初始化父子种群
        oldPopulation = new int[scale][cityNum];
        fitness = new int[scale];
        Pi = new float[scale];

        random = new Random();
    }

    // 初始化种群
    void initGroup() {
        int i, j, k;
        for (k = 0; k < scale; k++)// 种群数
        {
            oldPopulation[k][0] = random.nextInt(65535) % cityNum;//这里就是想初始化一条路径而已，
            // 我也不知道那个看起来很玄学的65535是干嘛的
            for (i = 1; i < cityNum;)// 染色体长度
            {
                oldPopulation[k][i] = random.nextInt(65535) % cityNum;
                for (j = 0; j < i; j++) {
                    if (oldPopulation[k][i] == oldPopulation[k][j]) {//如果相同,那就得重新来,路径不能重复
                        break;
                    }
                }
                if (j == i) {
                    i++;
                }
            }
        }

    }
    //计算适应度的函数
    public int evaluate(int[] chromosome) {
        // 0123
        int len = 0;
        // 染色体，起始城市,城市1,城市2...城市n
        for (int i = 1; i < cityNum; i++) {
            len += distance[chromosome[i - 1]][chromosome[i]];
            //0-1 1-2 2-3 3-4 4-5 5-6
        }
        // 城市n,起始城市,返回的过程
        len += distance[chromosome[cityNum - 1]][chromosome[0]];
        return len;
    }

    // 计算种群中各个个体的累积概率,做为轮盘赌算法的选择部分
    void countRate() {
        //这个轮盘赌写的太烂了，令人摸不着头脑，后期改一下
        int k;
        double sumFitness = 0;// 适应度总和
        double[] tempf = new double[scale];

        for (k = 0; k < scale; k++) {
            tempf[k] = 10.0 / fitness[k];//这个适应度函数的计算可真是令人摸不着头脑
            sumFitness += tempf[k];
        }

        Pi[0] = (float) (tempf[0] / sumFitness);
        for (k = 1; k < scale; k++) {
            Pi[k] = (float) (tempf[k] / sumFitness + Pi[k - 1]);
        }
    }

    // 挑选某代种群中适应度最高的个体，直接复制到子代中
    // 前提是已经计算出各个个体的适应度Fitness[max]
    //精英保留策略，要比全保留更优秀
    public void selectBestGh() {
        int k, i, maxid;
        int maxevaluation;

        maxid = 0;
        maxevaluation = fitness[0];
        for (k = 1; k < scale; k++) {
            if (maxevaluation > fitness[k]) {
                maxevaluation = fitness[k];
                maxid = k;
            }
        }

        if (bestLength > maxevaluation) {
            bestLength = maxevaluation;
            bestT = t;// 最好的染色体出现的代数;
            for (i = 0; i < cityNum; i++) {
                bestTour[i] = oldPopulation[maxid][i];
            }
        }
        // 复制染色体，k表示新染色体在种群中的位置，kk表示旧的染色体在种群中的位置
        copyGh(0, maxid);// 将当代种群中适应度最高的染色体k复制到新种群中，排在第一位0
    }

    // 复制染色体，k表示新染色体在种群中的位置，kk表示旧的染色体在种群中的位置
    public void copyGh(int k, int kk) {
        int i;
        for (i = 0; i < cityNum; i++) {
            newPopulation[k][i] = oldPopulation[kk][i];
        }
    }

    // 赌轮选择策略挑选
    public void select() {
        int k, i, selectId;
        float ran1;
        // Random random = new Random(System.currentTimeMillis());
        for (k = 1; k < scale; k++) {
            ran1 = (float) (random.nextInt(65535) % 1000 / 1000.0);
            //大哥好像就特别喜欢65535这个数字、、、、
            // 产生方式
            for (i = 0; i < scale; i++) {
                if (ran1 <= Pi[i]) {
                    break;
                }
            }
            selectId = i;
            copyGh(k, selectId);
        }
    }

    //进化函数，保留最好染色体不进行交叉变异
    public void evolution1() {
        int k;
        // 挑选某代种群中适应度最高的个体
        selectBestGh();
        // 赌轮选择策略挑选scale-1个下一代个体
        select();
        // Random random = new Random(System.currentTimeMillis());
        float r;
        for (k = 1; k + 1 < scale / 2; k = k + 2) {
            r = random.nextFloat();// /产生概率
            if (r < Pc) {
                OXCross(k, k + 1);// 进行交叉
                //OXCross(k,k+1);//进行交叉
            } //else {
                r = random.nextFloat();// /产生概率
                // 变异
                if (r < Pm) {
                    OnCVariation(k);
                }
                r = random.nextFloat();// /产生概率
                // 变异
                if (r < Pm) {
                    OnCVariation(k + 1);
                }
            //}
        }
        if (k == scale / 2 - 1)// 剩最后一个染色体没有交叉L-1
        {
            r = random.nextFloat();// /产生概率
            if (r < Pm) {
                OnCVariation(k);
            }
        }

    }

    // 类OX交叉算子
    void OXCross(int k1, int k2) {
        int i, j, k, flag;
        int ran1, ran2, temp;
        int[] Gh1 = new int[cityNum];
        int[] Gh2 = new int[cityNum];
        ran1 = random.nextInt(65535) % cityNum;
        ran2 = random.nextInt(65535) % cityNum;

        while (ran1 == ran2) {
            //如果交叉位置相同的话
            ran2 = random.nextInt(65535) % cityNum;
        }

        if (ran1 > ran2)// 确保ran1<ran2
        {
            temp = ran1;
            ran1 = ran2;
            ran2 = temp;
        }

        flag = ran2 - ran1 + 1;// 删除重复基因前染色体长度
        for (i = 0, j = ran1; i < flag; i++, j++) {
            Gh1[i] = newPopulation[k2][j];//交叉基因组1
            Gh2[i] = newPopulation[k1][j];//交叉基因组2
        }
        // 已近赋值i=ran2-ran1个基因
        for (k = 0, j = flag; j < cityNum;)// 染色体长度
        {
            Gh1[j] = newPopulation[k1][k++];//对个体1的基因组进行读取
            for (i = 0; i < flag; i++) {
                if (Gh1[i] == Gh1[j]) {
                    break;
                }
            }
            if (i == flag) {
                j++;
            }
        }

        for (k = 0, j = flag; j < cityNum;)// 染色体长度
        {
            Gh2[j] = newPopulation[k2][k++];
            for (i = 0; i < flag; i++) {
                if (Gh2[i] == Gh2[j]) {
                    break;
                }
            }
            if (i == flag) {
                j++;
            }
        }

        for (i = 0; i < cityNum; i++) {
            newPopulation[k1][i] = Gh1[i];// 交叉完毕放回种群
            newPopulation[k2][i] = Gh2[i];// 交叉完毕放回种群
        }

    }

    // 交换变异算子,任意的选择两个点，然后进行互换操作,这应该是属于最基础的变异操作了吧
    public void OnCVariation(int k) {
        int ran1, ran2, temp,count;

        count=random.nextInt(65535) % cityNum;

        for (int i=0;i<count;i++){
            ran1 = random.nextInt(65535) % cityNum;
            ran2 = random.nextInt(65535) % cityNum;
            while (ran1 == ran2) {
                ran2 = random.nextInt(65535) % cityNum;
            }
            temp = newPopulation[k][ran1];
            newPopulation[k][ran1] = newPopulation[k][ran2];
            newPopulation[k][ran2] = temp;
        }
    }

    public void solve() {
        int i;
        int k;
        // 初始化种群
        initGroup();
        // 计算初始化种群适应度，Fitness[max]
        for (k = 0; k < scale; k++) {
            fitness[k] = evaluate(oldPopulation[k]);
        }
        // 计算初始化种群中各个个体的累积概率，Pi[max]
        countRate();
        for (t = 0; t < MAX_GEN; t++) {
            evolution1();
            // 将新种群newGroup复制到旧种群oldGroup中，准备下一代进化
            for (k = 0; k < scale; k++) {
                for (i = 0; i < cityNum; i++) {
                    oldPopulation[k][i] = newPopulation[k][i];
                }
            }
            // 计算种群适应度
            for (k = 0; k < scale; k++) {
                fitness[k] = evaluate(oldPopulation[k]);
            }
            // 计算种群中各个个体的累积概率
            countRate();
        }

        System.out.println("最后种群...");
        for (k = 0; k < scale; k++) {
            for (i = 0; i < cityNum; i++) {
                System.out.print(oldPopulation[k][i] + ",");
            }
            System.out.println();
        }
        System.out.println("---" + bestTour.toString() + " 最佳长度" + bestLength);

        System.out.println("最佳长度出现代数：");
        System.out.println(bestT);
        System.out.println("最佳长度");
        System.out.println(bestLength);
        System.out.println("最佳路径：");
        for (i = 0; i < cityNum; i++) {
            System.out.print(bestTour[i] + ",");
        }

    }


    public static void main(String[] args) throws IOException {
        System.out.println("Start....");
        GATsp ga = new GATsp(30, 48, 50000, 0.8f, 0.7f);
        ga.init("D://data.txt");
        ga.solve();
    }

}
