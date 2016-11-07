import java.io.*;
import java.util.List;

import algo.*;
import game.Game;
import util.FileHandler;

public class Test {
    public static void main(String[] args) {
        new Test().run(args);
    }

    private void run(String[] args) {
//        testFeatureExtraction();
        testNegative();
    }

    /*
     * test feature extraction result
     */
    private void testFeatureExtraction() {
        String file = "src/data/sample.txt";
        BufferedReader br = FileHandler.read(file);
        
        if (br != null) {
            String strLine;
            try {
                while ((strLine = br.readLine()) != null) {
                    Game game = new Game();
                    game.initBoard(strLine);
                    System.out.println(strLine);
                    List<List<Double>> features = Feature.featureExtractor(game);
                    for (List<Double> fl : features) {
                        for (Double d : fl) {
                            System.out.print(d + "\t");
                        }
                        System.out.println();
                    }
                }
            } catch (IOException e) {
                e.printStackTrace();
            } finally {
                try {
                    br.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
        
    }

    /*
     * test negative sample generator
     */
    private void testNegative() {
        String file = "src/data/sample.txt";
        BufferedReader br = FileHandler.read(file);
        
        if (br != null) {
            String strLine;
            String fileName = "src/data/negative.txt";
            try {
                while ((strLine = br.readLine()) != null) {
                    NegativeData.generate(strLine, 2, fileName, false);
                }
            } catch (IOException e) {
                e.printStackTrace();
            } finally {
                try {
                    br.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
    }
}
