package game;

public class Recorder {
    private String record;
    
    public Recorder() {record = "";}
    public Recorder(String record) {
        this.record = record;
    }
    
    public void record(BoardPosition from, BoardPosition to) {
        record += from.toString() + to.toString();
    }
    
    public String retrieve() {
        return record;
    }
    
    public Recorder copy() {
        return new Recorder(this.record);
    }
}
