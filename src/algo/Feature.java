package algo;

import java.util.ArrayList;
import java.util.List;

import constants.Colors;
import constants.PieceName;
import game.BoardPosition;
import game.Game;
import pieces.Piece;

/*
 * Feature extraction from the chess board
 * separated into 3 categories and corresponding sub-categories,
 * number in the parenthesis means how many features in all
 * 
 * 1. Global
 * - side to move (1)
 * - number of each type of pieces (6*2 except JIANG)
 * 
 * 2. Piece-centric
 * for each piece
 * - existence (32)
 * - normalized position/coordinates (32*2)
 * - lowest ranked attacker and defender (32*2 only store rank value)
 * - sliding mobility for sliding pieces, i.e. Ju and Pao, in each direction(2*2*2*4)
 * 
 * 3. Position-centric
 * for each board position
 * - lowest ranked attacker and defender (9*10*2 only store rank value)
 */
public class Feature {
	/*
	 * parameters:
	 * @board is the whole board position
	 * @redTurn indicates those turn it is, which can't be extracted from board
	 * 
	 *  return a double-valued list with size three
	 *  each list stores features in the main categories
	 */
	public static List<List<Double>> featureExtractor(Game game) {
		List<List<Double>> features = new ArrayList<List<Double>>();
		features.add(globalFeature(game));
		features.add(pieceFeature(game));
		features.add(positionFeature(game));
		return features;
	}
	
	/*
	 * extract global feature
	 */
	private static List<Double> globalFeature(Game game) {
		List<Double> globalF = new ArrayList<Double>();
		boolean redTurn = game.isRedTurn();
		Piece[][] board = game.getPieces();
		
		// which side to move, 1 for red, 0 for black
		if (redTurn) globalF.add(new Double(0));
		else globalF.add(new Double(1));
		
		/*
		 *  total number of each type of piece on each side except JIANG
		 */
		int[][] num = new int[2][6];
		for (int i = 0; i < Game.ROW; i++) {
			for (int j = 0; j < Game.COLUMN; j++) {
				Piece p = board[i][j];
				if (p != null && !p.getName().equals(PieceName.JIANG)) {
					num[p.getColor().ordinal()][p.getName().ordinal()]++;
				}
			}
		}
		
		for (int i = 0; i < 2; i++) {
			for (int j = 0; j < 6; j++) {
				globalF.add(new Double(num[i][j]));
			}
		}
		
		return globalF;
	}
	
	/*
	 * extract chess piece centric feature
	 */
	private static List<Double> pieceFeature(Game game) {
		Piece[][] board = game.getPieces();
		/*
		 *  each slot has five features:
		 *  presence, x, y, attacker, defender
		 */
		int[] starter = {0, 10, 20, 30, 55, 65, 75, 0, 0, 0, 0, 0, 0, 0};
		int offset = 80;
		for (int i = 7; i < starter.length; i++)
			starter[i] = offset + starter[i-7];
		
		final int featSize = 160;
		double[] feat = new double[featSize];
		
		for (int i = 0; i < Game.ROW; i++) {
			for (int j = 0; j < Game.COLUMN; j++) {
				Piece p = board[i][j];
				if (p != null) {
					int pointer = p.getName().ordinal();
					Colors c = p.getColor(); 
					if (c.equals(Colors.BLACK))
						pointer += starter.length / 2;
					
					int index = starter[pointer];
					feat[index] = 1; // it is present
					double[] normPos = normalizeCoord(i, j);
					feat[index+1] = normPos[0];
					feat[index+2] = normPos[1];
					
					// attack/defend map
					BoardPosition bp = new BoardPosition(i, j);
					feat[index+3] = attackMap(game, bp, c);
					feat[index+4] = defendMap(game, bp, c);
					starter[pointer] += 5;
				}
			}
		}
		
		/*
		 *  check existence bit, if 0, set normalized x-, y- coordinates
		 *  and attack/defend mapper as -1
		 */
		List<Double> pieceF = new ArrayList<Double>();
		for (int i =0; i < featSize; i++) {
			pieceF.add(new Double(feat[i]));
			if (i % 5 == 0 && feat[i] == 0) {
				feat[i+1] = -1;
				feat[i+2] = -1;
				feat[i+3] = -1;
				feat[i+4] = -1;
			}
		}

		return pieceF;
	}
	
	/*
	 * normalize position coordinate to range [-1, 1]
	 */
	private static double[] normalizeCoord(int row, int col) {
		double upper = 1.0, lower = 0.0;
		double[] normed = new double[2];
		normed[0] = (upper - lower) * row / (Game.ROW - 1) + lower;
		normed[1] = (upper - lower) * col / (Game.COLUMN - 1) + lower;
		return normed;
	}
	
	/*
	 * find all opponent piece that can attack position @bp 
	 * and return the rank of the lowest ranked one
	 */
	public static int attackMap(Game game, BoardPosition bp, Colors side) {
		int lowest = 1000;
		for (int i = 0; i < Game.ROW; i++) {
			for (int j = 0; j < Game.COLUMN; j++) {
				Piece p = game.getPiece(i, j);
				
				if (p != null && !p.getColor().equals(side)) {
					Game g = game.copy();
					boolean success = g.movePiece(new BoardPosition(i, j), bp);
					
					if (success && lowest > p.getRank()) {
						lowest = p.getRank();
					}
				}
			}
		}
		
		if (lowest == 1000)
			lowest = 0;
		return lowest;
	}
	
	/*
	 * find all other pieces of the same side that can defend the piece at 
	 * position @bp and return the lowest ranked value
	 */
	public static int defendMap(Game game, BoardPosition bp, Colors side) {
		int lowest = 1000;
		for (int i = 0; i < Game.ROW; i++) {
			for (int j = 0; j < Game.COLUMN; j++) {
				Piece p = game.getPiece(i, j);
				
				if (p != null && p.getColor().equals(side)) {
					Game g = game.copy();
					g.getPiece(bp.Row(), bp.Col()).changeColor();
					List<BoardPosition> moves = p.getLegalMoves(g.getPieces(), new BoardPosition(i, j));
					g.getPiece(bp.Row(), bp.Col()).changeColor();
					
					boolean success = false;
					for (BoardPosition pos : moves) {
						if (pos.equals(bp)) {
							success = true;
							break;
						}
					}
					
					if (success && lowest > p.getRank()) {
						lowest = p.getRank();
					}
				}
			}
		}
		
		if (lowest == 1000)
			lowest = 0;
		return lowest;
	}

	/*
	 * extract board position centric feature
	 */
	private static List<Double> positionFeature(Game game) {
		List<Double> positionF = new ArrayList<Double>();
		return positionF;
	}
}
