package ui;

import java.awt.*;
import java.awt.geom.*;
import javax.swing.*;
import java.awt.event.*;

import pieces.*;
import constants.*;

public class Board extends JPanel implements MouseListener, MouseMotionListener {
	/*
	 *   Some constants
	 */
	private final int TOP_MARGIN = 50;
	private final int LEFT_MARGIN = 100;
	private final int GAP = 60;

	public static final int ROW = 10;
	public static final int COLUMN = 9;
	
	/*
	 *   private variables to maintain the board information
	 */
	private Piece[][] pieces;
	private boolean redTurn = true; // red goes first
	int x = 0, y = 0;                                        // capture the coordinate of mouse clicking
	int lastRowPosition, lastColPosition;
	Colors winner;
	
	@Override
	public void paintComponent(Graphics g) {
		super.paintComponent(g);
		this.addMouseListener(this);
		this.addMouseMotionListener(this);
		
		if (winner == null) {
			int[] pos = coord2pos(x, y);
			int row = pos[0], col = pos[1];
			updateLastPosition(row, col, g);
		}
		drawBoard(g);
		drawChess(g);
		if (lastRowPosition != -1 && lastColPosition != -1)
			drawCursor(lastRowPosition, lastColPosition, g);
		
		if (winner != null) {
			String s = this.winner + "  WINS!!!";
			g.setFont(new Font("TimesRoman", Font.PLAIN, 75));
			g.setColor(Color.RED);
			g.drawString(s, LEFT_MARGIN,  TOP_MARGIN + GAP * ROW / 2);
		}
	}
	
	public Board() {
		initBoard();
	}
	
	/*
	 *   Draw the chess board
	 */
	private void drawBoard(Graphics g) {
		// board background
		//g.setColor(new Color(204, 102, 0));
		g.setColor(Color.WHITE);
		g.fillRect(LEFT_MARGIN - GAP / 2, TOP_MARGIN - GAP / 2, COLUMN * GAP, ROW * GAP);
		
		// vertical and horizontal lines
		g.setColor(Color.BLACK);
		int offset = GAP * (COLUMN-1);
		for (int i = 0; i < ROW; i++) {
			int y = TOP_MARGIN + i * GAP;
			((Graphics2D) g).draw(new Line2D.Float(LEFT_MARGIN, y, LEFT_MARGIN + offset, y));
		}
		
		offset = GAP * (ROW - 1);
		((Graphics2D) g).draw(new Line2D.Float(LEFT_MARGIN, TOP_MARGIN, LEFT_MARGIN, TOP_MARGIN + offset));
		int x = LEFT_MARGIN + GAP * (COLUMN - 1);
		((Graphics2D) g).draw(new Line2D.Float(x, TOP_MARGIN, x, TOP_MARGIN + offset));
		
		offset = GAP * (ROW/2 - 1);
		int y = TOP_MARGIN + GAP * ROW / 2;
		for (int i = 1; i < COLUMN - 1; i++) {
			x = LEFT_MARGIN + i * GAP;
			((Graphics2D) g).draw(new Line2D.Float(x, TOP_MARGIN, x, TOP_MARGIN + offset));
			((Graphics2D) g).draw(new Line2D.Float(x, y, x, y + offset));
		}
		
		// other details
		((Graphics2D) g).draw(new Line2D.Float(
				 LEFT_MARGIN + GAP * 3, TOP_MARGIN, 
				 LEFT_MARGIN + GAP * 5, TOP_MARGIN + GAP * 2));
		((Graphics2D) g).draw(new Line2D.Float(
				LEFT_MARGIN + GAP * 3, TOP_MARGIN + GAP * 2,  
				LEFT_MARGIN + GAP * 5, TOP_MARGIN));
		((Graphics2D) g).draw(new Line2D.Float(
				LEFT_MARGIN + GAP * 3, TOP_MARGIN + GAP * 7,  
				LEFT_MARGIN + GAP * 5,  TOP_MARGIN + GAP * 9));
		((Graphics2D) g).draw(new Line2D.Float(
				LEFT_MARGIN + GAP * 3, TOP_MARGIN + GAP * 9,  
				LEFT_MARGIN + GAP * 5,  TOP_MARGIN + GAP * 7));
		
		for (int i = 0; i < COLUMN; i+=2) {
			drawMarks(g, 3, i);
			drawMarks(g, 6, i);
		}
		drawMarks(g, 2, 1);
		drawMarks(g, 2, 7);
		drawMarks(g, 7, 1);
		drawMarks(g, 7, 7);
	}
	
