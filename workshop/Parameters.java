package workshop;

import java.util.*;

/**
 * The fixed parameters file, to be edited by all teams
 * The initial system parameters, which are chosen by the initializer before the process starts
 * Needs to be initialized once(!) before the actual elections.
 */
public class Parameters {
	//The group we use for encrypting the votes
	public static Group ourGroup;
	//The cipher-text QR version
	public static int topQRLevel;
	//The audit QR version
	public static int bottomQRLevel;
	//Set of the names of all the candidates in these elections - for the mapping.
	public static HashSet<String> candidatesNames;
	//List of objects of type RaceProperties, which contains the properties on each race of the elections.
	// (important mainly for those who read the QR)
	public static ArrayList<RaceProperties> racesProperties;
	//The mapping between candidates and group elements
	public static HashMap<String, byte[]> candidatesMap = mapCandidates(candidatesNames);
	//The time-stamp accuracy level, either 1 or 2, 1 for HH:MM format, 2 for HH:MM:SS format
	public static int timeStampLevel;
	
	/**
	 * Maps the candidates to group elements, and update the corresponding field
	 * @param candidates
	 */
	public static HashMap<String, byte[]> mapCandidates(HashSet<String> candidates) {
		HashMap<String, byte[]> result = new HashMap<String, byte[]>();
		HashMap<String, Integer> tempMap = new HashMap<String, Integer>();
		int n = 0;
		for (String name : candidates){
			tempMap.put(name, n++);
		}
		HashMap<Integer, byte[]> mapToGroupElem = new HashMap<Integer, byte[]>(); // instead " = funcThatReturnsTheMapping() "
		for (String name : candidates){
			result.put(name, mapToGroupElem.get(tempMap.get(name)));
		}
		return result;
	}	
	
	public static void setParameters(Group ourGroup1, int topQRLevel1, int bottomQRLevel1, HashSet<String> candidatesNames1, ArrayList<RaceProperties> racesProperties1, int timeStampLevel1){
		ourGroup = ourGroup1;
		topQRLevel = topQRLevel1;
		bottomQRLevel = bottomQRLevel1;
		candidatesNames = candidatesNames1;
		racesProperties = racesProperties1;
		timeStampLevel = timeStampLevel1;
	}
}
