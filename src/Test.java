import java.io.*;
import java.util.*;

import algo.*;
import game.*;
import util.FileHandler;

public class Test {
    public static void main(String[] args) {
        new Test().run(args);
    }

    private void run(String[] args) {
//        testFeatureExtraction();
//        testNegative();
        testNN();
//        testMCTS();
//        testSim();
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
        
        Game game = new Game();
        game.initBoard();
        Game g = game.copy();
        g.movePiece(new BoardPosition(9, 4), new BoardPosition(8, 4));
        List<List<Double>> f = Feature.featureExtractor(g);
        features = Feature.featureExtractor(game);
        nn.init(features);
//        nn.init(features, n1, n2);
        for (int i = 0; i < 100; i++) {
            System.out.println(nn.forward(features) + "\t" + nn.forward(f));
            nn.backpropagation(features, 0);
            nn.backpropagation(f, -1);
            nn.converge();
        }
        
        System.out.println("final\t"+nn.forward(features) + "\t" + nn.forward(f));
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
