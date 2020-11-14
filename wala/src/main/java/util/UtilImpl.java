package util;

import java.io.*;
import java.util.HashSet;
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
}
