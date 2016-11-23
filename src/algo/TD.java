package algo;

import game.*;
import pieces.Piece;

import java.util.*;

import constants.Colors;

/*
 * Use Temporal-Difference learning to generate error signal
 * 
 * This is a kind of self-learning mechanism so that the algorithm 
 * can learn and play Chinese chess itself.
 */
public class TD {
    private int numStepToSim;
    
    // constant parameters
    private final double Z = 200.0; // normalized reward factor
    public static final double gamma = 0.7; // discounted factor
    public static final double lambda = 0.7; // TD(lambda)
    private final double alpha = 0.2; // learning rate
    
    public TD(int numStepToSim) {
        this.numStepToSim = numStepToSim;
    }
    
    public TD() {
        this(10);
    }
    
    /*
     * TDLearn() uses TD lambda method to generate accumulated error over 
     * n steps (moves) with discounted factor.
     * 
     * We assume:
     *     reward is given by the value returned by game.movePiece()
     *     the move in the @record is the optimal move
     *     
     * @nn is used to calculate the (fitness) value using current parameters
     * @record is the chess record to be learned
     */
    public void TDLearn(NN nn, String record) {
        int numMoves = record.length() / 4;
        Game game = new Game();
        game.initBoard();
        
        double lambdaReturn = 0, lambda = 1;
        List<List<Double>> features = Feature.featureExtractor(game);
        double curVal = nn.forward(features);
        for (int i = 0; i < numMoves; i++) {
            int len = Math.min(numMoves - i, this.numStepToSim) * 4;
            int start = i * 4;
            double tStepVal = nStepReturn(game, nn, record.substring(start, start + len));
            
            // take actual move to next game state
            BoardPosition from = new BoardPosition(
                    Integer.parseInt(record.substring(start+1, start+2)), Integer.parseInt(record.substring(start, start+1)));
            BoardPosition to = new BoardPosition(
                    Integer.parseInt(record.substring(start+3, start+4)), Integer.parseInt(record.substring(start+2, start+3)));
            game.movePiece(from, to);
            
            lambdaReturn += tStepVal * lambda;
            lambda *= this.lambda;
        }
        
        // calculate error signal and back propagate in NN
        lambdaReturn *= 1 - this.lambda;
        double error = (lambdaReturn - curVal) * this.alpha;
        nn.backpropagationWithTD(features, error);
    }
    
    /*
     * Calculate n-step return
     * 
     * nStepReturn = sum r_i * gamma^{i-1} + gamma ^ n * V(S_{t+n})
     */
    private double nStepReturn(Game game, NN nn, String record) {
        int steps = record.length() / 4;
        Game copy = game.copy();
        double tStepVal = 0, gamma = 1;
        boolean win = false;
        int t;
        for (t = 0; t < steps; t++) {
            int start = t * 4;
            BoardPosition from = new BoardPosition(
                    Integer.parseInt(record.substring(start+1, start+2)), Integer.parseInt(record.substring(start, start+1)));
            BoardPosition to = new BoardPosition(
                    Integer.parseInt(record.substring(start+3, start+4)), Integer.parseInt(record.substring(start+2, start+3)));
            double reward = copy.movePiece(from, to) / this.Z;
            win = copy.getWinner() != null;
            if (win) reward = 1;
            tStepVal += reward * gamma;
            if (win) break;
            gamma *= this.gamma;
        }
        if (!win)
            tStepVal += nn.forward(Feature.featureExtractor(copy)) * gamma;
        
        return tStepVal;
    }
    
    /*
     * TDSelfLearn() plays chess itself given the neural network and current 
     * game state
     */
    public double TDSelfLearn(NN nn, Game game) {
        double[] values  = new double[this.numStepToSim]; // store value function
        
        String move = game.chooseOptMove(nn);
        double error = 0;
        return error;
    }
    
    /*
     * Use TD to generate error signal and call NN backpropagation method to train the model.
     */
    public void TDtrain(NN nn, String record, int result) {
        int n = record.length() / 4;
        if (n % 2 == 0)
            result = -result;
        
        // TD lambda discount factor for calculation convenience
        double[] lambda = new double[this.numStepToSim];
        lambda[0] = 1;
        for (int i = 1; i < lambda.length; i++)
            lambda[i] = lambda[i-1] * this.lambda;
        
        List<List<List<Double>>> featureList = this.featureList(record);
        List<List<Double>> features;
        
        for (int i = 0; i < n; i++)
            System.out.println(nn.forward(featureList.get(i)));
        
        // V_n is the actual value -- result, calculate it first
        features = featureList.get(n-1);
        double pred = nn.forward(features);
        nn.backpropagationWithTD(features, result - pred);
        nn.converge();
        
        // the rest of n-1 steps
        for (int t = n-2; t >= 0; t--) {
            double error = this.errorSignal(nn, featureList, t, result, lambda);
            features = featureList.get(t);
            nn.backpropagationWithTD(features, error);
        }
        
        System.out.println();
        for (int i = 0; i < n; i++)
            System.out.println(nn.forward(featureList.get(i)));
    }
    
    /*
     * error signal \sum_{k=t}^n lambda^{k-t} (V_{k+1} - V_k)
     * 
     * parameters:
     * @nn is the network to compute estimated value function
     * @featureList is a list of feature vectors of all the chess game positions
     * @t indicates which step it is
     * @result is the actual result at the end of the game
     */
    private double errorSignal(NN nn, List<List<List<Double>>> featureList, int t, int result, double[] lambda) {
        double error = 0;
        int n = featureList.size();
        int end = Math.min(n, t+this.numStepToSim);
        for (int k = t; k < end; k++) {
            double nextVal = result;
            if (k != n-1) nextVal = nn.forward(featureList.get(k+1));
            double currentVal = nn.forward(featureList.get(k));
            error += (nextVal - currentVal) * lambda[k-t];
        }

        return error;
    }
    
    /*
     * calculate feature vectors from step 1 to n
     * 
     * parameters:
     * @record is the chess record
     * 
     * @return a list of feature vectors with the same format in Feature file
     */
    private List<List<List<Double>>> featureList(String record) {
        int n = record.length() / 4;
        
        Game game = new Game();
        game.initBoard();
        List<List<List<Double>>> featureList = new ArrayList<List<List<Double>>>();
        
        for (int t = 0; t < n; t++) {
            String move = record.substring(t*4, t*4+4);
            BoardPosition from = new BoardPosition(
                    Integer.parseInt(move.substring(1, 2)), Integer.parseInt(move.substring(0, 1)));
            BoardPosition to = new BoardPosition(
                    Integer.parseInt(move.substring(3, 4)), Integer.parseInt(move.substring(2, 3)));
            game.movePiece(from, to);
            
            game.changeTurn();
            List<List<Double>> feat = Feature.featureExtractor(game);
            featureList.add(feat);
            game.changeTurn();
        }
        
        return featureList;
    }
}
