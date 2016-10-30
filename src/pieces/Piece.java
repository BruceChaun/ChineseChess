package pieces;

import constants.*;

public abstract class Piece {
	protected PieceName name;
	protected String  imageURL;
	protected Colors color;
	protected int rowPosition;
	protected int colPosition;
	protected boolean isEaten;
	
	public Piece(Colors color, int row, int col) {
		this.color = color;
		this.rowPosition = row;
		this.colPosition = col;
		this.isEaten = false;
	}
	
	public PieceName getName() {
		return this.name;
	}
	
	public String getImage() {
		return this.imageURL;
	}
	
	public Colors getColor() {
		return this.color;
	}
	
	public void eaten() {
		this.isEaten = true;
	}
}
