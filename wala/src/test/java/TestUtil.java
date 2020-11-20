import org.junit.Test;

import java.io.*;
import java.util.HashSet;
import java.util.Set;

import static org.junit.Assert.*;

import static org.junit.Assert.assertEquals;

public class TestUtil {

    //只能一个个单独测试，测试前先生成对应selection-class.txt和selection-method.txt
    @Test
    public void ALUTest() throws IOException {
        Set<String> classLevelRes = readFile("selection-class.txt");
        Set<String> methodLevelRes = readFile("selection-method.txt");
        Set<String> classLevelData = readFile( "E:\\SE\\AutomatedTesting\\file\\经典大作业\\ClassicAutomatedTesting\\1-ALU\\data\\selection-class.txt");
        Set<String> methodLevelData = readFile("E:\\SE\\AutomatedTesting\\file\\经典大作业\\ClassicAutomatedTesting\\1-ALU\\data\\selection-method.txt");
        assertTrue(cmpRes(classLevelRes, classLevelData));
        assertTrue(cmpRes(methodLevelRes, methodLevelData));
    }

    @Test
    public void DataLogTest() throws IOException {
        Set<String> classLevelRes = readFile("selection-class.txt");
        Set<String> methodLevelRes = readFile("selection-method.txt");
        Set<String> classLevelData = readFile( "E:\\SE\\AutomatedTesting\\file\\经典大作业\\ClassicAutomatedTesting\\2-DataLog\\data\\selection-class.txt");
        Set<String> methodLevelData = readFile("E:\\SE\\AutomatedTesting\\file\\经典大作业\\ClassicAutomatedTesting\\2-DataLog\\data\\selection-method.txt");
        assertTrue(cmpRes(classLevelRes, classLevelData));
        assertTrue(cmpRes(methodLevelRes, methodLevelData));
    }

    @Test
    public void BinaryHeapTest() throws IOException {
        Set<String> classLevelRes = readFile("selection-class.txt");
        Set<String> methodLevelRes = readFile("selection-method.txt");
        Set<String> classLevelData = readFile( "E:\\SE\\AutomatedTesting\\file\\经典大作业\\ClassicAutomatedTesting\\3-BinaryHeap\\data\\selection-class.txt");
        Set<String> methodLevelData = readFile("E:\\SE\\AutomatedTesting\\file\\经典大作业\\ClassicAutomatedTesting\\3-BinaryHeap\\data\\selection-method.txt");
        assertTrue(cmpRes(classLevelRes, classLevelData));
        assertTrue(cmpRes(methodLevelRes, methodLevelData));
    }

    @Test
    public void NextDayTest() throws IOException {
        Set<String> classLevelRes = readFile("selection-class.txt");
        Set<String> methodLevelRes = readFile("selection-method.txt");
        Set<String> classLevelData = readFile( "E:\\SE\\AutomatedTesting\\file\\经典大作业\\ClassicAutomatedTesting\\4-NextDay\\data\\selection-class.txt");
        Set<String> methodLevelData = readFile("E:\\SE\\AutomatedTesting\\file\\经典大作业\\ClassicAutomatedTesting\\4-NextDay\\data\\selection-method.txt");
        assertTrue(cmpRes(classLevelRes, classLevelData));
        assertTrue(cmpRes(methodLevelRes, methodLevelData));
    }

    @Test
    public void MoreTriangleTest() throws IOException {
        Set<String> classLevelRes = readFile("selection-class.txt");
        Set<String> methodLevelRes = readFile("selection-method.txt");
        Set<String> classLevelData = readFile( "E:\\SE\\AutomatedTesting\\file\\经典大作业\\ClassicAutomatedTesting\\5-MoreTriangle\\data\\selection-class.txt");
        Set<String> methodLevelData = readFile("E:\\SE\\AutomatedTesting\\file\\经典大作业\\ClassicAutomatedTesting\\5-MoreTriangle\\data\\selection-method.txt");
        assertTrue(cmpRes(classLevelRes, classLevelData));
        assertTrue(cmpRes(methodLevelRes, methodLevelData));
    }

    private boolean cmpRes(Set<String> res, Set<String> data) {
        System.out.println("res.size is "+res.size());
        System.out.println("data.size is "+data.size());
        if (res.size() != data.size()) {
            return false;
        }
        for (String s : res) {
            if (!data.contains(s)) {
                return false;
            }
        }
        return true;
    }

    private Set<String> readFile(String path) throws IOException {
        File file = new File(path);
        BufferedReader bufferedReader = new BufferedReader(new FileReader(file));
        Set<String> changedMethods = new HashSet<String>();
        String s;
        while ((s = bufferedReader.readLine()) != null) {
            changedMethods.add(s.trim());
        }
        return changedMethods;
    }

}

