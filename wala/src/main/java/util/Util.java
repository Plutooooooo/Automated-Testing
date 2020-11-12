package util;

public interface Util {
    /**
     * 用于解析命令行传入的参数，决定执行那种级别的测试选择
     * @param args 接收命令行参数字符串
     */
    public void parseCommand(String args[]);
}
