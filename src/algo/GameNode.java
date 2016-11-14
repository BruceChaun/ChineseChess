package algo;

import game.Game;
import java.util.*;

/*
 * GameNode class used in search tree
 */
class GameNode {
    Game game; // game information
    Map<String, GameNode> children; // children, an action-to-state mapping
    GameNode parent; // pointer to its parent
    int numVisit; // number of times being visited
    int simScore; // accumulated score simulated to the end of the game
    double NNScore; // fitness score evaluated by NN
    
    GameNode(Game game, GameNode parent) {
        this.game = game;
        this.parent = parent;
        this.children = new HashMap<String, GameNode>();
        this.numVisit = 0;
        this.simScore = 0;
    }
    
    GameNode(Game game) {
        this(game, null);
    }
    
    /*
     * whether @this game node is terminated 
     */
    boolean isTerminal() {
        return this.game.getWinner() != null;
    }
    
    void setNNScore(NN nn) {
        this.NNScore = nn.forward(Feature.featureExtractor(this.game));
    }
    
    /*
     * UCT = MCTS + UCB
     * MCTS is the fitness score given the NN, 
     * UCB is the sum of estimated value of @this game node and another term 
     * indicating exploration, the first term is called exploitation
     */
    double UCT() {
        double uct = this.NNScore;
        if (this.numVisit > 0 && this.simScore > 0)
            uct += 1.0 * this.simScore / this.numVisit;
        if (this.parent != null) {
            uct += Math.sqrt(2 * Math.log(this.parent.numVisit) / this.numVisit);
        }
        return uct;
    }
}
