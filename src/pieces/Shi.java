package pieces;

import constants.*;

public class Shi extends Piece {
	public Shi(Colors color, int row, int col) {
		super(color, row, col);
		
		name = PieceName.SHI;
		if (color.equals(Colors.BLACK))
			this.imageURL = "../img/black_shi.png";
		else 
			this.imageURL = "../img/red_shi.png";
	}
}
