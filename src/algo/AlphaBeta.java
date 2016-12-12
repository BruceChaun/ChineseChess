/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package algo;

import constants.Colors;
import game.BoardPosition;
import game.Game;
import java.util.ArrayList;
import java.util.List;
import javafx.util.Pair;
import pieces.Piece;

/**
 *
 * @author mw352
 */
public class AlphaBeta {

    /*
    set alpha to INT_MIN, beta to INT_MAX, MinMax to "Max" as the initialization. 
    Depth should be an even number
    use bestmove as the result of nextmove in the ArrayList of getNextPossibleGame()
     */
    private int bestmove;
    private Neuroph neuroph;

    public AlphaBeta() {
        this.bestmove = -1;
        this.neuroph = new Neuroph();
    }

    //depth = 2, 4, 6, 8......
    public String getNextMoveIndex(Game game, int depth) {
        this.bestmove = -1;
        Game copy = game.copy();
        AlphaBetaPruning(copy, Double.MIN_VALUE, Double.MAX_VALUE, depth, "Max", depth);
        List<String> moves = game.getAllPiecePossibleMoves();
        return moves.get(this.bestmove);
    }

    public Game getTargetGameState(Game game, int depth) {
        ArrayList<Integer> movelist = new ArrayList();
        Game copy1 = game.copy();
        //get the path of the MaxMin based AlphaBeta pruning tree
        Pair<Double, ArrayList<Integer>> bestmovestate = AlphaBetaPruningForState(copy1, Double.MIN_VALUE, Double.MAX_VALUE, depth, "Max", movelist);
        ArrayList<Integer> bestmovelist = bestmovestate.getValue();
        int num = bestmovelist.size();
        Game copy2 = game.copy();
        //tracking the path, update to the target gamestate
        for (int i = 0; i < num; i++) {
            List<String> moves = copy2.getAllPiecePossibleMoves();
            String move = moves.get(bestmovelist.get(i));
            BoardPosition from = new BoardPosition(
                    Integer.parseInt(move.substring(1, 2)), Integer.parseInt(move.substring(0, 1)));
            BoardPosition to = new BoardPosition(
                    Integer.parseInt(move.substring(3, 4)), Integer.parseInt(move.substring(2, 3)));
            copy2.movePiece(from, to);
        }
        return copy2;
    }
    //The function return the path of the AlphaBetaPruning Algorithm;
    //ArrayList<Integer> movelist is to record the path through the game state tree
    public Pair<Double, ArrayList<Integer>> AlphaBetaPruningForState(Game game, double alpha, double beta, int depth, String MinMax, ArrayList<Integer> movelist) {
        //reach the leaf nodes, use fitness function to evaluate.
        if (depth == 0) {
            List<List<Double>> feat = Feature.featureExtractor(game);
            double[] array = Feature.featureArray(feat);
            double value = neuroph.evaluate(array);
            Pair<Double, ArrayList<Integer>> p = new Pair(value, movelist);
            return p;
        } else //at Max Layer
        if (MinMax == "Max") {
            List<String> moves = game.getAllPiecePossibleMoves();
            int num = moves.size();
            ArrayList<Integer> bestmovelist = new ArrayList();
            for (int i = 0; i < num; i++) {
                Game copy = game.copy();
                // use API in chess game to update game states
                String move = moves.get(i);
                BoardPosition from = new BoardPosition(
                        Integer.parseInt(move.substring(1, 2)), Integer.parseInt(move.substring(0, 1)));
                BoardPosition to = new BoardPosition(
                        Integer.parseInt(move.substring(3, 4)), Integer.parseInt(move.substring(2, 3)));
                copy.movePiece(from, to);
                ArrayList<Integer> newlist = new ArrayList(movelist);
                newlist.add(i);
                Pair<Double, ArrayList<Integer>> result = AlphaBetaPruningForState(copy, alpha, beta, depth - 1, "Min", newlist);
                // when the evaluation is larger than alpha, replace alpha with current value
                if (result.getKey() > alpha) {
                    alpha = result.getKey();
                    if (alpha >= beta) {
                        break;
                    }
                    // substitude with the current best path
                    bestmovelist = result.getValue();

                }
            }
            //use the pair of evaluation with recorded path as return
            Pair<Double, ArrayList<Integer>> p = new Pair(alpha, bestmovelist);
            return p;
        } else {
            //the same as above, change alpha logic to beta logic
            List<String> moves = game.getAllPiecePossibleMoves();
            int num = moves.size();
            ArrayList<Integer> bestmovelist = new ArrayList();
            for (int i = 0; i < num; i++) {
                Game copy = game.copy();
                String move = moves.get(i);
                BoardPosition from = new BoardPosition(
                        Integer.parseInt(move.substring(1, 2)), Integer.parseInt(move.substring(0, 1)));
                BoardPosition to = new BoardPosition(
                        Integer.parseInt(move.substring(3, 4)), Integer.parseInt(move.substring(2, 3)));
                copy.movePiece(from, to);
                ArrayList<Integer> newlist = new ArrayList(movelist);
                newlist.add(i);
                Pair<Double, ArrayList<Integer>> result = AlphaBetaPruningForState(copy, alpha, beta, depth - 1, "Max", newlist);
                if (result.getKey() < beta) {
                    beta = result.getKey();
                    if (alpha >= beta) {
                        break;
                    }
                    bestmovelist = result.getValue();
                }
            }
            Pair<Double, ArrayList<Integer>> p = new Pair(alpha, bestmovelist);
            return p;
        }
    }

