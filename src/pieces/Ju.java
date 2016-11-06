package pieces;

import java.util.ArrayList;
import java.util.List;

import constants.*;
import game.BoardPosition;
import game.Game;

public class Ju extends Piece {
	public Ju(Colors color) {
		super(color);
		this.rank = 9;
		
		name = PieceName.JU;
		if (color.equals(Colors.BLACK))
			this.imageURL = "../img/black_ju.png";
		else 
			this.imageURL = "../img/red_ju.png";
	}

	@Override
	public List<BoardPosition> getLegalMoves(Piece[][] board, BoardPosition now) {
		int row = now.Row(), col = now.Col();
		List<BoardPosition> moves = new ArrayList<BoardPosition>();
		
		// go up
		for (int r = row-1; r>=0; r--) {
			if (board[r][col] == null) {
				moves.add(new BoardPosition(r, col));
			} else {
				if (!board[r][col].getColor().equals(this.color))
					moves.add(new BoardPosition(r, col));
				break;
			}
		}
		
		// go down
		for (int r = row+1; r<Game.ROW; r++) {
			if (board[r][col] == null) {
				moves.add(new BoardPosition(r, col));
			} else {
				if (!board[r][col].getColor().equals(this.color))
					moves.add(new BoardPosition(r, col));
				break;
			}
		}
		
		// go left
		for (int c = col-1; c>=0; c--) {
			if (board[row][c] == null) {
				moves.add(new BoardPosition(row, c));
			} else {
				if (!board[row][c].getColor().equals(this.color))
					moves.add(new BoardPosition(row, c));
				break;
			}
		}
		
		// go right
		for (int c = col+1; c<Game.COLUMN; c++) {
			if (board[row][c] == null) {
				moves.add(new BoardPosition(row, c));
			} else {
				if (!board[row][c].getColor().equals(this.color))
					moves.add(new BoardPosition(row, c));
				break;
			}
		}
		
		return moves;
	}

}
