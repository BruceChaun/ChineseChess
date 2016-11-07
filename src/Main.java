import javax.swing.*;

import ui.Board;
import ui.BoardFrame;

public class Main {
	public static void main(String[] args) {
		BoardFrame f = new BoardFrame("Chinese Chess");
		f.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
		Board b = new Board();
		f.add(b);
		f.setSize(700,  700);
		f.setVisible(true);
	}
	
}
