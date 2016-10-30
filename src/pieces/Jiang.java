package pieces;

import constants.*;

public class Jiang extends Piece{
	public Jiang(Colors color) {
		super(color);
		
		name = PieceName.JIANG;
		if (color.equals(Colors.BLACK))
			this.imageURL = "../img/black_jiang.png";
		else 
			this.imageURL = "../img/red_jiang.png";
	}
}
