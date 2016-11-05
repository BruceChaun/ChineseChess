package game;

public class BoardPosition {
	private int row;
	private int col;
	
	public BoardPosition(int row, int col) {
		this.row = row;
		this.col = col;
	}
	
	public int Row() {return this.row;}
	public int Col() {return this.col;}
	
	public boolean equals(BoardPosition that) {
		if (this.row == that.row && this.col == that.col)
			return true;
		return false;
	}
}
