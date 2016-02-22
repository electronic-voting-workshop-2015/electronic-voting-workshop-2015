package workshop;
import org.json.*;


public interface VotingBooth {
	
	/**
	 * processes the choice of the voter, encrypt it and prints the ballot.
	 * @param jsonRepr represents the choice of the voter in all the races in the elections. it is a JSON string format.
	 */
	public void vote(JSONObject jsonRepr);
	
	/**
	 * 
	 * @param isAudit represents whether the voter wanted to audit.
	 * 		if true, prints the rest of the voting paper and terminates the voting booth state.
	 * 		otherwise, terminates the voting booth state.
	 */
	public void audit(boolean isAudit);
		
}
