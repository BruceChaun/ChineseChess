package pieces;

import constants.*;
import ui.Board;

public class Bing  extends Piece {
	public Bing(Colors color) {
		super(color);
		
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
			return row >= Board.ROW / 2;
		else 
			return row < Board.ROW / 2;
	}
}
