package pieces;

import java.util.ArrayList;
import java.util.List;

import constants.*;
import game.BoardPosition;
import game.Game;

public class Ma extends Piece {
	public Ma(Colors color) {
		super(color);
		
		name = PieceName.MA;
		if (color.equals(Colors.BLACK))
			this.imageURL = "../img/black_ma.png";
		else 
			this.imageURL = "../img/red_ma.png";
	}

	@Override
	public List<BoardPosition> getLegalMoves(Piece[][] board, BoardPosition now) {
		int row = now.Row(), col = now.Col();
		List<BoardPosition> moves = new ArrayList<BoardPosition>();
		
		if (row > 0 && board[row-1][col] == null) {
			moves.add(new BoardPosition(row-2, col-1));
			moves.add(new BoardPosition(row-2, col+1));
		}
		if (col < Game.COLUMN-1 && board[row][col+1] == null) {
			moves.add(new BoardPosition(row-1, col+2));
			moves.add(new BoardPosition(row+1, col+2));
		}
		if (row < Game.ROW-1 && board[row+1][col] == null) {
			moves.add(new BoardPosition(row+2, col+1));
			moves.add(new BoardPosition(row+2, col-1));
		}
		if (col > 0 && board[row][col-1] == null) {
			moves.add(new BoardPosition(row+1, col-2));
			moves.add(new BoardPosition(row-1, col-2));
		}
		
		// check each possible move and remove it if illegal
		for (int i = moves.size()-1; i>=0; i--) {
			BoardPosition pos = moves.get(i);
			int r = pos.Row(), c = pos.Col();
			if (c < 0 || c >= Game.COLUMN || r < 0 || r >= Game.ROW) {
				moves.remove(pos);
			} else if (board[r][c] != null &&
					board[r][c].getColor().equals(this.color)) {
				moves.remove(pos);
			}
		}
		return moves;
	}

}
