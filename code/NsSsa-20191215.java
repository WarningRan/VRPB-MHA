/**
 * author:FHhui
 * description:多目标SSA
 * 松鼠TSP解法
 * 如果要将单目标改为多目标排序的松鼠怎么搞，
 * 什么作为排序松鼠的选择标准，假定帕累托等级为1的个体在最有树上，帕累托等级为2的个体在次优书上？
 * 这样的话就是最有树也有多棵，好像确实挺多目标的，但是距离值好像就没有用了
 * 或者是可以假定前多少个是最优树，然后相同帕累托等级就比较距离，emmmmmm
 * 突然觉得第二种想法好有道理，我简直就是个天。。。。
 * 生的沙雕。。。
 **/
import java.util.ArrayList;
public class NsSsa {
    private static boolean debug=false;//debug参数
    private static double FS[][];//FS[i][j]表示第i只松鼠，第j维度的位置
    private static double FS_best[];//FS_best[i]表示最优松鼠第i维度的位置 括号内为维度
    private static double FuncVal_best;
    private static double FS_second_best[][];//FS_second_best[i][j]表示第i个次优松鼠第j维度的位置
    private static int N = 100;//松鼠的个数50只
    private static int nq[];//松鼠的被支配的个数
    private static int Sq[][];//松鼠支配的个数
    private static int best = 5;//最优解个数为5
    private static int N_second_best = 9;//次优解个数为9
    private static int dimension = 30;//维度
    private static int Round=2000;  //迭代上限
    private static int t=0;  //当前迭代次数
    private static double[] distance;//拥挤度
    private static int[] rank;//帕累托等级数组****
    private static double FS_upper[];//定义域的上界 括号内为维度
    private static double FS_lower[];//定义域的下界 括号内为维度
    private static double FuncVal[][]; //函数值数组，存储每一个适应度结果 括号内为松鼠编号*****
    private static int sorted_number[];//排序后的松鼠编号*****，为了区分最优解与次优解的标志
    private static boolean sorted_number_flag[];//松鼠编号对应的是否吃过橡果true******
    private static boolean best_number_flag[];//松鼠编号对应的是否吃过山核桃true******
    public void init()
    {
        //初始化操作,构造函数
        //Scanner in = new Scanner(System.in);
        FS = new double[N][dimension];
        FS = new double[N][dimension];
        FS_best = new double[dimension];
        FS_second_best = new double[N_second_best][dimension];
        FS_upper = new double[dimension];
        FS_lower = new double[dimension];
        FuncVal = new double[N][2];
        distance =new double[N];
        rank=new int[N];
        nq=new int[N];
        Sq=new int[N][N];
        for (int i=0;i<N;i++){
            for (int j=0;j<N;j++)
                Sq[i][j]=0;
        }
        sorted_number = new int[N];
        sorted_number_flag = new boolean[N];
        best_number_flag = new boolean[N];
        for(int i=0;i<N;i++)
        {
            sorted_number[i]=i;				//存松鼠序号
            sorted_number_flag[i]=false;	//是否吃过橡果
            best_number_flag[i]=false;		//是否吃过山核桃
        }
        for(int i=0;i<dimension;i++)	//输入各个维度讨论的上下界
        {
            double upper=1.0;
            FS_upper[i] = upper;
            double lower=0.0;
            FS_lower[i] = lower;
        }
        for(int i=0;i<N;i++)	//初始化位置
        {
            for(int j=0;j<dimension;j++)
            {
                FS[i][j]=FS_lower[j] + (double)Math.random() * (FS_upper[j] - FS_lower[j]);
                while(FS[i][j]<=FS_lower[j] || FS[i][j]>=FS_upper[j]){
                    FS[i][j]=FS_lower[j] + (double)Math.random() * (FS_upper[j] - FS_lower[j]);
                }
            }
            //System.out.println(FS[i][0]+"  "+i);//初始化没有问题
        }
    }
    //快速非支配排序
    public void fast_nonsort(){
        //快速支配排序的位置点问题该如何判断
        nq=new int[N];
        Sq=new int[N][N];
        //更新两个数组防止产生影响
        ArrayList<Integer> f=new ArrayList<>();
        ArrayList<ArrayList<Integer>> F=new ArrayList<>();
        //划分帕累托等级
        //关键我这里的东西用啥存呢？？？有点儿奇怪、、、
        for (int i=0;i<N;i++){
            for (int j=0;j<N;j++){
                if (j!=i){
                if (FuncVal[i][0]<=FuncVal[j][0]&&FuncVal[i][1]<=FuncVal[j][1]){
                    if (FuncVal[i][0]==FuncVal[j][0]&&FuncVal[i][1]==FuncVal[j][1]){}
                    else{
                        Sq[i][j]=1;//表示支配关系，i支配j
                    }
                }else if(FuncVal[i][0]>=FuncVal[j][0]&&FuncVal[i][1]>=FuncVal[j][1]){
                    if (FuncVal[i][0]==FuncVal[j][0]&&FuncVal[i][1]==FuncVal[j][1]){}
                    else{
                        nq[i]++;//表示自己被支配了，
                    }
                }
        }
            }
            if(nq[i]==0){
                //第i个体为1，放入初始列表中
                rank[i]=1;//
                f.add(i);//将i添加进去，这里保存的是松鼠在总数组中的i
            }
        }
        F.add(f);
        int j=0;
        while(F.get(j).size()!=0){//如果下一个数组不为空
            f=new ArrayList<Integer>();
            for (int m=0;m<F.get(j).size();m++){
                int[] ind=Sq[F.get(j).get(m)];
                //System.out.println(ind[0]);
                for (int h=0;h<N;h++){
                    if (ind[h]==1)
                    {
                        //System.out.println(h);
                        nq[h]--;
                        if (nq[h]==0){
                            rank[h]=j+2;
                            f.add(h);
                        }
                    }
                }
            }
            F.add(f);
            j++;
        }
    }
    //距离值的计算，根据fun进行排序，因为这里的松鼠本身便需要有一个排序的操作，so。。。
    public void instance(){
        //对第一个进行排序，得到的便是第一个函数的升序序列
        for(int i=0;i<N;i++)
        {
            for(int j=0;j<N-1-i;j++)
            {
                if(FuncVal[j][0]>FuncVal[j+1][0])		//排序时交换位置值及各个属性数组
                {
                    //交换函数数组
                    double[] temp=FuncVal[j];
                    FuncVal[j]=FuncVal[j+1];
                    FuncVal[j+1]=temp;
                    for(int k=0;k<dimension;k++)
                    {//交换维度数组
                        double tem = FS[j][k];
                        FS[j][k]=FS[j+1][k];
                        FS[j+1][k]=tem;
                    }
                    //交换排序数组
                    int TEMP=sorted_number[j];
                    sorted_number[j]=sorted_number[j+1];
                    sorted_number[j+1]=TEMP;
                    //交换标记数组
                    boolean Temp=sorted_number_flag[j];
                    sorted_number_flag[j]=sorted_number_flag[j+1];
                    sorted_number_flag[j+1]=Temp;
                    //交换rank数组
                    int TEMP1=rank[j];
                    rank[j]=rank[j+1];
                    rank[j+1]=TEMP1;
                    //第一个函数可以不用交换距离数组
                }
            }
        }
        distance[0]=distance[N-1]=Integer.MAX_VALUE;
        for (int i=1;i<N-1;i++){
            distance[i]=(FuncVal[i+1][0]-FuncVal[i-1][0])/(FuncVal[N-1][0]-FuncVal[0][0]);
        }

        //对第二个函数进行排序
        for(int i=0;i<N;i++)
        {
            for(int j=0;j<N-1-i;j++)
            {
                if(FuncVal[j][1]>FuncVal[j+1][1])		//排序时交换位置值及各个属性数组
                {
                    //交换函数数组
                    double[] temp=FuncVal[j];
                    FuncVal[j]=FuncVal[j+1];
                    FuncVal[j+1]=temp;
                    for(int k=0;k<dimension;k++)
                    {//交换维度数组
                        double tem = FS[j][k];
                        FS[j][k]=FS[j+1][k];
                        FS[j+1][k]=tem;
                    }
                    //交换排序数组
                    int TEMP=sorted_number[j];
                    sorted_number[j]=sorted_number[j+1];
                    sorted_number[j+1]=TEMP;
                    //交换标记数组
                    boolean Temp=sorted_number_flag[j];
                    sorted_number_flag[j]=sorted_number_flag[j+1];
                    sorted_number_flag[j+1]=Temp;
                    //交换rank数组
                    int TEMP1=rank[j];
                    rank[j]=rank[j+1];
                    rank[j+1]=TEMP1;
                    //第二个函数需要交换距离数组
                    double Temp1=distance[j];
                    distance[j]=distance[j+1];
                    distance[j+1]=Temp1;
                }
            }
        }
        distance[0]=distance[N-1]=Integer.MAX_VALUE;
        for (int i=1;i<N-1;i++){
            distance[i]=(FuncVal[i+1][1]-FuncVal[i-1][1])/(FuncVal[N-1][1]-FuncVal[0][1]);
        }
    }
    //适应值的评价函数
    public void evalue()
    {

        for(int i=0;i<N;i++)
        {
            FuncVal[i][0] = FS[i][0];
            int j;
            double g = 1, sum = 0;
            for (j = 1; j<dimension; j++)
            {
                sum += FS[i][j];
            }
            sum += 9 * (sum / (dimension - 1));
            g += sum;
            FuncVal[i][1] = g*(1 - Math.sqrt(FS[i][0] / g));
        }

    }
    public void evalue_sorted()
    {
        //帕累托等级与拥挤度排序排序
        for(int i=0;i<N;i++)
        {
            for(int j=0;j<N-1-i;j++)
            {
                if(rank[j]>rank[j+1])		//排序时交换位置值及各个属性数组
                {
                    //交换距离数组
                    double Temp1=distance[j];
                    distance[j]=distance[j+1];
                    distance[j+1]=Temp1;
                    //交换rank数组
                    int TEMP1=rank[j];
                    rank[j]=rank[j+1];
                    rank[j+1]=TEMP1;
                    //交换函数数组
                    double[] temp=FuncVal[j];
                    FuncVal[j]=FuncVal[j+1];
                    FuncVal[j+1]=temp;
                    for(int k=0;k<dimension;k++)
                    {
                        double tem = FS[j][k];
                        FS[j][k]=FS[j+1][k];
                        FS[j+1][k]=tem;
                    }
                    //交换真实标号数组
                    int TEMP=sorted_number[j];
                    sorted_number[j]=sorted_number[j+1];
                    sorted_number[j+1]=TEMP;
                    //交换sort数组
                    boolean Temp=sorted_number_flag[j];
                    sorted_number_flag[j]=sorted_number_flag[j+1];
                    sorted_number_flag[j+1]=Temp;
                }else if (rank[j]==rank[j+1]){
                    if (distance[j]<distance[j+1]){
                        //我觉得这个交换可以写一个单独的函数出来了。。。
                        //交换距离数组
                        double Temp1=distance[j];
                        distance[j]=distance[j+1];
                        distance[j+1]=Temp1;
                        //交换rank数组
                        int TEMP1=rank[j];
                        rank[j]=rank[j+1];
                        rank[j+1]=TEMP1;
                        //交换函数数组
                        double[] temp=FuncVal[j];
                        FuncVal[j]=FuncVal[j+1];
                        FuncVal[j+1]=temp;
                        for(int k=0;k<dimension;k++)
                        {
                            double tem = FS[j][k];
                            FS[j][k]=FS[j+1][k];
                            FS[j+1][k]=tem;
                        }
                        //交换真实标号数组
                        int TEMP=sorted_number[j];
                        sorted_number[j]=sorted_number[j+1];
                        sorted_number[j+1]=TEMP;
                        //交换sort数组
                        boolean Temp=sorted_number_flag[j];
                        sorted_number_flag[j]=sorted_number_flag[j+1];
                        sorted_number_flag[j+1]=Temp;
                    }
                }
            }
            for(int j=0;j<best;j++)
                best_number_flag[sorted_number[j]]=true;
            for(int j=best;j<N_second_best+best;j++)
                sorted_number_flag[j]=true;			//标记吃过橡果的松鼠

        }
    }
    public void new_displace_pos()
    {
        double gc=1.9;
        for(int i=0;i<N;i++)
        {

            double r=Math.random();
            double pdp=0.1;
            if (r>=pdp)		//产生新位置（可能有捕食者）
            {
                if(i>=best&&i<best+N_second_best)
                {
                    for(int j=0;j<dimension;j++)
                    {
                        int n=(int) (Math.random()*best);
                        //System.out.println(n);
                       do{
                            double dg=0.5+Math.random()*0.61;//这里的dg这些东西。emmmmmm可真实令人头秃
                            FS[i][j]=FS[i][j]+dg*gc*(FS[n][j]-FS[i][j]);//上界与下界
                    } while(FS[i][j]<=FS_lower[j] || FS[i][j]>=FS_upper[j]);
                    }
                }
                else if(i>=best+N_second_best&&i<N&&sorted_number_flag[i]==false)
                {//这个函数没有跑是什么鬼？？？
                    int which_second_best = best+(int)(Math.random()*(N_second_best));
                    //System.out.println(which_second_best+"11111111111101112200");
                    for(int j=0;j<dimension;j++)
                    {
                       do{
                           double dg=0.5+Math.random()*0.61;//这里的dg这些东西。emmmmmm可真实令人头秃
                           FS[i][j]=FS[i][j]+dg*gc*(FS[which_second_best][j]-FS[i][j]);
                    } while(FS[i][j]<=FS_lower[j] || FS[i][j]>=FS_upper[j]);
                    }
                }
                else if(i>=N_second_best+best&&i<N&&sorted_number_flag[i]==true)
                {
                    for(int j=0;j<dimension;j++)
                    {
                        int n=(int)(Math.random()*best);
                        //System.out.println(n+"01112200");
                        do{
                            double dg=0.5+Math.random()*0.61;//这里的dg这些东西。emmmmmm可真实令人头秃
                            FS[i][j]=FS[i][j]+dg*gc*(FS[n][j]-FS[i][j]);
                        } while(FS[i][j]<=FS_lower[j] || FS[i][j]>=FS_upper[j]);
                    }
                }
            }
            else continue;
        }
    }
    public double Factorial(double n)		//阶乘
    {
        int m=10000;
        double An=1;
        for(int i=1;i<=m-1;i++)
        {
            An*=i/(i+n);
        }
        An=An*m*Math.pow((m+n/2), n-1);
        return An;
    }
    public double Levy(double n)  //列维函数
    {
        java.util.Random random = new java.util.Random();
        double ra=random.nextGaussian();
        double rb=random.nextGaussian();
        double beta=1.5;
        double sigma =Math.pow(((Factorial(beta)*Math.sin(Math.PI*beta/2))/(Factorial((beta-1)/2)*beta*Math.pow(2, ((beta-1)/2)))),1/beta);
        double levy=0.01*ra*sigma/(Math.pow((Math.abs(rb)),1/beta));
        return levy;
    }
    public void season_condition()	//判断季节条件
    {
        double Sct=0;
        double Smin=0.000001/Math.pow(365, 2.5*t/Round);
        for(int i=1;i<N_second_best;i++)
        {
            for(int j=0;j<dimension;j++)
            {
                Sct+=(FS[i][j]-FS[0][j])*(FS[i][j]-FS[0][j]);
            }
            Sct=Math.sqrt(Sct);
        }
        if(Sct<Smin)
        {
            for(int i=1;i<N;i++)
            {
                if(best_number_flag[sorted_number[i]]==false)
                {
                    for(int j=0;j<dimension;j++)
                    {
                            FS[i][j] = FS_lower[j] + Levy(N) * (FS_upper[j] - FS_lower[j]);
                        }
                }
                else continue;
            }
        }
    }
    public static void main(String[] args) throws InterruptedException
    {
        //主函数部分
        NsSsa test=new NsSsa();
        test.init();
        test.fast_nonsort();
        test.instance();
        test.evalue();
        test.evalue_sorted();
        for(t=0;t<Round;t++)
        {
            test.new_displace_pos();
            Thread.sleep(0);		//线程停止时间
            test.season_condition();
            Thread.sleep(0);
            test.evalue();
            Thread.sleep(0);
            test.fast_nonsort();
            test.instance();
            test.evalue_sorted();
            Thread.sleep(0);
        }
        test.fast_nonsort();
        for (int i=0;i<N;i++){
            if (rank[i]==1){
                //System.out.println(FS[i][0]);
                System.out.println(FuncVal[i][0]+"     "+FuncVal[i][1]+" "+i);
            }
        }
    }
}
