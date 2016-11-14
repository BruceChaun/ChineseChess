package algo;

import java.util.*;

import game.*;

public class MCTS {
    private GameNode root;
    private NN nn;
    
    public MCTS(NN nn) {
        this.nn = nn;
        this.root = null;
    }
    
    public GameNode getRoot() {
        return root;
    }
    
    public void setRoot(Game game) {
        root = new GameNode(game);
        root.setNNScore(nn);
    }
    
    /*
     * encapsulation of MCTS
     */
    public void search() {
        GameNode node = selection();
        System.out.println(node);
        GameNode leaf = expansion(node);
        System.out.println("expanded");
        int result = simulation(node);
        System.out.println(result);
        backpropagation(leaf, result);
        System.out.println("back proped");
    }
    
    public void printAllNodes(GameNode node) {
        if (node != null) {
            System.out.println(node.game.getTurn() + "\t" + node + "\t" +node.parent + "\t" + node.simScore + "/" + node.numVisit + "\t" + node.UCT() + "\tnum children\t" + node.children.size());
            for (String key : node.children.keySet()) {
                System.out.println(key);
                printAllNodes(node.children.get(key));
            }
        }
    }
    
    /*
     * selection from root, recursively select the optimal child
     */
    public GameNode selection() {
        return selection(root);
    }
    
    /*
     * select from GameNode @node
     */
    private GameNode selection(GameNode node) {
        double randRate = 0.2;
        if (new Random().nextDouble() < randRate)
            return node;
        
        String action = "";
        GameNode bestChild = null;
        double highestVal = Double.MIN_VALUE;
        Map<String, GameNode> map = node.children;
        
        for (String key : map.keySet()) {
            GameNode child = map.get(key);
            double uct = child.UCT();
            if (uct > highestVal) {
                highestVal = uct;
                action = key;
                bestChild = child;
            }
        }
        
        if (bestChild != null && !bestChild.isTerminal())
            return selection(bestChild);
        return node;
    }
    
    public GameNode addNodeAt(GameNode node) {
        String move = node.game.chooseRandMove();
        System.out.println(move);
        int n = Integer.parseInt(move);
        Game newGame = node.game.copy();
        newGame.movePiece(new BoardPosition(n/100%10, n/1000), new BoardPosition(n%10, n/10%10));
        GameNode child = new GameNode(newGame, node);
        node.children.put(move, child);
        child.setNNScore(nn);
        return child;
    }
    
    public GameNode expansion(GameNode node) {
//        if (node.isTerminal()) return null;
        
        // create one new node
        return addNodeAt(node);
    }
    
    /*
     * This is a toy sample. 
     * Change to alpha-beta pruning later
     */
    public int simulation(GameNode node) {
        // play 200 steps for example
        Game g = node.game.copy();
        final double randRate = 1; // purely random
        for (int i = 0; i < 200; i++) {
            String move = "";
            if (new Random().nextDouble() < randRate) 
                move = g.chooseRandMove();
            else 
                move = g.chooseOptMove(nn);
            int n = Integer.parseInt(move);
            g.movePiece(new BoardPosition(n/100%10, n/1000), 
                    new BoardPosition(n%10, n/10%10));
            
            if (g.getWinner() != null) {
                break;
            }
            g.changeTurn();
        }
        
        int result = -1;
        if (g.getWinner() == null) {
            result = 0;
        } else if (g.getWinner().equals(node.game.getTurn())) {
            result = 1;
        }
        
        return result;
    }
    
    public void backpropagation(GameNode leaf, int result) {
        GameNode node = leaf;
        while (node != null) {
            node.numVisit++;
            node.simScore += result;
            node = node.parent;
        }
    }
}
