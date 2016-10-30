package pieces;

import constants.*;

public class Xiang extends Piece{
	public Xiang(Colors color, int row, int col) {
		super(color, row, col);
		
		name = PieceName.XIANG;
		if (color.equals(Colors.BLACK))
			this.imageURL = "../img/black_xiang.png";
		else 
			this.imageURL = "../img/red_xiang.png";
	}
}
