package algo;

import java.util.*;

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
    
    /*
     * initialize the weights of NN
     */
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
        
        this.hiddenOutput1 = new ArrayList<double[]>();
        this.hiddenOutput2 = new double[this.numHidden2];
        
        this.globalWeights = new double[this.numGlobalInput + 1][this.numHidden11];
        this.pieceWeights = new double[this.numPieceInput + 1][this.numHidden12];
        this.positionWeights = new double[this.numPositionInput + 1][this.numHidden13];
        this.hiddenLayerWeights = new double[this.numHidden1 + 1][this.numHidden2];
        this.outputWeights = new double[this.numHidden2 + 1][this.numOutput];
        
        this.globalDelta = new double[this.numGlobalInput + 1][this.numHidden11];
        this.pieceDelta = new double[this.numPieceInput + 1][this.numHidden12];
        this.positionDelta = new double[this.numPositionInput + 1][this.numHidden13];
        this.hiddenLayerDelta = new double[this.numHidden1 + 1][this.numHidden2];
        this.outputDelta = new double[this.numHidden2 + 1][this.numOutput];
        
        initWeights(this.globalWeights);
        initWeights(this.pieceWeights);
        initWeights(this.positionWeights);
        initWeights(this.hiddenLayerWeights);
        initWeights(this.outputWeights);
        
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
        System.out.println("weights");
        for (int i = 0; i < weights.length; i++) {
            for (int j = 0; j < weights[0].length; j++) {
                weights[i][j] = new Random().nextDouble() * 2 - 1;
                System.out.print(weights[i][j] + "\t");
            }
            System.out.println();
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
        
        for (double[] list : this.hiddenOutput1) {
            System.out.println("===================");
            for (double d : list) System.out.print(d + "\t");
            System.out.println();
        }
        
        System.out.println("2================");
        for (double d : this.hiddenOutput2) {
            System.out.print(d + "\t");
        }
        System.out.println();
        
        return outputs[0];
    }
    
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
     * back propagation
     * 
     * This is supervised version of training
     * input is @features and output is @label which indicates the positive 
     * or negative samples. Because we use tanh activation function whose 
     * range is (-1, 1), the label here should be 1 or -1.
     * 
     * The loss function is E = (y - label) ^ 2 / 2
     */
    public void backpropagation(List<List<Double>> features, int label) {
        double y = this.forward(features);
        double p = (y - label) * this.dtanh(y);
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