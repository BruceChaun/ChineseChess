package game;

public class Recorder {
    private String record;
    
    public Recorder() {record = "";}
    public Recorder(String record) {
        this.record = record;
        }
    
    public void record(BoardPosition from, BoardPosition to) {
        record += from.Col() + "" + from.Row() + "" + to.Col() + "" + to.Row();
    }
    
    public String output() {
        return record;
    }
    
    public Recorder copy() {
        return new Recorder(this.output());
    }
}
