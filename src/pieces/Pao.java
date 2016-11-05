package pieces;

import java.util.ArrayList;
import java.util.List;

import constants.*;
import game.BoardPosition;
import game.Game;

public class Pao extends Piece {
	public Pao(Colors color) {
		super(color);
		
		name = PieceName.PAO;
		if (color.equals(Colors.BLACK))
			this.imageURL = "../img/black_pao.png";
		else 
			this.imageURL = "../img/red_pao.png";
	}

	@Override
	public List<BoardPosition> getLegalMoves(Piece[][] board, BoardPosition now) {
		int row = now.Row(), col = now.Col();
		List<BoardPosition> moves = new ArrayList<BoardPosition>();
		
		// go up
		boolean eat = false;
		for (int r = row-1; r>=0; r--) {
			if (board[r][col] == null) {
				if (!eat) moves.add(new BoardPosition(r, col));
			} else if (!eat){
				eat = true;
			} else if (!board[r][col].getColor().equals(this.color)) {
				moves.add(new BoardPosition(r, col));
				break;
			}
		}
		
		// go down
		eat = false;
		for (int r = row+1; r<Game.ROW; r++) {
			if (board[r][col] == null) {
				if (!eat) moves.add(new BoardPosition(r, col));
			} else if (!eat){
				eat = true;
			} else if (!board[r][col].getColor().equals(this.color)) {
				moves.add(new BoardPosition(r, col));
				break;
			}
		}
		
		// go left
		eat = false;
		for (int c = col-1; c>=0; c--) {
			if (board[row][c] == null) {
				if (!eat) moves.add(new BoardPosition(row, c));
			} else if (!eat){
				eat = true;
			} else if (!board[row][c].getColor().equals(this.color)) {
				moves.add(new BoardPosition(row, c));
				break;
			}
		}
		
		// go right
		eat = false;
		for (int c = col+1; c<Game.COLUMN; c++) {
			if (board[row][c] == null) {
				if (!eat) moves.add(new BoardPosition(row, c));
			} else if (!eat){
				eat = true;
			} else if (!board[row][c].getColor().equals(this.color)) {
				moves.add(new BoardPosition(row, c));
				break;
			}
		}
		return moves;
	}

}
