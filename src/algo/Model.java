package algo;

import java.io.BufferedReader;
import java.io.IOException;

import game.Game;
import util.FileHandler;

public class Model {
    public static void main(String[] args) {
        new Model().run();
    }

    private void run() {
        train();
    }

    private void train() {
        String recordFile = "src/data/sample.txt";
        String paraFile = "src/data/parameter.txt";
        final int maxIter = 10;
        int result = 1;
        
        NN nn = new NN();
        Game game = new Game();
        game.initBoard();
        nn.init(Feature.featureExtractor(game));
//        nn.init(paraFile);
        TD td = new TD();
        
        for (int i = 0; i < maxIter; i++) {
            System.out.println("Round "+i);
            BufferedReader br = FileHandler.read(recordFile);
            
            String line = "";
            try {
                if (br != null) {
                    while ((line = br.readLine()) != null) {
                        String record = line.trim();
                        td.TDtrain(nn, record, result);
//                        nn.backpropagation(result, record);
                    }
                } else {
                    System.err.println("read error");
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
            
            if (nn.converge()) break;
        }
        
        nn.exportParameters("p.txt");
    }
}
