package pieces;

import java.util.List;

import constants.Colors;
import constants.PieceName;
import game.BoardPosition;

public abstract class Piece {
	protected PieceName name;
	protected String  imageURL;
	protected Colors color;
//	protected int rowPosition;
//	protected int colPosition;
	protected boolean isEaten;
	
	public Piece(Colors color) {
		this.color = color;
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
	
	/*
	 * return all the legal moves of the piece at position now 
	 * based on current position @board
	 */
	abstract public List<BoardPosition> getLegalMoves(Piece[][] board, BoardPosition now);
}
