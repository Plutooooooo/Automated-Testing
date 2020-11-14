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

    private static void boot(String[] args) throws IOException, InvalidClassFileException, CancelException, ClassHierarchyException {
       /* String cmd = args[0];
        String targetPath = args[1];
        String changeInfoPath = args[2];*/

        String targetPath = "E:\\SE\\AutomatedTesting\\file\\经典大作业\\ClassicAutomatedTesting\\0-CMD\\target";
        String changeInfoPath = "E:\\SE\\AutomatedTesting\\file\\经典大作业\\ClassicAutomatedTesting\\0-CMD\\data\\change_info.txt";
        initWalaInfo(targetPath);
        initChangeInfo(changeInfoPath);
        Set<String> res = new HashSet<String>();//测试用例选择结果
        res = selectTestsOnClassLevel();
        for(String s : res){
            System.out.println("in class level res "+s);
        }
        System.out.println("-----------");

        res = selectTestsOnMethodLevel();
        for(String s : res){
            System.out.println("in res "+s);
        }
        System.out.println("-----------");

        //-c执行类级别测试选择,-m执行方法级别测试选择
        /*if (cmd.equals("-c")) {
            res = selectTestsOnClassLevel();
            for(String s : res){
                System.out.println("in res "+s);
            }
            System.out.println("-----------");
        } else {
            res = selectTestsOnMethodLevel();
            for(String s : res){
                System.out.println("in res "+s);
            }
            System.out.println("-----------");
        }*/
    }
    public static void main(String[] args) throws CancelException, ClassHierarchyException, InvalidClassFileException, IOException {
        boot(args);
    }

}
