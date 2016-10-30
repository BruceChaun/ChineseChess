package pieces;

import constants.*;

public class Shi extends Piece {
	public Shi(Colors color) {
		super(color);
		
		name = PieceName.SHI;
		if (color.equals(Colors.BLACK))
			this.imageURL = "../img/black_shi.png";
		else 
			this.imageURL = "../img/red_shi.png";
	}
}
