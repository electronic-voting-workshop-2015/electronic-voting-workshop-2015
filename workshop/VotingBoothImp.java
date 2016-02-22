package workshop;

import java.io.File;
import java.util.ArrayList;
import java.util.Set;

import org.json.*;

public class VotingBoothImp implements VotingBooth {

	private String ciphertext; // the ciphertext to be printed as a QR.
								// Contains: concatenated encryptions of the
								// chosen candidates names, machine signature,
								// timestamp.
	private String auditRandomness; // the randomnesses to be printed as an
									// Audit QR. Contains: concatenated
									// randomnesses which was used in the
									// encryptions.
	private ArrayList<Race> curVote; // the list of Race objects, representing
										// the votes of the voter in each race.

	/**
	 * processes the choice of the voter, encrypt it and prints the ballot.
	 * 
	 * @param jsonRepr
	 *            represents the current choice of the voter in all the races in
	 *            the elections. it is a JSON string format.
	 */
	public void vote(JSONArray jsonRepr) {
		// TODO Auto-generated method stub

	}

	/**
	 * 
	 * @param isAudit
	 *            represents whether the voter wanted to audit. if true, prints
	 *            the rest of the voting paper and terminates the voting booth
	 *            state. otherwise, terminates the voting booth state.
	 */
	public void audit(boolean isAudit) {
		// TODO Auto-generated method stub
	}

	/**
	 * 
	 * @param jsonRepr
	 *            represents the current choice of the voter in all the races in
	 *            the elections. it is a JSON string format.
	 * @return list of Race objects, representing the votes of the voter in each
	 *         race, parsed from the input JSON.
	 */
	private ArrayList<Race> parseJSON(JSONArray jsonRepr) throws JSONException {
		ArrayList<Race> curVote= new ArrayList<Race>();
		int raceNum=0;
		JSONObject curRace;	
		String curCand;
		for(RaceProperties rp : Parameters.racesProperties){
			try{
				curRace=jsonRepr.getJSONObject(raceNum);
			}
			catch(Exception ClassCastException){
				throw(new ClassCastException());
			}								
			curVote.add(raceNum, new Race(rp));
			Set<String> validNames=rp.getPossibleCandidates();
			String[] curRaceArrayOfNames= new String[rp.getNumOfSlots()];
			for(int i=0;i<rp.getNumOfSlots();i++){
				try{
					curCand=curRace.getJSONArray("chosenCandidates").get(i).toString();
				}
				catch(Exception ClassCastException){
					throw(new ClassCastException());
				}
				if(validNames.contains(curCand)){
					curRaceArrayOfNames[i]=curCand;
				}				
			}
			curVote.get(raceNum).setVotesArray(curRaceArrayOfNames);
		}
		return curVote;		
	}

	/**
	 * prepares the machine signature of the current vote, by calling the Sign
	 * method. Prepares the current timestamp.
	 * 
	 * @return a string which is a concatenation of the signature and timestamp.
	 */
	private String addSignatureAndTimeStamp() {
		return null;
		// TODO Auto-generated method stub
	}

	/**
	 * Creates the top QR
	 * 
	 * @param ciphertext
	 *            . a string represents the ciphertext to be encoded as a QR.
	 * @return QR png file
	 */
	private File topQRCreator(String ciphertext) {
		return null;
		// TODO Auto-generated method stub

	}

	/**
	 * Creates the bottom QR
	 * 
	 * @param randomness
	 *            . a string represents the concatenated randomnesses to be
	 *            encoded as a Small QR.
	 * @return QR png file
	 */
	private File bottomQRCreator(String randomness) {
		return null;
		// TODO Auto-generated method stub

	}

	/**
	 * prints the ballot.
	 * 
	 * @param votesInAllRaces
	 *            . a list of Race objects contains the names of candidates to
	 *            be chosen in each race.
	 */

	private void print(ArrayList<Race> votesInAllRaces) {
		// TODO Auto-generated method stub
	}

}
