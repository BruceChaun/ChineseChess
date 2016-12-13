/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package algo;

import game.BoardPosition;
import game.Game;
import java.util.List;

/**
 *
 * @author mw352
 */
public class MinMax {
    private int bestmove;
    private Neuroph neuroph;
    public MinMax() {
        bestmove = -1;
        neuroph = new Neuroph();
    }
    
    public String getNextMoveIndex(Game game, int depth) {
        this.bestmove = -1;
        Game copy = game.copy();
        MinMax(copy, Double.MIN_VALUE, Double.MAX_VALUE, depth, "Max", depth);
        List<String> moves = game.getAllPiecePossibleMoves();
        return moves.get(this.bestmove);
    }
    
    private double MinMax(Game game, double max, double min, int depth, String MinMax, int realdepth) {
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
                double value = MinMax(copy, Double.MIN_VALUE, Double.MAX_VALUE, depth - 1, "Min", realdepth);
                if (value > max) {
                    max = value;
                    //record the current bestmove index
                    if (depth == realdepth) {
                        this.bestmove = i;
                    }
                }
            }
            return max;
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
                double value = MinMax(copy, Double.MIN_VALUE, Double.MAX_VALUE, depth - 1, "Max", realdepth);
                if (value < min) {
                    min = value;
                    if (depth == realdepth) {
                        this.bestmove = i;
                    }
                }
            }
            return min;
        }
    }
}
