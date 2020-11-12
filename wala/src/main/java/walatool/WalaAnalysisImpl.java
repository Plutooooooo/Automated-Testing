package walatool;

import com.ibm.wala.ipa.callgraph.AnalysisScope;
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

import java.io.File;
import java.io.IOException;

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
}
