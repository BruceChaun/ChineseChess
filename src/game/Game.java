package game;

import java.util.*;

import algo.Feature;
import algo.NN;
import algo.TD;
import constants.*;
import pieces.*;

public class Game {
    private Piece[][] pieces;
    private boolean redTurn = true;
    private Colors winner;
    private Recorder recorder;

    public static final int ROW = 10;
    public static final int COLUMN = 9;

    /*
     * get the whole chess board position
     */
    public Piece[][] getPieces() {
        return pieces;
    }

    /*
     * get a specific chess piece at position (@row, @col)
     */
    public Piece getPiece(int row, int col) {
        return pieces[row][col];
    }

    /*
     * get a specific piece position by name and color
     */
    public BoardPosition getPiecePosition(Colors color, PieceName name) {
        for (int i = 0; i < ROW; i++) {
            for (int j = 0; j < COLUMN; j++) {
                Piece p = pieces[i][j];
                if (p != null && p.getColor().equals(color) && p.getName().equals(name))
                    return new BoardPosition(i, j);
            }
        }
        return null;
    }

    /*
     * return if the next move is red turn or not
     */
    public boolean isRedTurn() {
        return redTurn;
    }
    
    public Colors getTurn() {
        return this.redTurn ? Colors.RED : Colors.BLACK;
    }

    /*
     * change turn after each move
     */
    public void changeTurn() {
        redTurn = !redTurn;
    }

    /*
     * get winner of current chess position
     * 
     * null winner means no checkmate
     */
    public Colors getWinner() {
        return winner;
    }

    /*
     * record a move
     */
    public void record(String move) {
        BoardPosition from = new BoardPosition(
                Integer.parseInt(move.substring(1, 2)), Integer.parseInt(move.substring(0, 1)));
        BoardPosition to = new BoardPosition(
                Integer.parseInt(move.substring(3, 4)), Integer.parseInt(move.substring(2, 3)));
        this.recorder.record(from, to);
    }
    
    /*
     * retrieve current record
     */
    public String getRecord() {
        return this.recorder.retrieve();
    }

    /*
     * get a copy of current board
     */
    public Game copy() {
        Game g = new Game();
        g.setPieces(pieces);
        g.setRedTurn(redTurn);
        g.setWinner(winner);
        return g;
    }

    private void setPieces(Piece[][] pieces) {
        this.pieces = new Piece[ROW][COLUMN];
        // deep copy
        for (int i = 0; i < ROW; i++) {
            for (int j = 0; j < COLUMN; j++) {
                if (pieces[i][j] != null)
                    this.pieces[i][j] = pieces[i][j].copy(); 
            }
        }
    }

    private void setRedTurn(boolean redTurn) {
        this.redTurn = redTurn;
    }

    private void setWinner(Colors winner) {
        this.winner = winner;
    }

