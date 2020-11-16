package walatool;

import com.ibm.wala.classLoader.IMethod;
import com.ibm.wala.classLoader.ShrikeBTMethod;
import com.ibm.wala.ipa.callgraph.AnalysisScope;
import com.ibm.wala.ipa.callgraph.CGNode;
import com.ibm.wala.ipa.callgraph.Entrypoint;
import com.ibm.wala.ipa.callgraph.cha.CHACallGraph;
import com.ibm.wala.ipa.callgraph.impl.AllApplicationEntrypoints;
import com.ibm.wala.ipa.cha.ClassHierarchy;
import com.ibm.wala.ipa.cha.ClassHierarchyException;
import com.ibm.wala.ipa.cha.ClassHierarchyFactory;
import com.ibm.wala.shrikeCT.InvalidClassFileException;
import com.ibm.wala.types.ClassLoaderReference;
import com.ibm.wala.types.annotations.Annotation;
import com.ibm.wala.util.CancelException;
import com.ibm.wala.util.config.AnalysisScopeReader;
import com.ibm.wala.util.io.FileProvider;

import java.io.File;
import java.io.IOException;
import java.util.*;

public class WalaAnalysisImpl implements WalaAnalysis {

    public AnalysisScope setupAnalysisScope(String targetPath) throws IOException, InvalidClassFileException {
        //先构建原生类分析域，再以此将目标代码加入分析域
        File exFile = new FileProvider().getFile("exclusion.txt");
        AnalysisScope scope = AnalysisScopeReader.readJavaScope("scope.txt", exFile, WalaAnalysisImpl.class.getClassLoader());
        //生产代码字节码文件
        File[] classes = new File(targetPath + "\\classes\\net\\mooctest").listFiles();
        //测试代码字节码文件
        File[] testClasses = new File(targetPath + "\\test-classes\\net\\mooctest").listFiles();
        for (File clazz : classes) {
            scope.addClassFileToScope(ClassLoaderReference.Application, clazz);
        }
        for (File testClass : testClasses) {
            scope.addClassFileToScope(ClassLoaderReference.Application, testClass);
        }
        System.out.println(scope);
        System.out.println("---------------------------------");
        return scope;
    }

    public CHACallGraph constructCallGraph(AnalysisScope analysisScope) throws ClassHierarchyException, CancelException {
        //生成类层次关系对象
        ClassHierarchy cha = ClassHierarchyFactory.makeWithRoot(analysisScope);
        //生成进入点
        Iterable<Entrypoint> eps = new AllApplicationEntrypoints(analysisScope, cha);
        CHACallGraph cg = new CHACallGraph(cha);
        //初始化调用图
        cg.init(eps);
        return cg;
    }

    public Set<String> getSignatureOfTestMethods(CHACallGraph cg) {
        Set<String> testMethods = new HashSet<String>();
        for (CGNode node : cg) {
            if (node.getMethod() instanceof ShrikeBTMethod) {
                // node.getMethod()返回一个比较泛化的IMethod实例，不能获取到我们想要的信息
                ShrikeBTMethod method = (ShrikeBTMethod) node.getMethod();
                // 使用Primordial类加载器加载的类都属于Java原生类，我们一般不关心。
                if ("Application".equals(method.getDeclaringClass().getClassLoader().toString())) {
                    String classInnerName = method.getDeclaringClass().getName().toString();
                    String signature = method.getSignature();
                    //用反射机制拿到@Test注解来判断是不是测试方法
                    if (isTest(method)) {
                        testMethods.add(classInnerName + " " + signature);
                    }
                }
            } else {
                System.out.println(String.format("'%s'不是一个ShrikeBTMethod：%s", node.getMethod(), node.getMethod().getClass()));
            }
        }
        return testMethods;
    }

    public Set<String> getSignatureOfApplicationMethods(CHACallGraph cg) {
        Set<String> appMethods = new HashSet<String>();
        for (CGNode node : cg) {
            if (node.getMethod() instanceof ShrikeBTMethod) {
                // node.getMethod()返回一个比较泛化的IMethod实例，不能获取到我们想要的信息
                ShrikeBTMethod method = (ShrikeBTMethod) node.getMethod();
                // 使用Primordial类加载器加载的类都属于Java原生类，我们一般不关心。
                if ("Application".equals(method.getDeclaringClass().getClassLoader().toString())) {
                    String classInnerName = method.getDeclaringClass().getName().toString();
                    String signature = method.getSignature();
                    //用反射机制拿到@Test注解来判断是不是测试方法
                    if (!isTest(method)) {
                        appMethods.add(classInnerName + " " + signature);
                    }
                }
            } else {
                System.out.println(String.format("'%s'不是一个ShrikeBTMethod：%s", node.getMethod(), node.getMethod().getClass()));
            }
        }
        return appMethods;
    }


