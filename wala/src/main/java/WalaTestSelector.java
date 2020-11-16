import com.ibm.wala.ipa.callgraph.AnalysisScope;
import com.ibm.wala.ipa.callgraph.Entrypoint;
import com.ibm.wala.ipa.callgraph.cha.CHACallGraph;
import com.ibm.wala.ipa.callgraph.impl.AllApplicationEntrypoints;
import com.ibm.wala.ipa.cha.ClassHierarchy;
import com.ibm.wala.ipa.cha.ClassHierarchyException;
import com.ibm.wala.ipa.cha.ClassHierarchyFactory;
import com.ibm.wala.shrikeCT.InvalidClassFileException;
import com.ibm.wala.util.CancelException;
import util.Util;
import util.UtilImpl;
import walatool.WalaAnalysis;
import walatool.WalaAnalysisImpl;

import java.io.IOException;
import java.util.HashSet;
import java.util.Hashtable;
import java.util.Set;

public class WalaTestSelector {
    private static WalaAnalysis walaAnalysis = new WalaAnalysisImpl();//封装的Wala工具类
    private static Util util = new UtilImpl();//Util工具类

    private static AnalysisScope scope;//分析域
    private static ClassHierarchy cha;//类继承关系
    private static Iterable<Entrypoint> eps;//进入点
    private static CHACallGraph cg;//调用图
    private static Set<String> testMethods;//测试方法签名集合(InnerClassName+" "+signature)
    private static Set<String> applicationMethods;//生产代码方法签名集合(InnerClassName+" "+signature)
    private static Set<String> changedMethods;//通过change_info.txt读到的变更方法的签名集合
    private static Set<String> changedClasses;//通过change_info.txt读到的变更类的集合
    private static Hashtable<String, Set<String>> methodsCalledByTest;//测试方法调用的所有方法集合(包括直接和间接调用)
    private static Hashtable<String, Set<String>> classesCalledByTest;//测试方法调用的所有类集合(包括直接和间接调用)
    private static Hashtable<String, Set<String>> methodsDirectlyCalled;//每个方法直接调用的方法，用于生成dot文件
    private static Hashtable<String, Set<String>> classesDirectlyCalled;//每个方法直接调用的类，用于生成dot文件

    //类级别测试选择理解错了，修改部分
    private static Hashtable<String, Set<String>> methodsUnderTestClass;//记录每个测试类下有哪些方法，类级别测试选择时，一个类改动，调用这个类的测试类的全部方法都要选择


    //初始化与Wala相关的信息，依据targetPath建立分析域
    private static void initWalaInfo(String targetPath) throws IOException, InvalidClassFileException, ClassHierarchyException, CancelException {
        scope = walaAnalysis.setupAnalysisScope(targetPath);
        cha = ClassHierarchyFactory.makeWithRoot(scope);
        eps = new AllApplicationEntrypoints(scope, cha);
        cg = new CHACallGraph(cha);
        cg.init(eps);
        testMethods = walaAnalysis.getSignatureOfTestMethods(cg);
        applicationMethods = walaAnalysis.getSignatureOfApplicationMethods(cg);
        methodsCalledByTest = walaAnalysis.recordMethodsCalledByTest(cg, testMethods);
        classesCalledByTest = walaAnalysis.recordClassesCalledByTest(cg, methodsCalledByTest);
        methodsDirectlyCalled = walaAnalysis.recordMethodsDirectlyCalled(cg);
        classesDirectlyCalled = walaAnalysis.recordClassesDirectlyCalled(cg,methodsDirectlyCalled);
    }

    //获得变更信息
    private static void initChangeInfo(String changeInfoPath) throws IOException {
        changedMethods = util.readChangedMethods(changeInfoPath);
        changedClasses = util.readChangedClasses(changedMethods);
    }

    //方法级别选取测试用例
    private static Set<String> selectTestsOnMethodLevel() {
        Set<String> res = new HashSet<String>();
        for (String testMethod : testMethods) {
            boolean flag = false;
            for (String changedMethod : changedMethods) {
                if (methodsCalledByTest.get(testMethod).contains(changedMethod)) {
                    flag = true;
                    break;
                }
            }
            if (flag) {
                //说明该测试用例调用了这个变更的方法
                res.add(testMethod);
            }
        }
        return res;
    }

    //类级别选取测试用例
    private static Set<String> selectTestsOnClassLevel() {
        Set<String> res = new HashSet<String>();
        for (String testMethod : testMethods) {
            boolean flag = false;
            for (String changedClass : changedClasses) {
                if (classesCalledByTest.get(testMethod).contains(changedClass)) {
                    flag = true;
                    break;
                }
            }
            if (flag) {
                //说明该测试用例调用了这个变更的类
                res.add(testMethod);
            }
        }
        return res;
    }

    private static void start(String[] args) throws IOException, InvalidClassFileException, CancelException, ClassHierarchyException {
        String cmd = args[0];
        String targetPath = args[1];
        String changeInfoPath = args[2];
        System.out.println("xxxxxxxxx");
        System.out.println(System.getProperty("user.dir"));
        initWalaInfo(targetPath);
        initChangeInfo(changeInfoPath);
        Set<String> res = new HashSet<String>();//测试用例选择结果
        //-c执行类级别测试选择,-m执行方法级别测试选择
        if (cmd.equals("-c")) {
            res = selectTestsOnClassLevel();
            util.writeSelectionResultFile(System.getProperty("user.dir")+"/selection-class.txt",res);
            util.constructDotFile("class",classesDirectlyCalled);
        } else {
            res = selectTestsOnMethodLevel();
            util.writeSelectionResultFile(System.getProperty("user.dir")+"/selection-method.txt",res);
            util.constructDotFile("method",methodsDirectlyCalled);
        }

    }
    public static void main(String[] args) throws CancelException, ClassHierarchyException, InvalidClassFileException, IOException {
        start(args);
    }

}

    /*String targetPath = "E:\\SE\\AutomatedTesting\\file\\经典大作业\\ClassicAutomatedTesting\\1-ALU\\target";
           String changeInfoPath = "E:\\SE\\AutomatedTesting\\file\\经典大作业\\ClassicAutomatedTesting\\1-ALU\\data\\change_info.txt";*/
       /* String targetPath = "E:\\SE\\AutomatedTesting\\file\\经典大作业\\ClassicAutomatedTesting\\0-CMD\\target";
        String changeInfoPath = "E:\\SE\\AutomatedTesting\\file\\经典大作业\\ClassicAutomatedTesting\\0-CMD\\data\\change_info.txt";*/
  /*  String targetPath = "E:\\SE\\AutomatedTesting\\file\\经典大作业\\ClassicAutomatedTesting\\2-DataLog\\target";
    String changeInfoPath = "E:\\SE\\AutomatedTesting\\file\\经典大作业\\ClassicAutomatedTesting\\2-DataLog\\data\\change_info.txt";*/