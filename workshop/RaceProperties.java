package workshop;

import java.util.Set;

/**
 * 
 * Represents the properties of a single race in the elections.
 * It is constructed once in the initialization part, when it is decided how much races, candidates per race etc. 
 * 
 */

public class RaceProperties {
	private Set<String> possibleCandidates; // set of all the candidates names running in this race
	private String nameOfRace; // name of this race ("prime minister", "minister" etc.)
	private boolean isOrdered; // is the order of the chosen candidates relevant
	private int numOfSlots; // how much candidates are supposed to be chosen in this race
	
	public RaceProperties(Set<String> possibleCandidates, String nameOfRace, int numOfPossibleCan, boolean isOrdered){
		this.possibleCandidates = possibleCandidates;
		this.nameOfRace = nameOfRace;
		this.numOfSlots = numOfPossibleCan;
		this.isOrdered = isOrdered;
	}
	
	public Set<String> getPossibleCandidates() {
		return possibleCandidates;
	}
	
	
	public String getNameOfRace() {
		return nameOfRace;
	}
	
	public int getNumOfSlots() {
		return numOfSlots;
	}

	public boolean isOrdered() {
		return isOrdered;
	}

		
}
