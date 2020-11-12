package walatool;

import com.ibm.wala.ipa.callgraph.AnalysisScope;
import com.ibm.wala.ipa.callgraph.cha.CHACallGraph;
import com.ibm.wala.ipa.cha.ClassHierarchyException;
import com.ibm.wala.shrikeCT.InvalidClassFileException;
import com.ibm.wala.util.CancelException;

import java.io.IOException;

public interface WalaAnalysis {
    /**
     * 构建分析域，将targetPath下需要的.class文件加入分析域
     * @param targetPath
     */
    public AnalysisScope setupAnalysisScope(String targetPath) throws IOException, InvalidClassFileException;

    /**
     * 根据AnalysisScope构建调用图
     * @param analysisScope
     * @return
     */
    public CHACallGraph constructCallGraph(AnalysisScope analysisScope) throws ClassHierarchyException, CancelException;
}
