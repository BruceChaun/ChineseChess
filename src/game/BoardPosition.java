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
    
    @Override
    public String toString() {
        return this.col + "" + this.row;
    }

    public static BoardPosition[] fromString(String move) {
        BoardPosition from = new BoardPosition(
                Integer.parseInt(move.substring(1, 2)), Integer.parseInt(move.substring(0, 1)));
        BoardPosition to = new BoardPosition(
                Integer.parseInt(move.substring(3, 4)), Integer.parseInt(move.substring(2, 3)));
        return new BoardPosition[]{from ,to};
    }

    /*
     * calculate Manhattan distance from this to that
     */
    public BoardPosition distance(BoardPosition that) {
        return new BoardPosition(that.Row() - this.Row(), that.Col() - this.Col());
    }
}
