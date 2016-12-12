package algo;

import game.BoardPosition;
import game.Game;
import org.neuroph.core.Layer;
import org.neuroph.core.NeuralNetwork;
import org.neuroph.core.data.DataSet;
import org.neuroph.core.data.DataSetRow;
import org.neuroph.core.input.WeightedSum;
import org.neuroph.core.transfer.Ramp;
import org.neuroph.nnet.MultiLayerPerceptron;
import org.neuroph.nnet.comp.neuron.InputOutputNeuron;
import org.neuroph.nnet.learning.BackPropagation;
import org.neuroph.util.NeuronProperties;
import org.neuroph.util.TransferFunctionType;
import util.FileHandler;

import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

/**
 * Use Neuroph Neural Network package to build network anduse temporal-different learning to
 * label the output of each move in each episode.
 *
 * Created by cj on 16-12-10.
 */
public class Neuroph {
    private NeuralNetwork nn;

    /**
     * Store data set for neural network
     * Each data set contains input array and output/label array
     */
    private DataSet dataset;

    /**
     * Construct four layers with two hidden layers
     * The number of neurons in each hidden layer needs to be tuned
     */
    private final int nInput = 385;
    private final int nHidden1 = 50;
    private final int nHidden2 = 10;
    private final int nOutput = 1;

    /**
     * @lambda is used in TD(lambda) to generate error signal
     */
    private final double lambda = 0.7;

    /**
     * Learning rate, maybe (auto) adjusted in training phase
     */
    private double alpha = 0.1;

    /**
     * Convergence threshold
     */
    private final double epsilon = 1e-5;

    /**
     * Constructor
     *
     * The activation functions for hidden layers are Ramp, i.e. ReLU, and
     * hyperbolic tangent function for output layer
     */
    public Neuroph() {
        nn = new MultiLayerPerceptron(TransferFunctionType.TANH, nInput, nOutput);

        NeuronProperties prop = new NeuronProperties(
                InputOutputNeuron.class,
                WeightedSum.class,
                Ramp.class
        );
        Layer hiddenLayer1 = new Layer(nHidden1, prop);
        nn.addLayer(1, hiddenLayer1);
        Layer hiddenLayer2 = new Layer(nHidden2, prop);
        nn.addLayer(2, hiddenLayer2);

        dataset = new DataSet(nInput, nOutput);
    }

    /**
     * Constructor
     *
     * @param path is the path of a saved neural network
     */
    public Neuroph(String path) {
        try {
            nn = NeuralNetwork.load(new FileInputStream(path));
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }
    }

    /**
     * Save the current neural network
     *
     * @param path indicates where to store on disk
     */
    public void exportNetwork(String path) {
        nn.save(path);
    }

    /**
     * Get constructed neural network
     *
     * @return current neural network
     */
    public NeuralNetwork network() {
        return nn;
    }

    /**
     * evaluate chess position after extracting the features
     *
     * @param features is a feature vector
     * @return the evaluated value of current chess position
     */
    public double evaluate(double[] features) {
        nn.setInput(features);
        nn.calculate();
        double[] output = nn.getOutput();
        return output[0];
    }

    /**
     * Train neural network
     *
     * Get all the records from @filename, extract features after each red move and use TD to
     * reset the desired output of each move
     *
     * @param filename is the input file path
     * @param result is the final result of the chess, 1 for red win, -1 for red loss, 0 for draw
     */
    public void train(String filename, int result) {
        List<String> records = loadRecordsFrom(filename);
        if (records == null) {
            System.err.println("Records empty.");
            return;
        }

        // max iteration for training
        int maxIter = 100;
        int iter = 0;
        double oldError = -1;

        // BackPropagation for learning rule, set maxIteration and learningRate
        BackPropagation backPropagation = new BackPropagation();
        backPropagation.setMaxIterations(1);
        backPropagation.setLearningRate(alpha);

        while (iter++ < maxIter) {
            double error = 0;
            for (String record : records) {
                setDataSet(record);
                error += setLabel(result);
                nn.learn(dataset, backPropagation);
            }

            // convergence?
            if (oldError >= 0) {
                double rate = Math.abs(error - oldError) / oldError;
                if (rate < epsilon) {
                    System.out.println("converged at " + iter);
                    iter = maxIter;
                }
            }
        }
    }

