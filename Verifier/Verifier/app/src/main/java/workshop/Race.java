package workshop;

/**
 * Represents a vote in a single race in the elections.
 */
public class Race {
    private RaceProperties curRaceProp; // the properties of this race
    private String[] votesArray; // array of the names of the candidates chosen in this particular race by the voter

    public Race(RaceProperties curRaceProp) {
        this.curRaceProp = curRaceProp;
        this.setVotesArray(new String[this.curRaceProp.getNumOfSlots()]);
    }

    public String[] getVotesArray() {
        return votesArray;
    }

    public RaceProperties getCurRaceProp() {
        return curRaceProp;
    }

    public void setVotesArray(String[] votesArray) {
        this.votesArray = votesArray;
    }
}
