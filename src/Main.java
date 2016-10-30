import java.awt.*;
import javax.swing.*;

import ui.Board;

public class Main {
	public static void main(String[] args) {
		JFrame f = new JFrame("Chinese Chess");
		f.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		Board b = new Board();
		f.add(b);
		f.setSize(700,  700);
		f.setVisible(true);
	}
	
}
