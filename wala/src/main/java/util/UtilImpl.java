package util;

import java.io.*;
import java.util.HashSet;
import java.util.Hashtable;
import java.util.Set;

//Util实现
public class UtilImpl implements Util {

    public Set<String> readChangedMethods(String changeInfoPath) throws IOException {
        File file = new File(changeInfoPath);
        BufferedReader bufferedReader = new BufferedReader(new FileReader(file));
        Set<String> changedMethods = new HashSet<String>();
        String s;
        while ((s = bufferedReader.readLine()) != null) {
            changedMethods.add(s.trim());
        }
        return changedMethods;
    }

    public Set<String> readChangedClasses(Set<String> changedMethods) {
        Set<String> changedClasses = new HashSet<String>();
        for (String changedMethod : changedMethods) {
            String innerClassName = changedMethod.split(" ")[0];
            changedClasses.add(innerClassName);
        }
        return changedClasses;
    }

    public void writeSelectionResultFile(String path, Set<String> res) throws IOException {
        File f = new File(path);
        if (f.exists()) {
            f.delete();
            f.createNewFile();
        }
        BufferedWriter writer = new BufferedWriter(new FileWriter(f));
        for (String s : res) {
            writer.append(s).append("\n");
            writer.flush();
        }
        writer.close();
    }

    public void constructDotFile(String postFix, Hashtable<String, Set<String>> methodsDirectlyCalled) throws IOException {
        String path = postFix + ".dot";
        File file = new File(path);
        BufferedWriter writer=new BufferedWriter(new FileWriter(file));
        writer.append("digraph ").append(postFix).append(" {\n");
        for(String key : methodsDirectlyCalled.keySet()){
            for(String method : methodsDirectlyCalled.get(key)){
                writer.append(String.format("    \"%s\" -> \"%s\";\n",key,method));
                writer.flush();
            }
        }
        writer.append("}");
        writer.flush();
        writer.close();
    }


}
