package pieces;

import constants.*;

public class Ju extends Piece {
	public Ju(Colors color) {
		super(color);
		
		name = PieceName.JU;
		if (color.equals(Colors.BLACK))
			this.imageURL = "../img/black_ju.png";
		else 
			this.imageURL = "../img/red_ju.png";
	}
}
