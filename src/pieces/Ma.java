package pieces;

import constants.*;

public class Ma extends Piece {
	public Ma(Colors color) {
		super(color);
		
		name = PieceName.MA;
		if (color.equals(Colors.BLACK))
			this.imageURL = "../img/black_ma.png";
		else 
			this.imageURL = "../img/red_ma.png";
	}
}
