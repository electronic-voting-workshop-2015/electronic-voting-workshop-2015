package workshop;
import java.util.HashMap;

/**
 * The fixed parameters file, to be edited by all teams
 * The initial system parameters, which are chosen by the initializer before the process starts
 */
public class Parameters {
	//The group we use for encrypting the votes
	public static Group ourGroup;
	//The cipher-text QR version
	public static int topQRLevel;
	//The audit QR version
	public static int bottomQRLevel;
	//The number of total candidates
	public static int candidatesNumber;
	//The mapping between candidates and group elements
	public static HashMap<String, byte[]> candidatesMap;
	//The time-stamp accuracy level, either 1 or 2, 1 for HH:MM format, 2 for HH:MM:SS format
	public static int timeStampLevel;
	
	/**
	 * Maps the candidates to group elements, and update the corresponding field
	 * @param candidates
	 */
	void mapCandidates(String[] candidates) {
	}			
}
