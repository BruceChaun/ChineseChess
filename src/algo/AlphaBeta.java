/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package algo;

import constants.Colors;
import game.Game;
import java.util.ArrayList;
import pieces.Piece;

/**
 *
 * @author mw352
 */
public class AlphaBeta {
    private int naive_fitness(Game game) {
        Piece pieces[][] = game.getPieces();
        int score = 0;
        for (int i = 0; i < pieces.length; i++){
            for (int j = 0; j < pieces[0].length; j++) {
                if (pieces[i][j].getColor() == Colors.RED) {
                    score += 1;
                } else if (pieces[i][j].getColor() == Colors.BLACK) {
                    score -= 1;
                }
            }
        }
        return score;
    }
    /*
    This is seudocode for the AlphaBeta prruning algorithm
    alphaBeta(node,alpha,beta,depth,player)  
    if (depth = 0)  
        return valuation(player)调用估值函数  
    else   
        if （player = maxplayer）  
            foreach child of node  
                value :=alphaBeta(child,depth-1,alpha,beta,minplayer)  
                if(value>alpha)alpha:=value  
                if(alpha>=beta)break  
            return alpha  
        else  
            foreach child of node  
                value :=alphaBeta(child,depth-1,alpha,beta,maxplayer)  
                if(value<beta)beta:=value  
                if(alpha>=beta)break  
            return beta   
*/
    
    /*
    set alpha to INT_MIN, beta to INT_MAX, MinMax to "Max" as the initialization. 
    Depth should be an even number
    use bestmove as the result of nextmove in the ArrayList of getNextPossibleGame()
    */
    private int bestmove = -1;
    private ArrayList<Game> naive_getNextPossibleGame(Game game) {
        ArrayList<Game> games = new ArrayList<Game>();
        games.add(game);
        return games;
    }
    public int AlphaBeta(Game game, int alpha, int beta,int depth, String MinMax, int realdepth) {
        //reach the leaf nodes, use fitness function to evaluate.
        if (depth == 0) {
            return naive_fitness(game);
        } else {
            //at Max Layer
            if (MinMax == "Max") {
                ArrayList<Game> nextgames = naive_getNextPossibleGame(game);
                int num = nextgames.size();
                for (int i = 0; i < num; i++) {
                    int value = AlphaBeta(nextgames.get(i), alpha, beta, depth - 1, "Min", realdepth);
                    if (value > alpha) {
                        alpha = value;
                        if (depth == realdepth) {
                            this.bestmove = i;
                        }
                        if (alpha >= beta)
                            break;
                    }
                }
                return alpha;
            } else if (MinMax == "Min") {
                ArrayList<Game> nextgames = naive_getNextPossibleGame(game);
                int num = nextgames.size();
                for (int i = 0; i < num; i++) {
                    int value = AlphaBeta(nextgames.get(i), alpha, beta, depth - 1, "Max", realdepth);
                    if (value < beta) {
                        beta = value;
                        if (depth == realdepth) {
                            this.bestmove = i;
                        }
                        if (alpha >= beta)
                            break;
                    } 
                }
                return beta;
            }
        }
        return -1;
    }
}
