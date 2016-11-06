package pieces;

import java.util.ArrayList;
import java.util.List;

import constants.*;
import game.BoardPosition;
import game.Game;
import ui.Board;

public class Bing  extends Piece {
	public Bing(Colors color) {
		super(color);
		this.rank = 1;
		
		name = PieceName.BING;
		if (color.equals(Colors.BLACK))
			this.imageURL = "../img/black_bing.png";
		else 
			this.imageURL = "../img/red_bing.png";
	}
	
	/*
	 *   Set the convention that the red is at the lower part of the board
	 */
	public boolean isCrossRiver(int row) {
		if (color.equals(Colors.BLACK))
			return row >= Game.ROW / 2;
		else 
			return row < Game.ROW / 2;
	}

	@Override
	public List<BoardPosition> getLegalMoves(Piece[][] board, BoardPosition now) {
		int row = now.Row(), col = now.Col();
		List<BoardPosition> moves = new ArrayList<BoardPosition>();
		
		if (this.color.equals(Colors.BLACK)) {
			 // black side: go down
			moves.add(new BoardPosition(row+1, col));
		} else {
			// red side: go up
			moves.add(new BoardPosition(row-1, col));
		}
			
		if (isCrossRiver(row)) {
			// can go left or right if cross river
			moves.add(new BoardPosition(row, col-1));
			moves.add(new BoardPosition(row, col+1));
			this.rank = 2;
		}
			
		// check each possible move and remove it if illegal
		for (int i = moves.size()-1; i>=0; i--) {
			BoardPosition pos = moves.get(i);
			int r = pos.Row(), c = pos.Col();
			if (r < 0 || r == Game.ROW || c < 0 || c == Game.COLUMN) {
				// outside the board
				moves.remove(pos);
			} else if (board[r][c] != null &&
					board[r][c].getColor().equals(this.color)) {
				// coincide with your own piece
				moves.remove(pos);
			}
		}
		
		return moves;
	}
	
}
