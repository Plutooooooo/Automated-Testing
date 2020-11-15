package util;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.Hashtable;
import java.util.Set;

public interface Util {

    /**
     * 从changeInfo文件里读出被修改了的方法(InnerClassName+" "+signature)
     *
     * @param changeInfoPath change_info的绝对路径
     * @return
     * @throws FileNotFoundException
     * @throws IOException
     */
    public Set<String> readChangedMethods(String changeInfoPath) throws IOException;

    /**
     * 根据改变的方法分析改变的类
     *
     * @param changedMethods 变更了的方法签名集合
     * @return
     */
    public Set<String> readChangedClasses(Set<String> changedMethods);

    /**
     * 将选中的测试用例签名写到指定路径
     * @param path
     * @param res
     */
    public void writeSelectionResultFile(String path, Set<String> res) throws IOException;

    /**
     * 生成dot文件的方法
     * @param postFix 路径后的后缀(class/method)
     * @param methodsDirectlyCalled
     */
    public void constructDotFile(String postFix, Hashtable<String, Set<String>> methodsDirectlyCalled) throws IOException;
}
