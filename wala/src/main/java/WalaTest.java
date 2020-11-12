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
import com.ibm.wala.util.CancelException;
import com.ibm.wala.util.config.AnalysisScopeReader;
import com.ibm.wala.util.io.FileProvider;
import walatool.WalaAnalysis;
import walatool.WalaAnalysisImpl;

import java.io.File;
import java.io.IOException;

public class WalaTest {
    public static void main(String args[]) throws IOException, ClassHierarchyException, InvalidClassFileException, CancelException {
        WalaAnalysis walaAnalysis = new WalaAnalysisImpl();
        AnalysisScope scope = walaAnalysis.setupAnalysisScope("E:\\SE\\AutomatedTesting\\file\\经典大作业\\ClassicAutomatedTesting\\0-CMD\\target");
        /*File exFile = new FileProvider().getFile("exclusion.txt");
        System.out.println(exFile.getAbsolutePath());
        AnalysisScope scope = AnalysisScopeReader.readJavaScope("scope.txt", exFile, WalaTest.class.getClassLoader());
        File test = new FileProvider().getFile("E:\\SE\\AutomatedTesting\\file\\经典大作业\\ClassicAutomatedTesting\\1-ALU\\target\\classes\\net\\mooctest\\ALU.class");
        System.out.println(test);
        scope.addClassFileToScope(ClassLoaderReference.Application, test);*/
        ClassHierarchy cha = ClassHierarchyFactory.makeWithRoot(scope);
        Iterable<Entrypoint> eps = new AllApplicationEntrypoints(scope, cha);
        CHACallGraph cg = new CHACallGraph(cha);
        cg.init(eps);
        int i = 0;
        for (CGNode node : cg) {
            // node中包含了很多信息，包括类加载器、方法信息等，这里只筛选出需要的信息
            if (node.getMethod() instanceof ShrikeBTMethod) {
                // node.getMethod()返回一个比较泛化的IMethod实例，不能获取到我们想要的信息
                ShrikeBTMethod method = (ShrikeBTMethod) node.getMethod();
                // 使用Primordial类加载器加载的类都属于Java原生类，我们一般不关心。
                if ("Application".equals(method.getDeclaringClass().getClassLoader().toString())) {
                    // 获取声明该方法的类的内部表示
                    System.out.println(node.getGraphNodeId());
                    String classInnerName = method.getDeclaringClass().getName().toString();
                    String signature = method.getSignature();
                    System.out.println(classInnerName + " " + signature);
                    System.out.println("----------");
                }
            } else {
                System.out.println(String.format("'%s'不是一个ShrikeBTMethod：%s", node.getMethod(), node.getMethod().getClass()));
            }
        }
    }

}
