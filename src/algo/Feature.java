package algo;

import java.util.ArrayList;
import java.util.List;

import constants.Colors;
import constants.PieceName;
import game.BoardPosition;
import game.Game;
import pieces.*;

/*
 * Feature extraction from the chess board
 * separated into 3 categories and corresponding sub-categories,
 * number in the parenthesis means how many features in all
 * 
 * 1. Global
 * - side to move (1)
 * - number of each type of pieces (6*2 except JIANG)
 * 
 * 2. Piece-centric
 * for each piece
 * - existence (32)
 * - normalized position/coordinates (32*2)
 * - lowest ranked attacker and defender (32*2 only store rank value)
 * - sliding mobility for sliding pieces, i.e. Ju and Pao, in each direction(2*2*2*4)
 * 
 * 3. Position-centric
 * for each board position for current side
 * - lowest ranked attacker and defender (9*10*2 only store rank value)
 */
public class Feature {
    /*
     * parameters:
     * @board is the whole board position
     * @redTurn indicates those turn it is, which can't be extracted from board
     * 
     *  return a double-valued list with size three
     *  each list stores features in the main categories
     */
    public static List<List<Double>> featureExtractor(Game game) {
        List<List<Double>> features = new ArrayList<List<Double>>();
        features.add(globalFeature(game));
        features.add(pieceFeature(game));
        features.add(positionFeature(game));

        return features;
    }

    /*
     * extract global feature
     */
    private static List<Double> globalFeature(Game game) {
        List<Double> globalF = new ArrayList<Double>();
        boolean redTurn = game.isRedTurn();
        Piece[][] board = game.getPieces();

        // which side to move, 1 for red, 0 for black
        if (redTurn) globalF.add(new Double(0));
        else globalF.add(new Double(1));

        /*
         *  total number of each type of piece on each side except JIANG
         */
        int[][] num = new int[2][6];
        for (int i = 0; i < Game.ROW; i++) {
            for (int j = 0; j < Game.COLUMN; j++) {
                Piece p = board[i][j];
                if (p != null && !p.getName().equals(PieceName.JIANG)) {
                    num[p.getColor().ordinal()][p.getName().ordinal()]++;
                }
            }
        }

        for (int i = 0; i < 2; i++) {
            for (int j = 0; j < 6; j++) {
                globalF.add(new Double(num[i][j]));
            }
        }

        return globalF;
    }

    /*
     * extract chess piece centric feature
     */
    private static List<Double> pieceFeature(Game game) {
        Piece[][] board = game.getPieces();
        /*
         *  each slot has five features:
         *  presence, x, y, attacker, defender
         */
        final int featSize = 160;
        int[] starter = {0, 10, 20, 30, 55, 65, 75, 0, 0, 0, 0, 0, 0, 0};
        int offset = featSize / 2;
        for (int i = 7; i < starter.length; i++)
            starter[i] = offset + starter[i-7];

        double[] feat = new double[featSize];

        for (int i = 0; i < Game.ROW; i++) {
            for (int j = 0; j < Game.COLUMN; j++) {
                Piece p = board[i][j];
                if (p != null) {
                    int pointer = p.getName().ordinal();
                    Colors c = p.getColor(); 
                    if (c.equals(Colors.BLACK))
                        pointer += starter.length / 2;

                    int index = starter[pointer];
                    feat[index] = 1; // it is present
                    double[] normPos = normalizeCoord(i, j);
                    feat[index+1] = normPos[0];
                    feat[index+2] = normPos[1];

                    // attack/defend map
                    BoardPosition bp = new BoardPosition(i, j);
                    feat[index+3] = attackPieceMap(game, bp, c);
                    feat[index+4] = defendPieceMap(game, bp, c);
                    starter[pointer] += 5;
                }
            }
        }

        /*
         *  check existence bit, if 0, set normalized x-, y- coordinates
         *  and attack/defend mapper as -1
         */
        List<Double> pieceF = new ArrayList<Double>();
        for (int i =0; i < featSize; i++) {
            pieceF.add(new Double(feat[i]));
            if (i % 5 == 0 && feat[i] == 0) {
                feat[i+1] = -1;
                feat[i+2] = -1;
                feat[i+3] = -1;
                feat[i+4] = -1;
            }
        }

        // sliding piece (farthest) mobility
        double[] mobility = slideMobility(game);
        for (double d : mobility) {
            pieceF.add(new Double(d));
        }

        return pieceF;
    }

