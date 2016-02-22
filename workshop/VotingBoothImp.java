package workshop;

import java.io.File;
import java.util.ArrayList;

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
	public void vote(JSONObject jsonRepr) {
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
	private ArrayList<Race> parseJSON(JSONObject jsonRepr) {
		return null;
		// TODO Auto-generated method stub
	}

	/**
	 * prepares the machine signature of the current vote, by calling the Sign
	 * method. Prepares the current timestamp.
	 * 
	 * @return a string which is a concatenation of the signature and timestamp.
	 */
	private String addSignatureAndTimeStamp(String str) {
		return null;
		// TODO Auto-generated method stub
	}

	/**
	 * sets the desired Top QR parameters, Version , ECC level, number of bits
	 * to be decoded
	 * 
	 * @param ciphertext
	 *            . a string represents the ciphertext to be encoded as a QR.
	 * @return QR png file
	 */
	private File calcTopQRParameters(String ciphertext) {
		return null;
		// TODO Auto-generated method stub

	}

	/**
	 * sets the desired Bottom QR parameters, Version , ECC level, number of
	 * bits to be decoded
	 * 
	 * @param randomness
	 *            . a string represents the concatenated randomnesses to be
	 *            encoded as a Small QR.
	 * @return QR png file
	 */
	private File calcBottomQRParameters(String randomness) {
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
