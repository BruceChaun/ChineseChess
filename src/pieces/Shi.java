package pieces;

import java.util.ArrayList;
import java.util.List;

import constants.*;
import game.BoardPosition;
import game.Game;

public class Shi extends Piece {
    public Shi(Colors color) {
        super(color);
        this.rank = 2;

        name = PieceName.SHI;
        if (color.equals(Colors.BLACK))
            this.imageURL = "../img/black_shi.png";
        else 
            this.imageURL = "../img/red_shi.png";
    }

    @Override
    public List<BoardPosition> getLegalMoves(Piece[][] board, BoardPosition now) {
        int row = now.Row(), col = now.Col();
        List<BoardPosition> moves = new ArrayList<BoardPosition>();

        moves.add(new BoardPosition(row+1, col+1));
        moves.add(new BoardPosition(row-1, col+1));
        moves.add(new BoardPosition(row-1, col-1));
        moves.add(new BoardPosition(row+1, col-1));

        // check each possible move and remove it if illegal
        for (int i = moves.size()-1; i>=0; i--) {
            BoardPosition pos = moves.get(i);
            int r = pos.Row(), c = pos.Col();
            if (c < 3 || c > 5) {
                moves.remove(pos);
            } else if ((r < 0 || r > 2) && this.color.equals(Colors.BLACK)) {
                // black side box
                moves.remove(pos);
            } else if ((r == Game.ROW || r < Game.ROW - 3) && this.color.equals(Colors.RED)) { 
                // red side box
                moves.remove(pos);
            } else if (board[r][c] != null &&
                    board[r][c].getColor().equals(this.color)) {
                moves.remove(pos);
                    }
        }
        return moves;
    }

    @Override
    public Piece copy() {
        Shi shi = new Shi(this.getColor());
        return shi;
    }

}
