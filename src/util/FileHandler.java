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
    
    public static PrintWriter write(String fileName) {
        PrintWriter writer;
        try {
            writer = new PrintWriter(fileName, "UTF-8");
            return writer;
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }
}
