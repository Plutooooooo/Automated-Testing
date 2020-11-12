package util;

//Util实现
public class UtilImpl implements Util {

    public void parseCommand(String args[]){
        String cmd = args[0];
        String targetPath = args[1];
        String changeInfoPath = args[2];

        //-c执行类级别测试选择,-m执行方法级别测试选择
        if(cmd.equals("-c")){

        }else{

        }
    }
}
