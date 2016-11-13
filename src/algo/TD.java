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
    private final double gamma = 0.7; // discounted factor
    private final double lambda = 0.7; // TD(lambda)
    private final double alpha = 0.2; // learning rate
    
    public TD(int numStepToSim) {
        this.numStepToSim = numStepToSim;
    }
    
    public TD() {
        this(6);
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
        
        String move = chooseOptMove(game, nn);
        double error = 0;
        return error;
    }

    /*
     * choose the optimal move from all possible moves.
     * The returned string has the same format as the record
     */
    private String chooseOptMove(Game game, NN nn) {
        String optMove = "";
        double bestScore = -1;
        List<int[]> allMoves = getAllPiecePossibleMoves(game);
        
        // naive implementation: TD(0)
        for (int[] a : allMoves) {
            Game copy = game.copy();
            double val = copy.movePiece(new BoardPosition(a[0], a[1]), new BoardPosition(a[2], a[3]));
            val += nn.forward(Feature.featureExtractor(copy)) * this.gamma;
            if (bestScore < val) {
                optMove = a[1] + "" + a[0] + "" + a[3] + "" + a[2];
                bestScore = val;
            }
        }
        
        return optMove;
    }
    
    /*
     * check piece one by one and store in a list of int array, 
     * int array has size 4, each represent the row/column of 
     * one position
     */
    private List<int[]> getAllPiecePossibleMoves(Game game) {
        Colors side = game.getTurn();
        List<int[]> allMoves = new ArrayList<int[]>();
        
        for (int i = 0; i < Game.ROW; i++) {
            for (int j = 0; j < Game.COLUMN; j++) {
                Piece p = game.getPiece(i, j);
                if (p != null && p.getColor().equals(side)) {
                    List<BoardPosition> pieceMoves = p.getLegalMoves(game.getPieces(), new BoardPosition(i, j));
                    for (BoardPosition bp : pieceMoves) {
                        allMoves.add(new int[]{i, j, bp.Row(), bp.Col()});
                    }
                }
            }
        }
        return allMoves;
    }
}