    public Hashtable<String, Set<String>> recordMethodsCalledByTest(CHACallGraph cg, Set<String> testMethodsSet) {
        Hashtable<String, Set<String>> hashtable = new Hashtable<String, Set<String>>();
        DFSCG(hashtable, testMethodsSet, cg);
        return hashtable;
    }

    public Hashtable<String, Set<String>> recordClassesCalledByTest(CHACallGraph cg, Hashtable<String, Set<String>> methodsCalledByTest) {
        Hashtable<String, Set<String>> hashtable = new Hashtable<String, Set<String>>();
        for (String key : methodsCalledByTest.keySet()) {
            Set<String> calledClasses = new HashSet<String>();
            for (String methods : methodsCalledByTest.get(key)) {
                //按空格分割，第一个字符串就是InnerClassName
                String innerClassName = methods.split(" ")[0];
                calledClasses.add(innerClassName);
            }
            hashtable.put(key, calledClasses);
        }
        return hashtable;
    }

    public Hashtable<String, Set<String>> recordMethodsDirectlyCalled(CHACallGraph cg) {
        Hashtable<String, Set<String>> hashtable = new Hashtable<String, Set<String>>();
        for(CGNode node : cg){
            if (node.getMethod() instanceof ShrikeBTMethod){
                ShrikeBTMethod method = (ShrikeBTMethod) node.getMethod();
                if ("Application".equals(method.getDeclaringClass().getClassLoader().toString())){
                    String currentMethod = getMethodSignature(method);
                    if(!hashtable.containsKey(currentMethod)){
                        Set<String> calledMethods = new HashSet<String>();
                        Iterator<CGNode> sucNodes = cg.getSuccNodes(node);
                        //记录下所有sucNode代表的方法即可
                        for(Iterator<CGNode> it = sucNodes;it.hasNext();){
                            CGNode sucNode = sucNodes.next();
                            if(sucNode.getMethod() instanceof ShrikeBTMethod){
                                ShrikeBTMethod sucNodeMethod = (ShrikeBTMethod) sucNode.getMethod();
                                if("Application".equals(sucNodeMethod.getDeclaringClass().getClassLoader().toString())){
                                    String sucNodeMethodSignature = getMethodSignature(sucNodeMethod);
                                    calledMethods.add(sucNodeMethodSignature);
                                }
                            }
                        }
                        hashtable.put(currentMethod,calledMethods);
                    }
                }
            }
        }
        return hashtable;
    }

    public Hashtable<String, Set<String>> recordClassesDirectlyCalled(CHACallGraph cg, Hashtable<String, Set<String>> methodsDirectlyCalled) {
        Hashtable<String, Set<String>> hashtable = new Hashtable<String, Set<String>>();
        for(String key : methodsDirectlyCalled.keySet()){
            hashtable.put(key.split(" ")[0],new HashSet<String>());
        }
        for (String key : methodsDirectlyCalled.keySet()) {
            Set<String> calledClasses = hashtable.get(key.split(" ")[0]);
            for (String methods : methodsDirectlyCalled.get(key)) {
                //按空格分割，第一个字符串就是InnerClassName
                String innerClassName = methods.split(" ")[0];
                calledClasses.add(innerClassName);
            }
            hashtable.put(key.split(" ")[0], calledClasses);
        }
        return hashtable;
    }

    @Override
    public Hashtable<String, Set<String>> recordMethodsUnderTestClass(Set<String> testMethods) {
        Hashtable<String, Set<String>> hashtable = new Hashtable<String, Set<String>>();
        for(String testMethod : testMethods){
            String testClass = testMethod.split(" ")[0];
            if(!hashtable.containsKey(testClass)){
                hashtable.put(testClass,new HashSet<String>());
            }else{
                Set<String> methodsUnderTestClass = hashtable.get(testClass);
                methodsUnderTestClass.add(testMethod.split(" ")[1]);
            }
        }
        return hashtable;
    }