	private void drawMarks(Graphics g, int r, int c) {
		int x = LEFT_MARGIN + GAP * c;
		int y = TOP_MARGIN + GAP * r;
		int eps = GAP / 10;
		int offset = GAP / 4;
		
		if (c != COLUMN - 1) {
			((Graphics2D) g).draw(new Line2D.Float(x + eps, y + eps, x + offset, y + eps));
			((Graphics2D) g).draw(new Line2D.Float(x + eps, y + eps, x + eps, y + offset));
			((Graphics2D) g).draw(new Line2D.Float(x + eps, y - eps, x + offset, y - eps));
			((Graphics2D) g).draw(new Line2D.Float(x + eps, y - eps, x + eps, y - offset));
		}
		
		if (c != 0) {
			((Graphics2D) g).draw(new Line2D.Float(x - eps, y + eps, x - offset, y + eps));
			((Graphics2D) g).draw(new Line2D.Float(x - eps, y + eps, x - eps, y + offset));
			((Graphics2D) g).draw(new Line2D.Float(x - eps, y - eps, x - offset, y - eps));
			((Graphics2D) g).draw(new Line2D.Float(x - eps, y - eps, x - eps, y - offset));
		}
	}
	
	/*
	 *   initialize the chess board layout, i.e. place the chess pieces.
	 *   
	 *   The black pieces are always at the upper part of the board while
	 *   the red ones are at the lower part.
	 */
	private void initBoard() {
		this.pieces = new Piece[ROW][COLUMN];
		this.lastRowPosition = -1;
		this.lastColPosition = -1;
		this.winner = null;
		
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
	
	private void drawChess(Graphics g) {
		int a = GAP / 2 - 1;
		int size = 2 * a;
		for (int i = 0; i < ROW; i++) {
			for (int j = 0; j < COLUMN; j++) {
				if (pieces[i][j] != null) {
					Image pieceImage = 
							new ImageIcon(this.getClass().getResource(pieces[i][j].getImage())).getImage();
					int x = LEFT_MARGIN + GAP * j;
					int y = TOP_MARGIN + GAP * i;
					g.drawImage(pieceImage, x-a, y-a, size, size, this);
				}
			}
		}
	}

	/*
	 *   convert interface coordinate to chess board position
	 */
	private int[] coord2pos(int x, int y) {
		int col = x + GAP / 2 - LEFT_MARGIN;
		if (col < 0) return new int[]{-1, -1};
		int row = y + GAP / 2 - TOP_MARGIN;
		if (row < 0) return new int[]{-1, -1};
		col /= GAP;
		row /= GAP;
		if (row >= ROW || col >= COLUMN)
			return new int[]{-1, -1};
		return new int[]{row, col};
	}
	
	/*
	 *   Try to update last row and column positions if it is the first stage to move, 
	 *   if it is the second stage to move, try to move to that place.
	 */
	private void updateLastPosition(int row, int col, Graphics g) {
		// must click in the range of board
		if (row != -1 && col != -1) {
			// if it is the first stage to move
			if (lastRowPosition == -1 && lastColPosition == -1) {
				// must click on the region where there is a piece
				if (pieces[row][col] != null) {
					// must click on the red piece if red turn, otherwise must click on the black one
					if ((redTurn && pieces[row][col].getColor().equals(Colors.RED)) 
							|| (!redTurn && pieces[row][col].getColor().equals(Colors.BLACK))) {
						lastRowPosition = row;
						lastColPosition = col;
					}
				}
			} else {
				// if it is the second stage to move, then try to move to that place
				movePiece(row, col);
			}
		}
	}
	
	// draw cursor to remind player which piece to move
	private void drawCursor(int row, int col, Graphics g) {
		if (lastRowPosition != -1 && lastColPosition != -1) {
			Graphics2D g2 = (Graphics2D) g;
			g2.setStroke(new BasicStroke(4));
			g2.setColor(Color.RED);
			int x = LEFT_MARGIN + GAP * col;
			int y = TOP_MARGIN + GAP * row;
			int offset = GAP / 2;
			int length = GAP / 4;
			g2.draw(new Line2D.Float(x - offset, y - offset, x - offset + length, y - offset));
			g2.draw(new Line2D.Float(x - offset, y - offset, x - offset, y - offset + length));
			g2.draw(new Line2D.Float(x + offset, y - offset, x + offset, y - offset + length));
			g2.draw(new Line2D.Float(x + offset, y - offset, x + offset - length, y - offset));
			g2.draw(new Line2D.Float(x + offset, y + offset, x + offset - length, y + offset));
			g2.draw(new Line2D.Float(x + offset, y + offset, x + offset, y + offset - length));
			g2.draw(new Line2D.Float(x - offset, y + offset, x - offset + length, y + offset));
			g2.draw(new Line2D.Float(x - offset, y + offset, x - offset, y + offset - length));
			
			g2.setStroke(new BasicStroke(1));
		}
	}
	
	/*
	 *   check the move of the second stage, move if valid, 
	 *   otherwise do nothing 
	 *   (or you can reset lastRowPosition and lastColPosition)
	 *   
	 *   parameters:
	 *   @row and @col are the destination position
	 */
	private void movePiece(int row, int col) {
		//if (!validMove(row, col)) return;
		
		// can move if there is no piece in that place
		if (pieces[row][col] == null) {
			pieces[row][col] = pieces[lastRowPosition][lastColPosition];
			pieces[lastRowPosition][lastColPosition] = null;
			lastRowPosition = -1;
			lastColPosition = -1;
			redTurn = !redTurn;
		} else {
			Colors from = pieces[lastRowPosition][lastColPosition].getColor();
			Colors to = pieces[row][col].getColor();
			if (!from.equals(to)) {
				// eat your enemy
				if (pieces[row][col].getName().equals(PieceName.JIANG)) {
					winner = pieces[lastRowPosition][lastColPosition].getColor();
				}
				
				pieces[row][col].eaten();
				pieces[row][col] = pieces[lastRowPosition][lastColPosition];
				pieces[lastRowPosition][lastColPosition] = null;
				lastRowPosition = -1;
				lastColPosition = -1;
				redTurn = !redTurn;
			}
		}
	}
	
	/*
	 *   Check whether the move is valid
	 *   
	 *   parameters:
	 *   @row and @col are the destination position
	 */
	private boolean validMove(int row, int col) {
		PieceName name = pieces[lastRowPosition][lastColPosition].getName();
		if (name.equals(PieceName.JIANG)) {
			return validJiangMove(row, col);
		} else if (name.equals(PieceName.BING)) {
			return validBingMove(row, col);
		} else if (name.equals(PieceName.JU)) {
			return validJuMove(row, col);
		} else if (name.equals(PieceName.MA)) {
			return validMaMove(row, col);
		} else if (name.equals(PieceName.PAO)) {
			return validPaoMove(row, col);
		} else if (name.equals(PieceName.SHI)) {
			return validShiMove(row, col);
		} else {
			return validXiangMove(row, col);
		}
	}
	
	private boolean validXiangMove(int row, int col) {
		// TODO Auto-generated method stub
		return false;
	}

	private boolean validShiMove(int row, int col) {
		// TODO Auto-generated method stub
		return false;
	}

	private boolean validPaoMove(int row, int col) {
		// TODO Auto-generated method stub
		return false;
	}

	private boolean validMaMove(int row, int col) {
		// TODO Auto-generated method stub
		return false;
	}

	private boolean validJuMove(int row, int col) {
		// TODO Auto-generated method stub
		return false;
	}

	private boolean validBingMove(int row, int col) {
		// TODO Auto-generated method stub
		return false;
	}

	private boolean validJiangMove(int row, int col) {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public void mouseDragged(MouseEvent e) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void mouseMoved(MouseEvent e) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void mouseClicked(MouseEvent e) {
		// TODO Auto-generated method stub
		x = e.getX();
		y = e.getY();
		repaint();
	}

	@Override
	public void mousePressed(MouseEvent e) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void mouseReleased(MouseEvent e) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void mouseEntered(MouseEvent e) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void mouseExited(MouseEvent e) {
		// TODO Auto-generated method stub
		
	}
}
