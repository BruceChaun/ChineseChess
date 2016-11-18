package algo;

import java.io.BufferedReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.*;

import game.*;
import util.FileHandler;

/*
 * This is the neural network specific to the Chinese chess project, 
 * so the model has no ability to generalize.
 */
public class NN {
    /*
     * We use one input layer, two hidden layers and one output layer.
     * input is separated into three parts: global, piece-centric, and 
     * position-centric, and they are all independent from input-hidden 
     * layer. Between the two hidden layers are features fully connected. 
     * Activation function of hidden layers are ReLU and of output layer 
     * tanh.
     */
    // weights
    private double[][] globalWeights;
    private double[][] pieceWeights;
    private double[][] positionWeights;
    private double[][] hiddenLayerWeights;
    private double[][] outputWeights;
    
    // delta weight in last epoch
    private double[][] globalDelta;
    private double[][] pieceDelta;
    private double[][] positionDelta;
    private double[][] hiddenLayerDelta;
    private double[][] outputDelta;
    
    private int numGlobalInput;
    private int numPieceInput;
    private int numPositionInput;
    private int numHidden11 = 4;
    private int numHidden12 = 14;
    private int numHidden13 = 14;
    private int numHidden1;
    private int numHidden2 = 6;
    private int numOutput;
    
    // store activated values in the hidden layers
    private List<double[]> hiddenOutput1;
    private double[] hiddenOutput2;
    
    // momentum
    private final double mu = 0.5;
    private final double theta = 0.7;
    // delta-bar-delta
    private final double kappa = 0.1;
    private final double phi = 0.5;
    
    private final byte ReLU_MODE = 0;
    private final byte TanH_MODE = 1;
    
    // learning rate
    private double alpha;
    private double[][] globalEta;
    private double[][] pieceEta;
    private double[][] positionEta;
    private double[][] hiddenLayerEta;
    private double[][] outputEta;
    
    // history direction
    private double[][] globalDir;
    private double[][] pieceDir;
    private double[][] positionDir;
    private double[][] hiddenLayerDir;
    private double[][] outputDir;
    
    // last error gradient
    private double[][] globalGrad;
    private double[][] pieceGrad;
    private double[][] positionGrad;
    private double[][] hiddenLayerGrad;
    private double[][] outputGrad;
    
    // estimated values functions from step 1 to m (end)
    private double[] value;
    private int episodeWindow = 10;
    
    // gradient by eipsode
    private double[][][] globalGrad_e;
    private double[][][] pieceGrad_e;
    private double[][][] positionGrad_e;
    private double[][][] hiddenLayerGrad_e;
    private double[][][] outputGrad_e;
    