    @Override
    public Hashtable<String, Set<String>> recordClassesCalledByTestClass(Hashtable<String, Set<String>> classesCalledByTest) {
        Hashtable<String, Set<String>> hashtable = new Hashtable<String, Set<String>>();
        for(String s : classesCalledByTest.keySet()){
            String testClass = s.split(" ")[0];
            if(!hashtable.containsKey(testClass)){
                hashtable.put(testClass,new HashSet<String>());
            }
            Set<String> classesCalled = hashtable.get(testClass);
            classesCalled.addAll(classesCalledByTest.get(s));
            hashtable.put(testClass,classesCalled);
        }
        return hashtable;
    }


    /**
     * 深搜遍历调用图，获取测试用例直接/间接调用的所有生产代码方法
     *
     * @param hashtable
     * @param testMethodsSet
     * @param cg
     */
    private void DFSCG(Hashtable<String, Set<String>> hashtable, Set<String> testMethodsSet, CHACallGraph cg) {
        for (CGNode node : cg) {
            if (node.getMethod() instanceof ShrikeBTMethod) {
                ShrikeBTMethod method = (ShrikeBTMethod) node.getMethod();
                if ("Application".equals(method.getDeclaringClass().getClassLoader().toString())) {
                    //通过判断测试方法集合是否包含该方法判断是不是测试方法
                    if (testMethodsSet.contains(getMethodSignature(method))) {
                        String currentMethod = getMethodSignature(method);
                        if (!hashtable.containsKey(currentMethod)) {
                            //hashtable还没有存该测试方法
                            hashtable.put(currentMethod, new HashSet<String>());
                            Iterator<CGNode> sucNodes = cg.getSuccNodes(node);
                            Set<String> calledMethods = hashtable.get(currentMethod);
                            dfs(sucNodes, calledMethods, cg,currentMethod);

                            hashtable.put(currentMethod, calledMethods);
                        } else {
                            //已经有了该测试方法的记录
                            Iterator<CGNode> sucNodes = cg.getSuccNodes(node);
                            Set<String> calledMethods = hashtable.get(currentMethod);
                            dfs(sucNodes, calledMethods, cg,currentMethod);
                            hashtable.put(currentMethod, calledMethods);
                        }
                    }
                }
            } else {
                System.out.println(String.format("'%s'不是一个ShrikeBTMethod：%s", node.getMethod(), node.getMethod().getClass()));
            }
        }
    }

    private void dfs(Iterator<CGNode> sucNodes, Set<String> calledMethods, CHACallGraph cg,String rootMethod) {
        if (!sucNodes.hasNext()) {
            return;
        }
        for (Iterator<CGNode> it = sucNodes; it.hasNext(); ) {
            CGNode sucNode = it.next();
            if (isApplicationMethod(sucNode.getMethod())) {
                //如果该方法没有被加入set则加入
                if (!calledMethods.contains(getMethodSignature((ShrikeBTMethod) sucNode.getMethod()))) {
                    calledMethods.add(getMethodSignature((ShrikeBTMethod) sucNode.getMethod()));
                }else{
                    continue;
                }
            } else {
                //不是生产代码的就不用管了
                continue;
            }
            if(getMethodSignature((ShrikeBTMethod)sucNode.getMethod()).equals(rootMethod)){
                continue;
            }
            dfs(cg.getSuccNodes(sucNode), calledMethods, cg,rootMethod);
        }
    }

    //判断是否是生产代码方法
    private boolean isApplicationMethod(IMethod iMethod) {
        ShrikeBTMethod method = (ShrikeBTMethod) iMethod;
        return "Application".equals(method.getDeclaringClass().getClassLoader().toString());
    }

    //通过反射机制拿到注解判断是否是测试方法
    private boolean isTest(ShrikeBTMethod method){
        Collection<Annotation> annotations = method.getAnnotations();
        for(Annotation annotation : annotations){
            if(new String(annotation.getType().getName().getClassName().getValArray()).equals("Test")){
                return true;
            }
        }
        return false;
    }

    /**
     * 按照内部类名+" "+签名返回方法的表示
     *
     * @param method
     * @return
     */
    private String getMethodSignature(ShrikeBTMethod method) {
        return method.getDeclaringClass().getName().toString() + " " + method.getSignature();
    }
}
