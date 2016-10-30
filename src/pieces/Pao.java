package pieces;

import constants.*;

public class Pao extends Piece {
	public Pao(Colors color) {
		super(color);
		
		name = PieceName.PAO;
		if (color.equals(Colors.BLACK))
			this.imageURL = "../img/black_pao.png";
		else 
			this.imageURL = "../img/red_pao.png";
	}
}
