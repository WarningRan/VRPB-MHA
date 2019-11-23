
import java.io.*;

public class GA2 {
    private static boolean debug=false;//debug参数
    private static int A=-10,B=30,C=20; //函数参数
    private static int N=10;    //种群个数
    private static int eLen=10;  //编码长度
    private static double pc=0.6;  //交配概率
    private static double pm=0.3; //变异概率
    private static int Round=1000;  //迭代上限
    private static int InteNum=1024;  //间隔数
    private static double []douPop;    //浮点种群
    private static double []CdouPop;//子代的浮点种群
    private static String []binPop;  //二进制种群
    private static String[] CbinPop;//子代二进制种群
    private static double []FuncVal; //函数值数组，这个数组是用来存储每一个y值得
    private static double []CFuncVal; //函数值数组，这个数组是用来存储每一个y值得
    private static double []Prob;  //概率数组，这个数组是来为轮盘赌做准备的
    private static double []BestPerRX; //每轮最优X
    private static double []BestPerRY; //每轮最优Y
    private static int[] round;  //轮次
    public  void init(){
        //初始化操作,构造函数
        douPop=new double[N];
        CdouPop=new double[N];
        binPop=new String[N];
        CbinPop=new String[N];
        FuncVal=new double[N];
        CFuncVal=new double[N];
        BestPerRX=new double[Round];
        BestPerRY=new double[Round];
        round=new int[Round];
        int in;
        double dou;
        for (int i=0;i<N;i++){
            in=(int)(Math.random()*InteNum);
            if (debug)  System.out.println(in);
            dou=(double)(in)/(double)(InteNum);
            if (debug)  System.out.println(dou);
            String s=Integer.toBinaryString(in);
            //浮点数
            while(s.length()<10) {
                s='0'+s;
            };
            if(debug){
                System.out.println(s);
            }
            //补0操作
            douPop[i]=dou;
            binPop[i]=s;
            //一个是浮点数种群，一个是二进制种群
            FuncVal[i]=A*dou*Math.sin(C/dou)+B;
            //对适应度的计算
            if (debug) System.out.println(dou+" "+FuncVal[i]+ "  "+s);

        }
        Prob=new double[N];
    }
    private void choose(){
        double sum=0.0;
        for(int i=0;i<N;i++) sum+=FuncVal[i];//这个种群的适应度之和
        Prob[0]=0.0;
        for(int i=0;i<N-1;i++) Prob[i+1]=FuncVal[i]/sum+Prob[i];//这里应该是后期的轮盘赌的概率
        //锦标赛法，感觉这里好像有点问题。//不用锦标赛了，改用轮盘赌
//        for(int i=0;i<N;i++){
//            int m,n;
//            m=(int)(Math.random()*N);
//            n=(int)(Math.random()*N);
//            binPop[i]=FuncVal[m]>FuncVal[n]?binPop[m]:binPop[n];
//            //二进制刷新,可能有问题
//        }
        //轮盘赌法,好像赌错了，收敛性更差了
        String[] bin=binPop;
        for (int i=0;i<N;i++){
            double m=Math.random();
            int pos=0;
            for(int j=0;j<N;j++){
                if (m==0){
                    pos=0;
                    break;
                }
                if(m-Prob[j]<0){
                    pos=j-1;
                    break;
                }
            }
            CbinPop[i]=bin[pos];
        }
    }
    private void Crosser(){
        //交叉：双亲双子法+随机概率小于pc则两两交配
        for(int i=0;i<N-1;i++){
            if(Math.random()<pc){//pc的意义就是变异可能发生的概率。
                int pos=(int)(Math.random()*N);
                if(debug){
                    System.out.println("position:"+pos);
                }
                String tmp=CbinPop[i].substring(pos);
                if(debug){
                    System.out.println("i"+i);
                }
                CbinPop[i]=CbinPop[i].substring(0,pos)+CbinPop[i+1].substring(pos);
                CbinPop[i+1]=CbinPop[i+1].substring(0,pos)+tmp;
                //这里执行的是字符串进行交换的操作
            }
        }
    }
    private void Mutation(){
        //变异操作
        for(int i=0;i<N;i++){
            if(Math.random()<pm){
                int pos=(int)(Math.random()*N);//随机化一个位置
                StringBuilder strBuilder = new StringBuilder(CbinPop[i]);   //String 不可变,就是始终是一个对象，
                if(CbinPop[i].charAt(pos)=='0'){
                    strBuilder.setCharAt(pos,'1');
                }
                else {
                    strBuilder.setCharAt(pos,'0');
                }
                //具体的变异操作
                CbinPop[i]=strBuilder.toString();
            }
        }
    }
    private void generate(){
        //更新种群的算法
        int tem;
        for (int i=0;i<N;i++){
            tem=0;
            tem=Integer.parseInt(CbinPop[i],2);
            CdouPop[i]=(double)tem/InteNum;//存到浮点数组中
            CFuncVal[i]=A*CdouPop[i]*Math.sin(C/CdouPop[i])+B;//求适应度
        }
    }
    public void FindBest(int r){
        //r代表的就是迭代的次数
        double maxX=-0x3f3f3f,maxY=-0x3f3f3f;
        for(int i=0;i<N;i++){
            if(CFuncVal[i]>maxY){
                maxY=CFuncVal[i]; maxX=CdouPop[i];
            }
        }
        BestPerRY[r]=maxY;
        BestPerRX[r]=maxX;

        //迭代的次数
        if (r!=0){
            if(BestPerRY[r]<BestPerRY[r-1]){
                CdouPop=new double[N];
                CbinPop=new String[N];
                CFuncVal=new double[N];
                BestPerRY[r]=BestPerRY[r-1];
                BestPerRX[r]=BestPerRX[r-1];
            }else{
                douPop=CdouPop;
                binPop=CbinPop;
                FuncVal=CFuncVal;
                CFuncVal=new double[N];
                CdouPop=new double[N];
                CbinPop=new String[N];
            }
        }
        System.out.println("第"+r+"次"+"最优x的值为    "+BestPerRX[r]+"    y的值为"+BestPerRY[r]);

        if(r==0) round[r]=1;
        else round[r]=round[r-1]+1;
    }
    public void run(){
        init();
        for(int i=0;i<Round;i++) {
            choose();  //选择
            Crosser();  //交叉
            Mutation();// 变异
            generate();  //更新douPop，FuncVal数组
            FindBest(i);  //找出每轮最优

        }

        System.out.printf("The best x is %f, the function value is %f, the round is: %d\n",BestPerRX[Round-1],BestPerRY[Round-1],round[Round-1]);
    }
    public void w()  {
        File a=new File("G:\\gxh.txt");
        FileWriter fw= null;
        try {
            fw = new FileWriter(a);
            BufferedWriter bw=new BufferedWriter(fw);
            for (int i=0;i<Round;i++){
                bw.write(String.valueOf(BestPerRX[i]));
                bw.newLine();
            }
            bw.close();
        } catch (IOException e) {
            e.printStackTrace();
        }


    }
    public void w2()  {
        File a=new File("G:\\zfh.txt");
        FileWriter fw= null;
        try {
            fw = new FileWriter(a);
            BufferedWriter bw=new BufferedWriter(fw);
            for (int i=0;i<Round;i++){
                bw.write(String.valueOf(BestPerRY[i]));
                bw.newLine();
            }
            bw.close();
        } catch (IOException e) {
            e.printStackTrace();
        }


    }
    public static void main(String[] args){
        if(debug){
            System.out.println("11111111");
        }
        GA2 test=new GA2();
        if(debug){
            System.out.println("11111111");
        }
        test.run();
        test.w();
        test.w2();
        System.out.println("运行完成");
    }

}