    /*
     * Mobility of sliding piece (JU and PAO)
     * stopped by any piece, or border of the board
     */
    private static double[] slideMobility(Game game) {
        double[] mobility = new double[32];
        for (int i = 0; i < mobility.length; i++) {
            mobility[i] = -1; // -1 means no such piece
        }

        int[] starter = new int[4];
        for (int i = 0; i < starter.length; i++) {
            starter[i] = 8 * i;
        }

        for (int i = 0; i < Game.ROW; i++) {
            for (int j = 0; j < Game.COLUMN; j++) {
                Piece p = game.getPiece(i, j);
                if (p != null) {
                    PieceName name = p.getName();
                    int loc = -1;
                    if (name.equals(PieceName.JU)) 
                        loc = starter.length / 2 * p.getColor().ordinal();
                    else if (name.equals(PieceName.PAO))
                        loc = starter.length / 2 * p.getColor().ordinal() + 1;

                    if (loc != -1) {
                        // find the farthest move from all legal moves
                        BoardPosition current = new BoardPosition(i, j);
                        List<BoardPosition> legalMoves = p.getLegalMoves(game.getPieces(), current);

                        for (BoardPosition pos : legalMoves) {
                            BoardPosition dist = current.distance(pos);
                            int rowDiff = dist.Row(), colDiff = dist.Col();;
                            if (colDiff == 0) {
                                int index = starter[loc] + 1;
                                if (rowDiff < 0) {
                                    index--;
                                    rowDiff = -rowDiff;
                                }

                                if (mobility[index] == -1 || mobility[index] < rowDiff)
                                    if (game.getPiece(pos.Row(), pos.Col()) == null)
                                        mobility[index] = rowDiff;
                            } else if (rowDiff == 0) {
                                int index = starter[loc] + 3;
                                if (colDiff < 0) {
                                    index--;
                                    colDiff = -colDiff;
                                }

                                if (mobility[index] == -1 || mobility[index] < colDiff)
                                    if (game.getPiece(pos.Row(), pos.Col()) == null)
                                        mobility[index] =colDiff;
                            }
                        }
                        starter[loc] += 4;
                    }
                }
            }
        }

        // proofread the mobility
        for (int i = 0; i < mobility.length; i+=4) {
            boolean presence = false;
            for (int j = i; j < i+4; j++) {
                if (mobility[j] >= 0) {
                    presence = true;
                    break;
                }
            }

            for (int j = i; j < i+4; j++) {
                if (presence && mobility[j] == -1) mobility[j] = 0;
            }
        }
        return mobility;
    }

    /*
     * normalize position coordinate to range [0, 1]
     */
    private static double[] normalizeCoord(int row, int col) {
        double upper = 1.0, lower = 0.0;
        double[] normed = new double[2];
        normed[0] = (upper - lower) * row / (Game.ROW - 1) + lower;
        normed[1] = (upper - lower) * col / (Game.COLUMN - 1) + lower;
        return normed;
    }

    /*
     * find all opponent piece that can attack position @bp 
     * and return the rank of the lowest ranked one
     */
    public static int attackPieceMap(Game game, BoardPosition bp, Colors side) {
        int lowest = 1000;
        for (int i = 0; i < Game.ROW; i++) {
            for (int j = 0; j < Game.COLUMN; j++) {
                Piece p = game.getPiece(i, j);

                if (p != null && !p.getColor().equals(side)) {
                    Game g = game.copy();
                    int success = g.movePiece(new BoardPosition(i, j), bp);

                    if (success >= 0 && lowest > p.getRank()) {
                        lowest = p.getRank();
                    }
                }
            }
        }

        if (lowest == 1000)
            lowest = 0; // no opponent piece attacker
        return lowest;
    }

    /*
     * find all other pieces of the same side that can defend the piece at 
     * position @bp and return the lowest ranked value
     */
    public static int defendPieceMap(Game game, BoardPosition bp, Colors side) {
        int lowest = 1000;
        for (int i = 0; i < Game.ROW; i++) {
            for (int j = 0; j < Game.COLUMN; j++) {
                Piece p = game.getPiece(i, j);

                if (p != null && p.getColor().equals(side)) {
                    Game g = game.copy();
                    Piece target = g.getPiece(bp.Row(), bp.Col());
                    Piece from = g.getPiece(i, j);

                    boolean flag = target != null && 
                        (side.equals(target.getColor()) || from.getColor().equals(target.getColor()));
                    if (flag) target.changeColor();
                    List<BoardPosition> moves = p.getLegalMoves(g.getPieces(), new BoardPosition(i, j));
                    if (flag) target.changeColor();

                    boolean success = false;
                    for (BoardPosition pos : moves) {
                        if (pos.equals(bp)) {
                            success = true;
                            break;
                        }
                    }

                    if (success && lowest > p.getRank()) {
                        lowest = p.getRank();
                    }
                }
            }
        }

        if (lowest == 1000)
            lowest = 0; // no self piece defender
        return lowest;
    }

    /*
     * extract board position centric feature
     */
    private static List<Double> positionFeature(Game game) {
        List<Double> positionF = new ArrayList<Double>();
        Colors side = Colors.RED;
        if (!game.isRedTurn()) side = Colors.BLACK;

        for (int i = 0; i < Game.ROW; i++) {
            for (int j = 0; j < Game.COLUMN; j++) {
                BoardPosition current = new BoardPosition(i, j);
                int attacker = attackPositionMap(game, current, side);
                int defender = defendPositionMap(game, current, side);
                positionF.add(new Double(attacker));
                positionF.add(new Double(defender));
            }
        }

        return positionF;
    }

    /*
     * attack map of board position @bp
     */
    public static int attackPositionMap(Game game, BoardPosition bp, Colors side) {
        int lowest = 1000;
        for (int i = 0; i < Game.ROW; i++) {
            for (int j = 0; j < Game.COLUMN; j++) {
                Piece p = game.getPiece(i, j);

                if (p != null && !p.getColor().equals(side)) {
                    int rank = p.getRank();
                    Game g = game.copy();
                    Piece target = g.getPiece(bp.Row(), bp.Col());
                    boolean flag = target != null && target.getColor().equals(p.getColor());
                    if (flag) target.changeColor();
                    List<BoardPosition> moves = p.getLegalMoves(g.getPieces(), new BoardPosition(i, j));
                    if (flag) target.changeColor();

                    boolean success = false;
                    for (BoardPosition pos : moves) {
                        if (pos.equals(bp)) {
                            success = true;
                            break;
                        }
                    }

                    if (success && lowest > rank) {
                        lowest = rank;
                    }
                }
            }
        }

        if (lowest == 1000)
            lowest = 0; // no opponent piece attacker
        return lowest;
    }

    /*
     * defend map of board position @bp
     */
    public static int defendPositionMap(Game game, BoardPosition bp, Colors side) {
        return defendPieceMap(game, bp, side);
    }
}