    boolean exportParameters(String file) {
        try {
            FileWriter writer = FileHandler.write(file, false);
            if (writer != null) {
                exportWeights(writer, this.globalWeights, "global");
                exportWeights(writer, this.pieceWeights, "piece");
                exportWeights(writer, this.positionWeights, "position");
                exportWeights(writer, this.hiddenLayerWeights, "hidden");
                exportWeights(writer, this.outputWeights, "output");
                writer.close();
                return true;
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        return false;
    }
    
    private void exportWeights(FileWriter writer, double[][] weight, String which) throws IOException {
        writer.write(which + " " + weight.length + " " + weight[0].length + "\n");
        for (int i = 0; i < weight.length; i++) {
            for (int j = 0; j < weight[0].length; j++) {
                writer.write(weight[i][j] + " ");
            }
            writer.write("\n");
        }
    }
    
    /*
     * initialize the weights of NN
     */
    public boolean init(String file) {
        // read parameters from a saved file
        BufferedReader br = FileHandler.read(file);
        if (br != null) {
            String strLine;
            try {
                int i = 0;
                String which = "global"; 
                while ((strLine = br.readLine()) != null) {
                    String[] a = strLine.trim().split(" ");
                   if (a[0].equals("global")) {
                       int row = Integer.parseInt(a[1]);
                       int col = Integer.parseInt(a[2]);
                       this.globalWeights = new double[row][col];
                       i = 0;
                   } else if (a[0].equals("piece")) {
                       int row = Integer.parseInt(a[1]);
                       int col = Integer.parseInt(a[2]);
                       this.pieceWeights = new double[row][col];
                       i = 0;
                       which = "piece";
                   } else if (a[0].equals("position")) {
                       int row = Integer.parseInt(a[1]);
                       int col = Integer.parseInt(a[2]);
                       this.positionWeights = new double[row][col];
                       i = 0;
                       which = "position";
                   } else if (a[0].equals("hidden")) {
                       int row = Integer.parseInt(a[1]);
                       int col = Integer.parseInt(a[2]);
                       this.hiddenLayerWeights = new double[row][col];
                       i = 0;
                       which = "hidden";
                   } else if (a[0].equals("output")) {
                       int row = Integer.parseInt(a[1]);
                       int col = Integer.parseInt(a[2]);
                       this.outputWeights = new double[row][col];
                       i = 0;
                       which = "output";
                   } else {
                       if (which.equals("global")) {
                           int col = this.globalWeights[0].length;
                           for (int j = 0; j < col; j++) {
                               this.globalWeights[i][j] = Double.parseDouble(a[j]);
                           }
                           i++;
                       } else if (which.equals("piece")) {
                           int col = this.pieceWeights[0].length;
                           for (int j = 0; j < col; j++) {
                               this.pieceWeights[i][j] = Double.parseDouble(a[j]);
                           }
                           i++;
                       } else if (which.equals("position")) {
                           int col = this.positionWeights[0].length;
                           for (int j = 0; j < col; j++) {
                               this.positionWeights[i][j] = Double.parseDouble(a[j]);
                           }
                           i++;
                       } else if (which.equals("hidden")) {
                           int col = this.hiddenLayerWeights[0].length;
                           for (int j = 0; j < col; j++) {
                               this.hiddenLayerWeights[i][j] = Double.parseDouble(a[j]);
                           }
                           i++;
                       } else if (which.equals("output")) {
                           int col = this.outputWeights[0].length;
                           for (int j = 0; j < col; j++) {
                               this.outputWeights[i][j] = Double.parseDouble(a[j]);
                           }
                           i++;
                       }
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
            
            // init size
            this.numGlobalInput = this.globalWeights.length - 1;
            this.numPieceInput = this.pieceWeights.length - 1;
            this.numPositionInput = this.positionWeights.length - 1;
            this.numHidden11 = this.globalWeights[0].length;
            this.numHidden12 = this.pieceWeights[0].length;
            this.numHidden13 = this.positionWeights[0].length;
            this.numHidden1 = this.numHidden11 + this.numHidden12 + this.numHidden13;
            this.numHidden2 = this.outputWeights.length - 1;
            this.numOutput = 1;
            
            // init other parameters
            this.initOptParameters();
            return true;
        }
        return false;
    }
    
    public void init(List<List<Double>> features) {
        int[] n1 = {this.numHidden11, this.numHidden12, this.numHidden13};
        int n2 = this.numHidden2;
        this.init(features, n1, n2);
    }
    
    public void init(List<List<Double>> features, int[] numHidden1, int numHidden2) {
        this.numGlobalInput = features.get(0).size();
        this.numPieceInput = features.get(1).size();
        this.numPositionInput = features.get(2).size();
        this.numHidden11 = numHidden1[0];
        this.numHidden12 = numHidden1[1];
        this.numHidden13 = numHidden1[2];
        this.numHidden1 = this.numHidden11 + this.numHidden12 + this.numHidden13;
        this.numHidden2 = numHidden2;
        this.numOutput = 1;
        
        this.globalWeights = new double[this.numGlobalInput + 1][this.numHidden11];
        this.pieceWeights = new double[this.numPieceInput + 1][this.numHidden12];
        this.positionWeights = new double[this.numPositionInput + 1][this.numHidden13];
        this.hiddenLayerWeights = new double[this.numHidden1 + 1][this.numHidden2];
        this.outputWeights = new double[this.numHidden2 + 1][this.numOutput];
        
        initWeights(this.globalWeights);
        initWeights(this.pieceWeights);
        initWeights(this.positionWeights);
        initWeights(this.hiddenLayerWeights);
        initWeights(this.outputWeights);
        
        this.initOptParameters();
    }
    
    /*
     * initialize other helping parameters in optimization procedure
     */
    private void initOptParameters() {
        this.hiddenOutput1 = new ArrayList<double[]>();
        this.hiddenOutput2 = new double[this.numHidden2];
        
        this.globalDelta = new double[this.numGlobalInput + 1][this.numHidden11];
        this.pieceDelta = new double[this.numPieceInput + 1][this.numHidden12];
        this.positionDelta = new double[this.numPositionInput + 1][this.numHidden13];
        this.hiddenLayerDelta = new double[this.numHidden1 + 1][this.numHidden2];
        this.outputDelta = new double[this.numHidden2 + 1][this.numOutput];
        
        this.globalEta = new double[this.numGlobalInput + 1][this.numHidden11];
        this.pieceEta = new double[this.numPieceInput + 1][this.numHidden12];
        this.positionEta = new double[this.numPositionInput + 1][this.numHidden13];
        this.hiddenLayerEta = new double[this.numHidden1 + 1][this.numHidden2];
        this.outputEta = new double[this.numHidden2 + 1][this.numOutput];
        
        initLearningRate(this.globalEta);
        initLearningRate(this.pieceEta);
        initLearningRate(this.positionEta);
        initLearningRate(this.hiddenLayerEta);
        initLearningRate(this.outputEta);
        
        this.globalDir = new double[this.numGlobalInput + 1][this.numHidden11];
        this.pieceDir = new double[this.numPieceInput + 1][this.numHidden12];
        this.positionDir = new double[this.numPositionInput + 1][this.numHidden13];
        this.hiddenLayerDir = new double[this.numHidden1 + 1][this.numHidden2];
        this.outputDir = new double[this.numHidden2 + 1][this.numOutput];
        
        this.globalGrad = new double[this.numGlobalInput + 1][this.numHidden11];
        this.pieceGrad = new double[this.numPieceInput + 1][this.numHidden12];
        this.positionGrad = new double[this.numPositionInput + 1][this.numHidden13];
        this.hiddenLayerGrad = new double[this.numHidden1 + 1][this.numHidden2];
        this.outputGrad = new double[this.numHidden2 + 1][this.numOutput];
    }
    
    /*
     * initialize the learning rate
     */
    private void initLearningRate(double[][] rate) {
        for (int i = 0; i < rate.length; i++) {
            for (int j = 0; j < rate[0].length; j++) {
                rate[i][j] = 0.1;
            }
        }
    }
    
    /*
     * initialize weight in the range [-1, 1]
     */
    private void initWeights(double[][] weights) {
        for (int i = 0; i < weights.length; i++) {
            for (int j = 0; j < weights[0].length; j++) {
                weights[i][j] = new Random().nextDouble() * 2 - 1;
            }
        }
    }
    
    /*
     * deep copy an array
     */
    private double[] copy(double[] array) {
        int len = array.length;
        double[] dup = new double[len];
        for (int i = 0; i < len; i++)
            dup[i] = array[i];
        return dup;
    }
    
    /*
     * forward propagation
     * 
     * input: 
     * @features is extracted from Feature class with the same format
     */
    public double forward(List<List<Double>> features) {
        // input feature values
        double[] globalF = list2array(features.get(0));
        double[] pieceF = list2array(features.get(1));
        double[] positionF = list2array(features.get(2));
        
        double[] hiddenInput1 = transmit(globalF, this.globalWeights);
        double[] hiddenInput2 = transmit(pieceF, this.pieceWeights);
        double[] hiddenInput3 = transmit(positionF, this.positionWeights);
        
        this.hiddenOutput1.add(this.copy(hiddenInput1));
        this.hiddenOutput1.add(this.copy(hiddenInput2));
        this.hiddenOutput1.add(this.copy(hiddenInput3));
        
        this.activate(hiddenInput1, ReLU_MODE);
        this.activate(hiddenInput2, ReLU_MODE);
        this.activate(hiddenInput3, ReLU_MODE);
        
        // concatenate three hidden inputs
        int n = hiddenInput1.length + hiddenInput2.length + hiddenInput3.length;
        double[] hidden1 = new double[n+1]; // add bias
        int index = 0;
        hidden1[index++] = 1;
        for (double d : hiddenInput1)
            hidden1[index++] = d;
        for (double d : hiddenInput2)
            hidden1[index++] = d;
        for (double d : hiddenInput3)
            hidden1[index++] = d;
            
        // pass from hidden layer 1 to hidden layer 2
        this.hiddenOutput2 = transmit(hidden1, this.hiddenLayerWeights);
        double[] hidden2 = this.addBiasValue(this.copy(this.hiddenOutput2));
        this.activate(hidden2, ReLU_MODE);
        
        // transmit to output layer
        double[] outputs = transmit(hidden2, this.outputWeights);
        this.activate(outputs, TanH_MODE);
        
        /*for (double[] list : this.hiddenOutput1) {
            System.out.println("===================");
            for (double d : list) System.out.print(d + "\t");
            System.out.println();
        }
        
        System.out.println("2================");
        for (double d : this.hiddenOutput2) {
            System.out.print(d + "\t");
        }
        System.out.println();*/
        
        return outputs[0];
    }
    
    /*
     * feature list to feature array with bias value added
     */
    private double[] list2array(List<Double> list) {
        int size = list.size();
        double[] array = new double[size + 1];
        array[0] = 1; // this corresponds to bias
        for (int i = 0; i < size; i++) {
            array[i+1] = list.get(i);
        }
        return array;
    }
    
    /*
     * transmit values in the current layer to the next layer
     */
    private double[]  transmit(double[] values, double[][] weights) {
        int n = weights.length;
        int m = weights[0].length;
        double[] nextValues = new double[m];
        for (int i = 0; i < m; i++) {
            for (int j = 0; j < n; j++) {
                nextValues[i] += values[j] * weights[j][i];
            }
        }
        return nextValues;
    }
    
    private void activate(double[] values, byte mode) {
        int len = values.length;
        if (mode == this.ReLU_MODE) {
            for (int i = 0; i < len; i++) {
                values[i] = this.ReLU(values[i]);
            }
        } else {
            for (int i = 0; i < len; i++) {
                values[i] = this.tanh(values[i]);
            }
        }
    }
    
    private double[] addBiasValue(double[] array) {
        int len = array.length;
        double[] augArray = new double[len+1];
        augArray[0] = 1;
        for (int i = 0; i < len; i++)
            augArray[i+1] = array[i];
        return augArray;
    }
    
    /*
     * train the whole procedure using TD
     * use the @result as true label to back propagate, and assume only consider
     * the successive 10 moves, not all the moves.
     * 
     * dw_tij = alpha * (V_t+1-V_t) \sum_k=1^t lambda^t-k d_ij V_k
     * 
     * parameters:
     * @result is the final result of the chess
     * @record is the whole moves
     */
    public void backpropagation(int result, String record) {
        int n = record.length() / 4;
        this.value = new double[n];
        
        // w[t][i][j]
        this.globalGrad_e = new double[n][this.numGlobalInput + 1][this.numHidden11];
        this.pieceGrad_e = new double[n][this.numPieceInput + 1][this.numHidden12];
        this.positionGrad_e = new double[n][this.numPositionInput + 1][this.numHidden13];
        this.hiddenLayerGrad_e = new double[n][this.numHidden1 + 1][this.numHidden2];
        this.outputGrad_e = new double[n][this.numHidden2 + 1][this.numOutput];
      
        Game game = new Game();
        game.initBoard();
        // first move
        String move = record.substring(0, 4);
        BoardPosition from = new BoardPosition(
                Integer.parseInt(move.substring(1, 2)), Integer.parseInt(move.substring(0, 1)));
        BoardPosition to = new BoardPosition(
                Integer.parseInt(move.substring(3, 4)), Integer.parseInt(move.substring(2, 3)));
        game.movePiece(from, to);
        List<List<Double>> feat = Feature.featureExtractor(game);
        this.value[0] = this.forward(feat);
        game.changeTurn();
        
        // lambda array, I don't want to call pow function each iteration
        double[] lambda = new double[this.episodeWindow];
        lambda[0] = 1;
        for (int i = 1; i < lambda.length; i++)
            lambda[i] = lambda[i-1] * TD.lambda;
        
        // calculate error in each step
        for (int t = 0; t < n; t++) {
            double tdiff = 0;
            List<List<Double>> nextFeat = null;
            
            if (t < n -1) {
                move = record.substring((t+1) * 4, (t+2) * 4);
                from = new BoardPosition(
                        Integer.parseInt(move.substring(1, 2)), Integer.parseInt(move.substring(0, 1)));
                to = new BoardPosition(
                        Integer.parseInt(move.substring(3, 4)), Integer.parseInt(move.substring(2, 3)));
                game.movePiece(from, to);
                nextFeat = Feature.featureExtractor(game);
                this.value[t+1] = this.forward(nextFeat);
                game.changeTurn();
            }
            
            // temporal difference term: V_t+1 - V_t
            if (t < n-2) {
                tdiff = this.value[t+1] - this.value[t];
            } else if (t == n-2) {
                tdiff = -result - this.value[t];
            } else if (t == n-1) {
                tdiff = result - this.value[t];
            }
            
            // calculate gradient V_t w.r.t. w_ij
            double p = this.dtanh(this.value[t]);
            this.outputWeightGrad_e(t, p);
            this.hiddenWeightGrad_e(t, p);
            this.globalWeightGrad_e(t, p, this.list2array(feat.get(0)));
            this.pieceWeightGrad_e(t, p, this.list2array(feat.get(1)));
            this.positionWeightGrad_e(t, p, this.list2array(feat.get(2)));
            
            // update delta weight arrays
            int start = Math.max(0, t-this.episodeWindow+1);
            int end = start + this.episodeWindow - 1;
            this.updateWeight_e(this.outputGrad_e, tdiff, lambda, start, end, this.outputDelta);
            this.updateWeight_e(this.hiddenLayerGrad_e, tdiff, lambda, start, end, this.hiddenLayerDelta);
            this.updateWeight_e(this.globalGrad_e, tdiff, lambda, start, end, this.globalDelta);
            this.updateWeight_e(this.pieceGrad_e, tdiff, lambda, start, end, this.pieceDelta);
            this.updateWeight_e(this.positionGrad_e, tdiff, lambda, start, end, this.positionDelta);
            
            if (t < n-1) {
                feat = nextFeat;
            }
        }
        
        // update weight after summing over all the steps
        this.updateWeight(this.outputWeights, this.outputDelta);
        this.updateWeight(this.hiddenLayerWeights, this.hiddenLayerDelta);
        this.updateWeight(this.globalWeights, this.globalDelta);
        this.updateWeight(this.pieceWeights, this.pieceDelta);
        this.updateWeight(this.positionWeights, this.positionDelta);
    }
    
    /*
     * update weights in episodic sequence
     * 
     * parameters:
     * @grad is the error gradient array of weight
     * @tdiff is the temporal difference V_t+1 - V_t
     * @lambda is the lambda array, indicating the discount factor, TD-lambda
     * @start and @end give the range of the summation over gradients of several steps
     * @delta is the array stores the accumulated error of w_ij
     */
    private void updateWeight_e(double[][][] grad, double tdiff, 
            double[] lambda, int start, int end, double[][] delta) {
        int n = delta.length;
        int m = delta[0].length;
        for (int i = 0; i < n; i++) {
            for (int j = 0; j < m; j++) {
                double sum = 0;
                for (int k = start; k <= end; k++) {
                    sum += lambda[k-start] * grad[k][i][j];
                }
                delta[i][j] += sum * this.alpha * tdiff;
            }
        }
    }
    
    private void outputWeightGrad_e(int t, double factor) {
        int n = this.outputGrad_e[0].length;
        double[] values = this.copy(this.hiddenOutput2);
        this.activate(values, ReLU_MODE);
        
        for (int i = 0; i < n; i++) {
            double d_m = factor;
            if (i != 0) d_m *= values[i-1];
            this.outputGrad_e[t][i][0] = -d_m;
        }
    }
    
    private void hiddenWeightGrad_e(int t, double factor) {
        int n = this.hiddenLayerGrad_e[0].length;
        int m = this.hiddenLayerGrad_e[0][0].length;
        
        double[] global = this.copy(this.hiddenOutput1.get(0));
        int globalLen = global.length;
        this.activate(global, ReLU_MODE);
        double[] piece = this.copy(this.hiddenOutput1.get(1));
        int pieceLen = piece.length;
        this.activate(piece, ReLU_MODE);
        double[] position = this.copy(this.hiddenOutput1.get(2));
        this.activate(position, ReLU_MODE);
        
        for (int j = 0; j < m; j++) {
            double q = factor * this.dReLU(this.hiddenOutput2[j]) * this.outputWeights[j][0];
            for (int i = 0; i < n; i++) {
                double d_m = q;
                if (i == 0) {
                    // bias
                } else if (i <= globalLen) {
                    // global feature part
                    d_m *= global[i-1];
                } else if (i <= pieceLen + globalLen) {
                    // piece feature part
                    d_m *= piece[i - globalLen - 1];
                } else {
                    // position feature part
                    d_m *= position[i - globalLen - pieceLen - 1];
                }
                this.hiddenLayerGrad_e[t][i][j] = -d_m;
            }
        }
    }
    
    private void globalWeightGrad_e(int t, double factor, double[] globalFeatures) {
        int n = this.globalGrad_e[0].length;
        int m = this.globalGrad_e[0][0].length;
        double[] global = this.hiddenOutput1.get(0);
        int hidden2len = this.hiddenOutput2.length;
        
        for (int i = 0; i < m; i++) {
            double q = 0;
            for (int j = 0; j < hidden2len; j++) {
                q += this.outputWeights[j+1][0] * this.dReLU(this.hiddenOutput2[j])
                        * this.hiddenLayerWeights[i+1][j];
            }
            
            double d_m = factor * q * this.dReLU(global[i]);
            for (int k = 0; k < n; k++) {
                if (k != 0) d_m *=  globalFeatures[k-1];
                this.globalGrad_e[t][k][i] = -d_m;
            }
        }
    }
    
    private void pieceWeightGrad_e(int t, double factor, double[] pieceFeatures) {
        int n = this.pieceGrad_e[0].length;
        int m = this.pieceGrad_e[0].length;
        double[] piece = this.hiddenOutput1.get(1);
        int hidden2len = this.hiddenOutput2.length;
        
        for (int i = 0; i < m; i++) {
            double q = 0;
            for (int j = 0; j < hidden2len; j++) {
                q += this.outputWeights[j+1][0] * this.dReLU(this.hiddenOutput2[j])
                        * this.hiddenLayerWeights[i+1][j];
            }
            
            double d_m = factor * q * this.dReLU(piece[i]);
            for (int k = 0; k < n; k++) {
                if (k != 0) d_m *=  pieceFeatures[k-1];
                this.pieceGrad_e[t][k][i] = -d_m;
            }
        }
    }
    
    private void positionWeightGrad_e(int t, double factor, double[] positionFeatures) {
        int n = this.positionGrad_e[0].length;
        int m = this.positionGrad_e[0][0].length;
        double[] position = this.hiddenOutput1.get(2);
        int hidden2len = this.hiddenOutput2.length;
        
        for (int i = 0; i < m; i++) {
            double q = 0;
            for (int j = 0; j < hidden2len; j++) {
                q += this.outputWeights[j+1][0] * this.dReLU(this.hiddenOutput2[j])
                        * this.hiddenLayerWeights[i+1][j];
            }
            
            double d_m = factor * q * this.dReLU(position[i]);
            for (int k = 0; k < n; k++) {
                if (k != 0) d_m *=  positionFeatures[k-1];
                this.positionGrad_e[t][k][i] = -d_m;
            }
        }
    }
    
    /*
     * back propagation
     * 
     * This is supervised version of training
     * input is @features and output is @label which indicates the positive 
     * or negative samples. Because we use tanh activation function whose 
     * range is (-1, 1), the label here should be 1, 0 or  -1.
     * 
     * The loss function is E = (y - label) ^ 2 / 2
     */
    public void backpropagation(List<List<Double>> features, int label) {
        double y = this.forward(features);
        this.backpropagation(features, y, y - label);
    }
    
    /*
     * unsupervised back propagation with error signal generated by TD
     * @error is generated from TD learning
     */
    public void backpropagationWithTD(List<List<Double>> features, double error) {
        double y = this.forward(features);
        this.backpropagation(features, y, error);
    }
    
    /*
     * @features is input feature list of three parts
     * @pred is the predicted output of NN
     * @error is the error between actual and predicted values, or
     * estimated from TD
     */
    private void backpropagation(List<List<Double>> features, double pred, double error) {
        double p = error * this.dtanh(pred);
        
        this.outputWeightGrad(p);
        this.hiddenWeightGrad(p);
        this.globalWeightGrad(p, this.list2array(features.get(0)));
        this.pieceWeightGrad(p, this.list2array(features.get(1)));
        this.positionWeightGrad(p, this.list2array(features.get(2)));

        this.updateWeight(this.outputWeights, this.outputDelta);
        this.updateWeight(this.hiddenLayerWeights, this.hiddenLayerDelta);
        this.updateWeight(this.globalWeights, this.globalDelta);
        this.updateWeight(this.pieceWeights, this.pieceDelta);
        this.updateWeight(this.positionWeights, this.positionDelta);
    }
    
    /*
     * update learning rate of each weight by using Delta-Bar-Delta method
     * 
     * f_m = theta * f_{m-1} + (1-theta) * d_{m-1}
     * eta_m = eta_{m-1} + kappa if d_m * f_m > 0; o.w. eta_{m-1} * phi
     * 
     * parameters
     * @eta: last learning rate matrix
     * @dir: last direction
     * @grad: last error gradient
     * @newGrad: current error gradient
     * @i: from which neuron
     * @j: to which neuron
     */
    private void calLearningRate(double[][] eta, double[][] dir, double[][] grad, double newGrad, int i, int j) {
        // update direction value
        dir[i][j] = this.theta * dir[i][j] + (1-this.theta) * grad[i][j];
        // compare when new direction and new gradient have the same direction
        if (newGrad * dir[i][j] > 0) eta[i][j] += this.kappa;
        else eta[i][j] *= this.phi;
        // update error gradient
        grad[i][j] = newGrad;
    }
    
    /*
     * w = w + delta_w
     */
    private void updateWeight(double[][] weight, double[][] delta) {
        int n = weight.length;
        int m = weight[0].length;
        for (int i = 0; i < n; i++) {
            for (int j = 0; j < m; j++) {
                weight[i][j] += delta[i][j];
            }
        }
    }
    
    private void outputWeightGrad(double factor) {
        int n = this.outputDelta.length;
        double[] values = this.copy(this.hiddenOutput2);
        this.activate(values, ReLU_MODE);
        
        for (int i = 0; i < n; i++) {
            double d_m = factor;
            if (i != 0) d_m *= values[i-1];
            calLearningRate(this.outputEta, this.outputDir, this.outputGrad, d_m, i, 0);
            this.outputDelta[i][0] = this.mu * this.outputDelta[i][0] - (1-this.mu) * this.outputEta[i][0] * d_m;
        }
    }

    private void hiddenWeightGrad(double factor) {
        int n = this.hiddenLayerDelta.length;
        int m = this.hiddenLayerDelta[0].length;
        
        double[] global = this.copy(this.hiddenOutput1.get(0));
        int globalLen = global.length;
        this.activate(global, ReLU_MODE);
        double[] piece = this.copy(this.hiddenOutput1.get(1));
        int pieceLen = piece.length;
        this.activate(piece, ReLU_MODE);
        double[] position = this.copy(this.hiddenOutput1.get(2));
        this.activate(position, ReLU_MODE);
        
        for (int j = 0; j < m; j++) {
            double q = factor * this.dReLU(this.hiddenOutput2[j]) * this.outputWeights[j][0];
            for (int i = 0; i < n; i++) {
                double d_m = q;
                if (i == 0) {
                    // bias
                } else if (i <= globalLen) {
                    // global feature part
                    d_m *= global[i-1];
                } else if (i <= pieceLen + globalLen) {
                    // piece feature part
                    d_m *= piece[i - globalLen - 1];
                } else {
                    // position feature part
                    d_m *= position[i - globalLen - pieceLen - 1];
                }
                calLearningRate(this.hiddenLayerEta, this.hiddenLayerDir, this.hiddenLayerGrad, d_m, i, j);
                this.hiddenLayerDelta[i][j] = this.mu * this.hiddenLayerDelta[i][j]
                        - (1-this.mu) * this.hiddenLayerEta[i][j] * d_m;
            }
        }
    }
    
    private void globalWeightGrad(double factor, double[] globalFeatures) {
        int n = this.globalDelta.length;
        int m = this.globalDelta[0].length;
        double[] global = this.hiddenOutput1.get(0);
        int hidden2len = this.hiddenOutput2.length;
        
        for (int i = 0; i < m; i++) {
            double q = 0;
            for (int j = 0; j < hidden2len; j++) {
                q += this.outputWeights[j+1][0] * this.dReLU(this.hiddenOutput2[j])
                        * this.hiddenLayerWeights[i+1][j];
            }
            
            double d_m = factor * q * this.dReLU(global[i]);
            for (int k = 0; k < n; k++) {
                if (k != 0) d_m *=  globalFeatures[k-1];
                calLearningRate(this.globalEta, this.globalDir, this.globalGrad, d_m, k, i);
                this.globalDelta[k][i] = this.mu * this.globalDelta[k][i]
                        - (1-this.mu) * this.globalEta[k][i] * d_m;
            }
        }
    }
    
    private void pieceWeightGrad(double factor, double[] pieceFeatures) {
        int n = this.pieceDelta.length;
        int m = this.pieceDelta[0].length;
        double[] piece = this.hiddenOutput1.get(1);
        int hidden2len = this.hiddenOutput2.length;
        
        for (int i = 0; i < m; i++) {
            double q = 0;
            for (int j = 0; j < hidden2len; j++) {
                q += this.outputWeights[j+1][0] * this.dReLU(this.hiddenOutput2[j])
                        * this.hiddenLayerWeights[i+1][j];
            }
            
            double d_m = factor * q * this.dReLU(piece[i]);
            for (int k = 0; k < n; k++) {
                if (k != 0) d_m *=  pieceFeatures[k-1];
                calLearningRate(this.pieceEta, this.pieceDir, this.pieceGrad, d_m, k, i);
                this.pieceDelta[k][i] = this.mu * this.pieceDelta[k][i]
                        - (1-this.mu) * this.pieceEta[k][i] * d_m;
            }
        }
    }
    
    private void positionWeightGrad(double factor, double[] positionFeatures) {
        int n = this.positionDelta.length;
        int m = this.positionDelta[0].length;
        double[] position = this.hiddenOutput1.get(2);
        int hidden2len = this.hiddenOutput2.length;
        
        for (int i = 0; i < m; i++) {
            double q = 0;
            for (int j = 0; j < hidden2len; j++) {
                q += this.outputWeights[j+1][0] * this.dReLU(this.hiddenOutput2[j])
                        * this.hiddenLayerWeights[i+1][j];
            }
            
            double d_m = factor * q * this.dReLU(position[i]);
            for (int k = 0; k < n; k++) {
                if (k != 0) d_m *=  positionFeatures[k-1];
                calLearningRate(this.positionEta, this.positionDir, this.positionGrad, d_m, k, i);
                this.positionDelta[k][i] = this.mu * this.positionDelta[k][i]
                        - (1-this.mu) * this.positionEta[k][i] * d_m;
            }
        }
    }
    
    /*
     *  Rectified Linear activation and its derivative
     */
    private double ReLU(double u) {
        return u < 0 ? 0 : u;
    }
    
    private double dReLU(double u) {
        return u < 0 ? 0 : 1;
    }
    
    /*
     * hyperbolic tangent activation and its derivative
     */
    private double tanh(double u) {
        return Math.tanh(u);
    }
    
    private double dtanh(double u) {
        double z = tanh(u);
        return 1 - z * z;
    }
}