    /*
     *   initialize the chess board layout, i.e. place the chess pieces.
     *   
     *   The black pieces are always at the upper part of the board while
     *   the red ones are at the lower part.
     */
    public void initBoard() {
        this.pieces = new Piece[ROW][COLUMN];
        this.winner = null;
        this.redTurn = true;
        this.recorder = new Recorder();

        /*
         *   black player
         */
        pieces[0][0] = new Ju(Colors.BLACK);
        pieces[0][1] = new Ma(Colors.BLACK);
        pieces[0][2] = new Xiang(Colors.BLACK);
        pieces[0][3] = new Shi(Colors.BLACK);
        pieces[0][4] = new Jiang(Colors.BLACK);
        pieces[0][5] = new Shi(Colors.BLACK);
        pieces[0][6] = new Xiang(Colors.BLACK);
        pieces[0][7] = new Ma(Colors.BLACK);
        pieces[0][8] = new Ju(Colors.BLACK);
        pieces[2][1] = new Pao(Colors.BLACK);
        pieces[2][7] = new Pao(Colors.BLACK);
        pieces[3][0] = new Bing(Colors.BLACK);
        pieces[3][2] = new Bing(Colors.BLACK);
        pieces[3][4] = new Bing(Colors.BLACK);
        pieces[3][6] = new Bing(Colors.BLACK);
        pieces[3][8] = new Bing(Colors.BLACK);

        /*
         *   red player
         */
        pieces[9][0] = new Ju(Colors.RED);
        pieces[9][1] = new Ma(Colors.RED);
        pieces[9][2] = new Xiang(Colors.RED);
        pieces[9][3] = new Shi(Colors.RED);
        pieces[9][4] = new Jiang(Colors.RED);
        pieces[9][5] = new Shi(Colors.RED);
        pieces[9][6] = new Xiang(Colors.RED);
        pieces[9][7] = new Ma(Colors.RED);
        pieces[9][8] = new Ju(Colors.RED);
        pieces[7][1] = new Pao(Colors.RED);
        pieces[7][7] = new Pao(Colors.RED);
        pieces[6][0] = new Bing(Colors.RED);
        pieces[6][2] = new Bing(Colors.RED);
        pieces[6][4] = new Bing(Colors.RED);
        pieces[6][6] = new Bing(Colors.RED);
        pieces[6][8] = new Bing(Colors.RED);
    }

    public void initBoard(String record) {
        this.initBoard();
        this.simulate(record);
    }

    public void initBoard(String record, int steps) {
        this.initBoard();
        this.simulate(record, steps);
    }

    /*
     *   check the move of the second stage, move if legal, 
     *   otherwise do nothing 
     *   (or you can reset last position)
     *   
     *   parameters:
     *   @from and @to: move from @from to @to
     *   
     *   @return 
     *   the rank score of the eaten piece,
     *   0 if not eat any piece
     *   -1 if illegal move
     */
    public int movePiece(BoardPosition from, BoardPosition to) {
        int row = to.Row(), col = to.Col();
        int lastRow = from.Row(), lastCol = from.Col();

        // check if legal move
        if (!this.isLegalMove(from, to)) return -1;

        // can move if there is no piece in that place
        if (pieces[row][col] == null) {
            pieces[row][col] = pieces[lastRow][lastCol];
            pieces[lastRow][lastCol] = null;
            this.changeTurn();
            return 0;
        } else {
            Colors colorInFrom = pieces[lastRow][lastCol].getColor();
            Colors colorInTo = pieces[row][col].getColor();

            // collision, same color
            if (colorInFrom.equals(colorInTo)) {
                return -1;
            }
            else {
                // eat your enemy
                if (pieces[row][col].getName().equals(PieceName.JIANG)) {
                    this.winner = pieces[lastRow][lastCol].getColor();
                }
                int rank = pieces[row][col].getRank();

                pieces[row][col].eaten();
                pieces[row][col] = pieces[lastRow][lastCol];
                pieces[lastRow][lastCol] = null;
                this.changeTurn();
                return rank;
            }
        }
    }

    /*
     * check move (@from, @to) is legal or not
     */
    private boolean isLegalMove(BoardPosition from, BoardPosition to) {
        int row = from.Row(), col = from.Col();
        int toRow = to.Row(), toCol = to.Col();
        Piece pieceToMove = pieces[row][col];
        List<BoardPosition> allMoves = pieceToMove.getLegalMoves(pieces, from);
        
        BoardPosition blackJiang = getPiecePosition(Colors.BLACK, PieceName.JIANG);
        BoardPosition redJiang = getPiecePosition(Colors.RED, PieceName.JIANG);
        BoardPosition OpponentJiang = blackJiang;
        if (pieceToMove.getColor().equals(Colors.BLACK)) 
            OpponentJiang = redJiang;

        if (Math.min(blackJiang.Row(), redJiang.Row()) < row && row < Math.max(blackJiang.Row(), redJiang.Row())) {
            if (pieceToMove.getName().equals(PieceName.JIANG)) {
                if (toCol ==  OpponentJiang.Col() && 
                        numberOfPieces(toCol, toRow, OpponentJiang.Row()) == 0) 
                    return false;
            } else if (blackJiang.Col() == redJiang.Col() && col == redJiang.Col()) {
                if (numberOfPieces(col, blackJiang.Row(), redJiang.Row()) == 1 && toCol != col)
                    return false;
            }
        }

        for (BoardPosition pos : allMoves) {
            if (to.equals(pos)) return true;
        }
        return false;
    }

