package algo;

import java.io.PrintWriter;
import java.util.List;

import game.*;
import pieces.Piece;
import util.FileHandler;

public class NegativeData {
    /*
     * given a chess record @record, simulate @step-1 moves 
     * and select the @step-th move as positive sample, other 
     * moves are regard as negative samples.
     * 
     *  @onlySelectedPiece indicates whether only to move the 
     *  selected piece
     *  
     *  write the generated negative record into file @fileName
     */
    public static void generate(String record, int step, String fileName, boolean onlySelectedPiece) {
        if (step < 1 || step * 4 > record.length())
            return;
        
        Game game = new Game();
        game.initBoard();
        game.simulate(record, step - 1);
        
        // negative samples of @step-th move
        String move = record.substring((step - 1) * 4, step * 4);
        BoardPosition from = new BoardPosition(
                Integer.parseInt(move.substring(1, 2)), Integer.parseInt(move.substring(0, 1)));
        BoardPosition to = new BoardPosition(
                Integer.parseInt(move.substring(3, 4)), Integer.parseInt(move.substring(2, 3)));
        
        // the selected piece
        Piece chosen = game.getPiece(from.Row(), from.Col());
        List<BoardPosition> legalMoves = chosen.getLegalMoves(game.getPieces(), from);
        
        String previousRecord = record.substring(0, (step - 1) * 4);
        PrintWriter writer = FileHandler.write(fileName);
        for (BoardPosition bp : legalMoves) {
            if (!bp.equals(to)) {
                Recorder r = new Recorder(previousRecord);
                r.record(from, bp);
                writer.println(r.output());
            }
        }
        
        if (!onlySelectedPiece) {
            // other pieces
            for (int i = 0; i < Game.ROW; i++) {
                for (int j = 0; j < Game.COLUMN; j++) {
                    Piece piece = game.getPiece(i, j);
                    if (piece != null && piece != chosen && 
                            piece.getColor().equals(chosen.getColor())) {
                        BoardPosition pos = new BoardPosition(i, j);
                        legalMoves = piece.getLegalMoves(game.getPieces(), pos);
                        for (BoardPosition bp : legalMoves) {
                            Recorder r = new Recorder(previousRecord);
                            r.record(pos, bp);
                            writer.println(r.output());
                        }
                    }
                }
            }
        }
        
        writer.close();
    }
}
