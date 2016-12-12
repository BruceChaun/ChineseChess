import java.io.*;
import java.util.*;

import algo.*;
import game.Game;
import util.FileHandler;

public class Test {
    public static void main(String[] args) {
        new Test().run(args);
    }

    private void run(String[] args) {
//        testFeatureExtraction();
//        testNegative();
//        testNN();
          testMCTS();
        //testSim();
    }

    private void testSim() {
        String file = "src/data/movelist/lose.txt";
        BufferedReader br = FileHandler.read(file);
        
        if (br != null) {
            String strLine = "";
            try {
                while ((strLine = br.readLine()) != null) {
                    System.out.println(strLine);
                    Game game = new Game();
                    game.initBoard(strLine);
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
     * test feature extraction result
     */
    private List<List<Double>> testFeatureExtraction() {
        String file = "src/data/sample.txt";
        BufferedReader br = FileHandler.read(file);
        
        if (br != null) {
            String strLine;
            try {
                while ((strLine = br.readLine()) != null) {
                    Game game = new Game();
                    game.initBoard(strLine + "4353");
                    List<List<Double>> features = Feature.featureExtractor(game);
                    for (List<Double> fl : features) {
                        for (Double d : fl) {
                            System.out.print(d + "\t");
                        }
                        System.out.println();
                    }
                    return features;
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
        
        return null;
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

    /*
     * test neural network
     */
    private void testNN() {
        NN nn = new NN();
        List<List<Double>> features = new ArrayList<List<Double>>();
        for (int i = 0; i < 3; i++) {
            List<Double> list = new ArrayList<Double>();
            double d = new Random().nextDouble() * 5; 
            list.add(d);
//            System.out.print("features\n" + d + "\t");
            d = new Random().nextDouble() * 5;
//            System.out.println(d);
            list.add(d);
            features.add(list);
        }
        int[] n1 = {1,1,1};
        int n2 = 2;
        nn.init(features, n1, n2);
        for (int i = 0; i < 10; i++) {
            System.out.println("output\t" + nn.forward(features));
            nn.backpropagation(features, -1);
        }
    }
    
    /*
     * test MCTS
     */
    private void testMCTS() {
        String file = "src/data/sample.txt";
        BufferedReader br = FileHandler.read(file);
        Game game = new Game();
        if (br != null) {
            try {
                game.initBoard(br.readLine());
            } catch (IOException e) {
                game.initBoard();
            }
        } else {
            game.initBoard();
        }
        
        List<List<Double>> features = Feature.featureExtractor(game);
        NN nn = new NN();
        nn.init(features);
        
        MCTS mcts = new MCTS(nn);
        mcts.setRoot(game);
        for (int i = 0; i < 5; i++) {
            System.out.println("round " + i);
            mcts.search();
            mcts.printAllNodes(mcts.getRoot());
        }
    }
}