    /*
     * check how many pieces are in column @col and between 
     * @row1 and @row2, both exclusive
     */
    private int numberOfPieces(int col, int row1, int row2) {
        if (row1 == row2) return 0;
        if (row1 > row2) return numberOfPieces(col, row2, row1);

        int num = 0;
        for (int r = row1+1; r < row2; r++) {
            if (pieces[r][col] != null) num++;
        }
        return num;
    }

    /*
     * simulate the chess playing process, according to the chess record
     * 
     * parameters
     * @record is a string of chess record, e.g. "26251222174772427967.....", 
     * where four digits represent a move and eight digits represent a round
     * 
     * @ steps is the number of steps (suppose four digits as a step). If this 
     * argument is missing, simulate till the end of the chess record
     */
    public void simulate(String record, int steps) {
        int numMoves = record.length() / 4;
        if (steps >= 0) numMoves = steps;

        for (int i = 0; i < numMoves; i++) {
            String move = record.substring(i*4, i*4+4);
            BoardPosition from = new BoardPosition(
                    Integer.parseInt(move.substring(1, 2)), Integer.parseInt(move.substring(0, 1)));
            BoardPosition to = new BoardPosition(
                    Integer.parseInt(move.substring(3, 4)), Integer.parseInt(move.substring(2, 3)));
            int val = this.movePiece(from, to);
            if (val < 0) {
                System.out.println(i + "\t" + move);
            }
            this.record(move);
        }

        // reset turn
        if (numMoves % 2 == 1)
            this.redTurn = false;
    }

    public void simulate(String record) {
        this.simulate(record, -1);
    }
    
    /*
     * check piece one by one and store in a list of String array, 
     * each string is a standard move string
     */
    public List<String> getAllPiecePossibleMoves() {
        Colors side = getTurn();
        List<String> allMoves = new ArrayList<String>();
        
        for (int i = 0; i < ROW; i++) {
            for (int j = 0; j < COLUMN; j++) {
                Piece p = getPiece(i, j);
                if (p != null && p.getColor().equals(side)) {
                    List<BoardPosition> pieceMoves = p.getLegalMoves(getPieces(), new BoardPosition(i, j));
                    for (BoardPosition bp : pieceMoves) {
                        allMoves.add(j + "" + i + bp.toString());
                    }
                }
            }
        }
        return allMoves;
    }
    
    /*
     * choose the optimal move from all possible moves.
     * The returned string has the same format as the record
     */
    public String chooseOptMove(NN nn) {
        String optMove = "";
        double bestScore = Double.NEGATIVE_INFINITY;
        List<String> allMoves = getAllPiecePossibleMoves();
        
        // naive implementation: TD(0)
        for (String m : allMoves) {
            Game g = this.copy();
            double val = g.movePiece(
                    new BoardPosition(Integer.parseInt(m.substring(1, 2)), Integer.parseInt(m.substring(0, 1))), 
                    new BoardPosition(Integer.parseInt(m.substring(3, 4)), Integer.parseInt(m.substring(2, 3))));
            
            if (g.getWinner() != null) {
                optMove = m;
                break;
            }
            g.changeTurn();
            val += nn.forward(Feature.featureExtractor(g)) * TD.gamma;
            if (bestScore < val) {
                optMove = m;
                bestScore = val;
            }
        }
        
        return optMove;
    }
    
    public String chooseRandMove() {
        List<String> allMoves = getAllPiecePossibleMoves();
        String move = allMoves.get(new Random().nextInt(allMoves.size()));
        return move;
    }
}
