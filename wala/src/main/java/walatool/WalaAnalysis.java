package walatool;

import com.ibm.wala.ipa.callgraph.AnalysisScope;
import com.ibm.wala.ipa.callgraph.CallGraph;
import com.ibm.wala.ipa.callgraph.cha.CHACallGraph;
import com.ibm.wala.ipa.cha.ClassHierarchyException;
import com.ibm.wala.shrikeCT.InvalidClassFileException;
import com.ibm.wala.util.CancelException;

import java.io.IOException;
import java.util.Hashtable;
import java.util.Set;

public interface WalaAnalysis {
    /**
     * 构建分析域，将targetPath下需要的.class文件加入分析域
     *
     * @param targetPath
     */
    public AnalysisScope setupAnalysisScope(String targetPath) throws IOException, InvalidClassFileException;

    /**
     * 根据AnalysisScope构建调用图
     *
     * @param analysisScope
     * @return
     */
    public CHACallGraph constructCallGraph(AnalysisScope analysisScope) throws ClassHierarchyException, CancelException;

    /**
     * 把所有的测试方法的签名记录下来便于后续使用
     *
     * @param cg
     * @return
     */
    public Set<String> getSignatureOfTestMethods(CHACallGraph cg);

    /**
     * 把所有的生产代码方法的签名记录下来便于后续使用
     *
     * @param cg
     * @return
     */
    public Set<String> getSignatureOfApplicationMethods(CHACallGraph cg);

    /**
     * 记录每个测试方法调用的所有方法，包括直接调用和间接调用
     *
     * @param cg
     * @param testMethodsSet
     * @return
     */
    public Hashtable<String, Set<String>> recordMethodsCalledByTest(CHACallGraph cg, Set<String> testMethodsSet);

    /**
     * 记录每个测试方法调用的所有类，包括直接调用和间接调用
     *
     * @param cg
     * @param methodsCalledByTest
     * @return
     */

    public Hashtable<String, Set<String>> recordClassesCalledByTest(CHACallGraph cg, Hashtable<String, Set<String>> methodsCalledByTest);
}
