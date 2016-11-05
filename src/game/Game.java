package game;

import java.util.List;

import constants.Colors;
import constants.PieceName;
import pieces.Bing;
import pieces.Jiang;
import pieces.Ju;
import pieces.Ma;
import pieces.Pao;
import pieces.Piece;
import pieces.Shi;
import pieces.Xiang;

public class Game {
	private Piece[][] pieces;
	private boolean redTurn = true;
	private Colors winner;

	public static final int ROW = 10;
	public static final int COLUMN = 9;
	
	/*
	 * get the whole chess board position
	 */
	public Piece[][] getPieces() {
		return pieces;
	}
	
	/*
	 * get a specific chess piece at position (@row, @col)
	 */
	public Piece getPiece(int row, int col) {
		return pieces[row][col];
	}
	
	/*
	 * return if the next move is red turn or not
	 */
	public boolean isRedTurn() {
		return redTurn;
	}
	
	/*
	 * change turn after each move
	 */
	public void changeTurn() {
		redTurn = !redTurn;
	}

	/*
	 * get winner of current chess position
	 * 
	 * null winner means no checkmate
	 */
	public Colors getWinner() {
		return winner;
	}

	/*
	 *   initialize the chess board layout, i.e. place the chess pieces.
	 *   
	 *   The black pieces are always at the upper part of the board while
	 *   the red ones are at the lower part.
	 */
	public void initBoard() {
		this.pieces = new Piece[ROW][COLUMN];
		this.winner = null;
		this.redTurn = true;
		
		/*
		 *   black player
		 */
		pieces[0][0] = new Ju(Colors.BLACK);
		pieces[0][1] = new Ma(Colors.BLACK);
		pieces[0][2] = new Xiang(Colors.BLACK);
		pieces[0][3] = new Shi(Colors.BLACK);
		pieces[0][4] = new Jiang(Colors.BLACK);
		pieces[0][5] = new Shi(Colors.BLACK);
		pieces[0][6] = new Xiang(Colors.BLACK);
		pieces[0][7] = new Ma(Colors.BLACK);
		pieces[0][8] = new Ju(Colors.BLACK);
		pieces[2][1] = new Pao(Colors.BLACK);
		pieces[2][7] = new Pao(Colors.BLACK);
		pieces[3][0] = new Bing(Colors.BLACK);
		pieces[3][2] = new Bing(Colors.BLACK);
		pieces[3][4] = new Bing(Colors.BLACK);
		pieces[3][6] = new Bing(Colors.BLACK);
		pieces[3][8] = new Bing(Colors.BLACK);
		
		/*
		 *   red player
		 */
		pieces[9][0] = new Ju(Colors.RED);
		pieces[9][1] = new Ma(Colors.RED);
		pieces[9][2] = new Xiang(Colors.RED);
		pieces[9][3] = new Shi(Colors.RED);
		pieces[9][4] = new Jiang(Colors.RED);
		pieces[9][5] = new Shi(Colors.RED);
		pieces[9][6] = new Xiang(Colors.RED);
		pieces[9][7] = new Ma(Colors.RED);
		pieces[9][8] = new Ju(Colors.RED);
		pieces[7][1] = new Pao(Colors.RED);
		pieces[7][7] = new Pao(Colors.RED);
		pieces[6][0] = new Bing(Colors.RED);
		pieces[6][2] = new Bing(Colors.RED);
		pieces[6][4] = new Bing(Colors.RED);
		pieces[6][6] = new Bing(Colors.RED);
		pieces[6][8] = new Bing(Colors.RED);
	}

	/*
	 *   check the move of the second stage, move if legal, 
	 *   otherwise do nothing 
	 *   (or you can reset last position)
	 *   
	 *   parameters:
	 *   @from and @to: move from @from to @to 
	 */
	public boolean movePiece(BoardPosition from, BoardPosition to) {
		int row = to.Row(), col = to.Col();
		int lastRow = from.Row(), lastCol = from.Col();
		
		// check if legal move
		if (!isLegalMove(from, to)) return false;
				
		// can move if there is no piece in that place
		if (pieces[row][col] == null) {
			pieces[row][col] = pieces[lastRow][lastCol];
			pieces[lastRow][lastCol] = null;
			this.changeTurn();
			return true;
		} else {
			Colors colorInFrom = pieces[lastRow][lastCol].getColor();
			Colors colorInTo = pieces[row][col].getColor();
			
			if (colorInFrom.equals(colorInTo)) return false;
			else {
				// eat your enemy
				if (pieces[row][col].getName().equals(PieceName.JIANG)) {
					this.winner = pieces[lastRow][lastCol].getColor();
				}
				
				pieces[row][col].eaten();
				pieces[row][col] = pieces[lastRow][lastCol];
				pieces[lastRow][lastCol] = null;
				this.changeTurn();
				return true;
			}
		}
	}

	/*
	 * check move (@from, @to) is legal or not
	 */
	private boolean isLegalMove(BoardPosition from, BoardPosition to) {
		int row = from.Row(), col = from.Col();
		List<BoardPosition> allMoves = pieces[row][col].getLegalMoves(pieces, from);
		for (BoardPosition pos : allMoves) {
			if (to.equals(pos)) return true;
		}
		return false;
	}
}
