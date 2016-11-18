package util;

import java.io.*;

public class FileHandler {
    public static BufferedReader read(String fileName) {
        FileInputStream fstream;
        
        try {
            fstream = new FileInputStream(fileName);
            BufferedReader br = new BufferedReader(new InputStreamReader(fstream));
            return br;
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }
    
    public static FileWriter write(String fileName, boolean append) {
        try {
            FileWriter fw = new FileWriter(fileName, append);
            return fw;
        } catch (IOException e1) {
            e1.printStackTrace();
            return null;
        }
    }
}
