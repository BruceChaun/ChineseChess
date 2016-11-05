package ui;

import java.awt.*;
import java.awt.geom.*;
import javax.swing.*;
import java.awt.event.*;

import pieces.*;
import constants.*;
import game.BoardPosition;
import game.Game;

public class Board extends JPanel implements MouseListener, MouseMotionListener {
	/*
	 *   Some constants
	 */
	private final int TOP_MARGIN = 50;
	private final int LEFT_MARGIN = 100;
	private final int GAP = 60;
	
	/*
	 *   private variables to maintain the board information
	 */
	private Game game;
	int x = 0, y = 0;                                        // capture the coordinate of mouse clicking
	int lastRowPosition, lastColPosition;
	
	@Override
	public void paintComponent(Graphics g) {
		super.paintComponent(g);
		this.addMouseListener(this);
		this.addMouseMotionListener(this);
		
		if (game.getWinner() == null) {
			BoardPosition pos = coord2pos(x, y);
			updateLastPosition(pos, g);
		}
		drawBoard(g);
		drawChess(g);
		if (lastRowPosition != -1 && lastColPosition != -1)
			drawCursor(lastRowPosition, lastColPosition, g);
		
		if (game.getWinner() != null) {
			String s = game.getWinner() + "  WINS!!!";
			g.setFont(new Font("TimesRoman", Font.PLAIN, 75));
			g.setColor(Color.RED);
			g.drawString(s, LEFT_MARGIN,  TOP_MARGIN + GAP * Game.ROW / 2);
		}
		
		// show whose turn
		int yPos = 0;
		if (game.isRedTurn()) {
			g.setColor(Color.RED);
			yPos = TOP_MARGIN + GAP * (Game.COLUMN-2);
		} else {
			g.setColor(Color.BLACK);
			yPos = TOP_MARGIN + GAP * 2;
		}
		
		g.fillRect(LEFT_MARGIN - GAP*3/2, yPos, GAP / 2, GAP / 2);
	}
	
	public Board() {
		this.game = new Game();
		String record = "262512227747604219076364796770628979807079751002091900010605013107155041150322216665646575656254394831356563726269873525474362676367544667374627171270731222734319172535172735372737210105042324371701030403";
		game.initBoard(record);
		lastRowPosition = -1; 
		lastColPosition = -1;
	}
	
	/*
	 *   Draw the chess board
	 */
	private void drawBoard(Graphics g) {
		// board background
		//g.setColor(new Color(204, 102, 0));
		g.setColor(Color.WHITE);
		g.fillRect(LEFT_MARGIN - GAP / 2, TOP_MARGIN - GAP / 2, Game.COLUMN * GAP, Game.ROW * GAP);
		
		// vertical and horizontal lines
		g.setColor(Color.BLACK);
		int offset = GAP * (Game.COLUMN-1);
		for (int i = 0; i < Game.ROW; i++) {
			int y = TOP_MARGIN + i * GAP;
			((Graphics2D) g).draw(new Line2D.Float(LEFT_MARGIN, y, LEFT_MARGIN + offset, y));
		}
		
		offset = GAP * (Game.ROW - 1);
		((Graphics2D) g).draw(new Line2D.Float(LEFT_MARGIN, TOP_MARGIN, LEFT_MARGIN, TOP_MARGIN + offset));
		int x = LEFT_MARGIN + GAP * (Game.COLUMN - 1);
		((Graphics2D) g).draw(new Line2D.Float(x, TOP_MARGIN, x, TOP_MARGIN + offset));
		
		offset = GAP * (Game.ROW/2 - 1);
		int y = TOP_MARGIN + GAP * Game.ROW / 2;
		for (int i = 1; i < Game.COLUMN - 1; i++) {
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
		
		for (int i = 0; i < Game.COLUMN; i+=2) {
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
		
		if (c != Game.COLUMN - 1) {
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
	
	private void drawChess(Graphics g) {
		int a = GAP / 2 - 1;
		int size = 2 * a;
		for (int i = 0; i < Game.ROW; i++) {
			for (int j = 0; j < Game.COLUMN; j++) {
				Piece p = game.getPiece(i, j); 
				if (p != null) {
					Image pieceImage = 
							new ImageIcon(this.getClass().getResource(p.getImage())).getImage();
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
	private BoardPosition coord2pos(int x, int y) {
		int col = x + GAP / 2 - LEFT_MARGIN;
		if (col < 0) return new BoardPosition(-1, -1);
		int row = y + GAP / 2 - TOP_MARGIN;
		if (row < 0) return new BoardPosition(-1, -1);
		col /= GAP;
		row /= GAP;
		if (row >= Game.ROW || col >= Game.COLUMN)
			return new BoardPosition(-1, -1);
		return new BoardPosition(row, col);
	}
	
	/*
	 *   Try to update last row and column positions if it is the first stage to move, 
	 *   if it is the second stage to move, try to move to that place.
	 */
	private void updateLastPosition(BoardPosition bp, Graphics g) {
		int row = bp.Row(), col = bp.Col();
		// must click in the range of board
		if (row != -1 && col != -1) {
			// if it is the first stage to move
			if (lastRowPosition == -1 && lastColPosition == -1) {
				// must click on the region where there is a piece
				Piece p = game.getPiece(row, col);
				if (p != null) {
					// must click on the red piece if red turn, otherwise must click on the black one
					boolean redTurn = game.isRedTurn();
					if ((redTurn && p.getColor().equals(Colors.RED)) 
							|| (!redTurn && p.getColor().equals(Colors.BLACK))) {
						lastRowPosition = row;
						lastColPosition = col;
					}
				}
			} else {
				// if the last piece is touched
				if (row == lastRowPosition && col == lastColPosition) {
					lastRowPosition = -1;
					lastColPosition = -1;
				} else {
					// if it is the second stage to move, then try to move to that place
					BoardPosition last = new BoardPosition(lastRowPosition, lastColPosition);
					boolean success = game.movePiece(last, bp);
					if (success) {
						lastRowPosition = -1;
						lastColPosition = -1;
					}
				}
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
