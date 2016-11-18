package ui;

import java.io.FileWriter;
import java.io.IOException;

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
        FileWriter writer = FileHandler.write(fileName, true);
        
        if (writer != null && content.length() > 0) {
            try {
                writer.write(content + "\n");
                writer.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        
        super.dispose();
    }
}
