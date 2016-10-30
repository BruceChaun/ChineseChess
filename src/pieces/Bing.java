package pieces;

import constants.*;
import ui.Board;

public class Bing  extends Piece {
	public Bing(Colors color, int row, int col) {
		super(color, row, col);
		
		name = PieceName.BING;
		if (color.equals(Colors.BLACK))
			this.imageURL = "../img/black_bing.png";
		else 
			this.imageURL = "../img/red_bing.png";
	}
	
	/*
	 *   Set the convention that the red is at the lower part of the board
	 */
	public boolean isCrossRiver() {
		if (color.equals(Colors.BLACK))
			return rowPosition >= Board.ROW / 2;
		else 
			return rowPosition < Board.ROW / 2;
	}
}
