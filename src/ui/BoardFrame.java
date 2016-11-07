package ui;

import java.io.PrintWriter;

import javax.swing.JFrame;

import util.FileHandler;

public class BoardFrame extends JFrame {
    public BoardFrame(String label) {
        super(label);
    }
    
    
    @Override
    public void dispose()
    {
        // output the chess game record when click on EXIT
        String fileName = "src/data/record.txt";
        String content = Board.outputGameRecord();
        PrintWriter writer = FileHandler.write(fileName);
        if (writer != null) {
            writer.println(content);
        }
        writer.close();
        super.dispose();
    }
}