    //The function is to set bestmove to the result of the search in gamestate and depth
    public double AlphaBetaPruning(Game game, double alpha, double beta, int depth, String MinMax, int realdepth) {
        //reach the leaf nodes, use fitness function to evaluate.
        if (depth == 0) {
            List<List<Double>> feat = Feature.featureExtractor(game);
            double[] array = Feature.featureArray(feat);
            double value = neuroph.evaluate(array);
            return value;
        } else //at Max Layer
        if (MinMax == "Max") {
            List<String> moves = game.getAllPiecePossibleMoves();
            int num = moves.size();
            for (int i = 0; i < num; i++) {
                Game copy = game.copy();
                String move = moves.get(i);
                BoardPosition from = new BoardPosition(
                        Integer.parseInt(move.substring(1, 2)), Integer.parseInt(move.substring(0, 1)));
                BoardPosition to = new BoardPosition(
                        Integer.parseInt(move.substring(3, 4)), Integer.parseInt(move.substring(2, 3)));
                copy.movePiece(from, to);
                double value = AlphaBetaPruning(copy, alpha, beta, depth - 1, "Min", realdepth);
                if (value > alpha) {
                    alpha = value;
                    if (alpha >= beta) {
                        break;
                    }
                    //record the current bestmove index
                    if (depth == realdepth) {
                        this.bestmove = i;
                    }
                }
            }
            return alpha;
        } else {
            List<String> moves = game.getAllPiecePossibleMoves();
            int num = moves.size();
            for (int i = 0; i < num; i++) {
                Game copy = game.copy();
                String move = moves.get(i);
                BoardPosition from = new BoardPosition(
                        Integer.parseInt(move.substring(1, 2)), Integer.parseInt(move.substring(0, 1)));
                BoardPosition to = new BoardPosition(
                        Integer.parseInt(move.substring(3, 4)), Integer.parseInt(move.substring(2, 3)));
                copy.movePiece(from, to);
                double value = AlphaBetaPruning(copy, alpha, beta, depth - 1, "Max", realdepth);
                if (value < beta) {
                    beta = value;
                    if (alpha >= beta) {
                        break;
                    }
                    if (depth == realdepth) {
                        this.bestmove = i;
                    }
                }
            }
            return beta;
        }
    }
}