    /**
     * Load chess records of the input file
     *
     * @param filename is the input file path
     * @return a list of records with String format if success, null otherwise
     */
    private List<String> loadRecordsFrom(String filename) {
        BufferedReader br = FileHandler.read(filename);
        try {
            if (br != null) {
                List<String> records = new ArrayList<String>();
                String line;
                while ((line = br.readLine()) != null) {
                    String record = line.trim();
                    records.add(record);
                }
                return records;
            } else {
                System.err.println("read error");
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        return null;
    }

    /**
     * Put all the records in file into memory
     * Caution: if there is not enough memory available, use setDataSet(String)
     *
     * @param records is a list of the loaded records
     */
    private void setDataSet(List<String> records) {
        for (String record : records) {
            setDataSet(record);
        }
    }

    /**
     * Simulate the whole chess game, and add each red move into data set and set label as 0,
     * which will be updated by TD algorithm
     *
     * @param record is one episode of chess game
     */
    private void setDataSet(String record) {
        // clear the previous data
        dataset.clear();
        int n = record.length() / 4;

        Game game = new Game();
        game.initBoard();
        for (int t = 0; t < n; t++) {
            String move = record.substring(t*4, t*4+4);
            BoardPosition from = new BoardPosition(
                    Integer.parseInt(move.substring(1, 2)), Integer.parseInt(move.substring(0, 1)));
            BoardPosition to = new BoardPosition(
                    Integer.parseInt(move.substring(3, 4)), Integer.parseInt(move.substring(2, 3)));
            game.movePiece(from, to);

            // red player only
            if (t % 2 == 1) {
                continue;
            }

            game.changeTurn();
            List<List<Double>> feat = Feature.featureExtractor(game);
            double[] array = Feature.featureArray(feat);
            game.changeTurn();

            dataset.addRow(array, new double[]{0}); // y label will be updated later
        }
    }

    /**
     * Use TD algorithm to label the desired label of each chess position
     *
     * temporal difference of two successive move d[t] = J[t+1} - J[t], and
     * error signal \sum_{j=t}^{N-1} \lambda^{j-t} * d[t]
     *
     * Note: the true label of the final move is assumed to be the @result
     *
     * @param result is the final result at the end of the chess game
     * @return the squared error of each move evaluation
     */
    private double setLabel(int result) {
        int n = dataset.size();
        double[] J = evaluate();
        double totalError = 0;

        for (int t = 0; t < n-1; t++) {
            double tdiff = J[t+1] - J[t];
            double factor = 0;
            for (int j = t; j < n-1; j++) {
                factor = factor * lambda + 1;
            }
            double error = factor * tdiff;
            double desiredOutput = J[t] + error;
            totalError += error * error;

            DataSetRow row = dataset.getRowAt(t);
            row.setDesiredOutput(new double[]{desiredOutput});
        }

        DataSetRow row = dataset.getRowAt(n-1);
        row.setDesiredOutput(new double[]{result});
        double error = result - J[n-1];
        totalError += error * error;

        return totalError;
    }

    /**
     * Evaluate the estimated output of each input of data set
     *
     * @return an array of evaluation result
     */
    private double[] evaluate() {
        int n = dataset.size();
        double[] J = new double[n];
        int i = 0;

        Iterator<DataSetRow> iter = dataset.iterator();
        while (iter.hasNext()) {
            DataSetRow row = iter.next();
            nn.setInput(row.getInput());
            nn.calculate();
            double[] output = nn.getOutput();
            J[i++] = output[0];
        }

        return J;
    }

    private static String loadRecord(String filename) {
        BufferedReader br = FileHandler.read(filename);
        try {
            if (br != null) {
                String line = "";
                if ((line = br.readLine()) != null) {
                    line = line.trim();
                }
                return line;
            } else {
                System.err.println("read error");
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        return "";
    }

    public static void main(String[] args) {
        Neuroph neuroph = new Neuroph();
        String recordFile = "src/data/sample.txt";
        String record = loadRecord(recordFile);
        System.out.println(record.length()/4);
        neuroph.train(recordFile, 1);
        NeuralNetwork neuralNetwork = neuroph.network();
        Double[] weights = neuralNetwork.getWeights();
        double max = Double.NEGATIVE_INFINITY;
        double min = Double.POSITIVE_INFINITY;
        for (Double d : weights) {
            max = Math.max(max, d);
            min = Math.min(min, d);
        }
        System.out.println(max + "\t" + min);

        Game game = new Game();
        game.initBoard();
        List<List<Double>> feat = Feature.featureExtractor(game);
        double[] array = Feature.featureArray(feat);
        double value = neuroph.evaluate(array);
        System.out.println(value);

        game.initBoard(record);
        feat = Feature.featureExtractor(game);
        array = Feature.featureArray(feat);
        value = neuroph.evaluate(array);
        System.out.println(value);

    }
}
