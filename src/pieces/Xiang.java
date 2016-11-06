package pieces;

import java.util.ArrayList;
import java.util.List;

import constants.*;
import game.BoardPosition;
import game.Game;

public class Xiang extends Piece{
	public Xiang(Colors color) {
		super(color);
		this.rank = 2;
		
		name = PieceName.XIANG;
		if (color.equals(Colors.BLACK))
			this.imageURL = "../img/black_xiang.png";
		else 
			this.imageURL = "../img/red_xiang.png";
	}

	@Override
	public List<BoardPosition> getLegalMoves(Piece[][] board, BoardPosition now) {
		int row = now.Row(), col = now.Col();
		List<BoardPosition> moves = new ArrayList<BoardPosition>();
		
		moves.add(new BoardPosition(row+2, col+2));
		moves.add(new BoardPosition(row-2, col+2));
		moves.add(new BoardPosition(row-2, col-2));
		moves.add(new BoardPosition(row+2, col-2));
		
		// check each possible move and remove it if illegal
		for (int i = moves.size()-1; i>=0; i--) {
			BoardPosition pos = moves.get(i);
			int r = pos.Row(), c = pos.Col();
			if (c < 0 || c >= Game.COLUMN) {
				moves.remove(pos);
			} else if ((r < 0 || r > 4) && this.color.equals(Colors.BLACK)) {
				// black side
				moves.remove(pos);
			} else if ((r >= Game.ROW || r < 5) && this.color.equals(Colors.RED)) { 
				// red side
				moves.remove(pos);
			} else if (board[r][c] != null &&
					board[r][c].getColor().equals(this.color)) {
				moves.remove(pos);
			} else if (board[(r+row)/2][(c+col)/2] != null) {
				moves.remove(pos);
			}
		}
		return moves;
	}

}
