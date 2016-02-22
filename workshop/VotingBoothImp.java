package workshop;

import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Set;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.print.PageFormat;
import java.awt.print.Printable;
import java.awt.print.PrinterException;
import java.awt.print.PrinterJob;

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

	private int topECCLevel; // top QR error correction level
	private int bottomECCLevel; // bottom QR error correction level

	/**
	 * processes the choice of the voter, encrypt it and prints the ballot.
	 * 
	 * @param jsonRepr
	 *            represents the current choice of the voter in all the races in
	 *            the elections. it is a JSON string format.
	 */
	public void vote(JSONArray jsonRepr) {
		StringBuilder sbCiphertext = new StringBuilder();
		StringBuilder sbRandomness = new StringBuilder();
		byte[][] encryptResult;
		try {
			curVote = parseJSON(jsonRepr); // parse the JSONArray to get info
											// about the vote
		} catch (JSONException e) {
			System.err
					.println("An error occured during the parse of JSONArray.");
		}

		// encrypt the vote
		for (Race race : curVote) {
			for (String name : race.getVotesArray()) {
				encryptResult = Parameters.cryptoClient.encryptGroupMember(
						Parameters.publicKey,
						Parameters.candidatesMap.get(name));
				sbCiphertext.append(Arrays.toString(encryptResult[0]));
				sbRandomness.append(Arrays.toString(encryptResult[1]));
			}
		}
		sbCiphertext.append(addSignatureAndTimeStamp());
		ciphertext = sbCiphertext.toString();
		auditRandomness = sbRandomness.toString();
		File topQr = topQRCreator(ciphertext);
		printPage(topQr, curVote);
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
		ArrayList<Race> result = new ArrayList<Race>();
		int raceNum = 0;
		JSONObject curRace;
		String curCand;
		for (RaceProperties rp : Parameters.racesProperties) {
			curRace = jsonRepr.getJSONObject(raceNum);
			result.add(raceNum, new Race(rp));
			Set<String> validNames = rp.getPossibleCandidates();
			String[] curRaceArrayOfNames = new String[rp.getNumOfSlots()];
			for (int i = 0; i < rp.getNumOfSlots(); i++) {
				try {
					curCand = curRace.getJSONArray("chosenCandidates").get(i)
							.toString();
				} catch (Exception ClassCastException) {
					throw (new ClassCastException());
				}
				if (validNames.contains(curCand)) {
					curRaceArrayOfNames[i] = curCand;
				}
			}
			result.get(raceNum).setVotesArray(curRaceArrayOfNames);
		}
		return result;
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

 	/**
 	* prints the ballot.
 	* 
 	* @param votesInAllRaces
 	*            . a list of Race objects contains the names of candidates to
 	*            be chosen in each race.
 	* @param qrPng QR png file.
 	*/
	private void printPage(File qrPng, ArrayList<Race> votesInAllRaces) {
		class Ballot extends JPanel implements printable{
		
			public Ballot(){
				GridLayout mainLayout = new GridLAyout(4,1);
				JPanel emptyPanel, tapePanel, votePanel, qrPanel;
			
				int votePanelH = 54, width = 242, rows = 0;
			
				emptyPanel = new JPanel(); votePanel = new JPanel();
				tapePanel = new JPanel(); qrPanel = new JPanel();
			  	this.setSize(new Dimension(312,242));
				emptyPanel.setSize(new Dimension(54,242));
				votePanel.setSize(new Dimension(54,242));
				tapePanel.setSize(new Dimension(13,242));
				qrPanel.setSize(new Dimension(191,242));
				
				for(Race r : votesInAllRaces){
					if(r.curRaceProp.numOfPossibleCan >= lines)
						rows = r.curRaceProp.numOfPossibleCan;
				}
			
			
				GridLayout votePanelLayout = new GridLayout(1,votesInAllRaces.size());
				for(Race r : votesInAllRaces){
					JPanel p = new JPanel();
					p.setSize(new Dimension(votePanelH,width/votesInAllRaces.size());
				
					GridLayout l = new GridLayout(rows+1,1);
					JTextField raceName = new JTextfield(r.curRacePror.nameOfRace);
					l.add(raceName);
					for(String s:r.votesArray){
						JTextField jtf = new JTextfield(s);
						l.add(jtf);								
					}
					p.setLayout(l);
				
					votePanelLayout.add(p);
				}
				votePanel.setLayout(votePanelLayout);
			
				ImageIcon qrIcon = new ImageIcon(qrPng.getAbsolutePath());
				JLabel imagelabel = new JLabel(qrIcon, JLabel.CENTER);
				qrPanel.add(JLabel,JPanel.CENTER);
			
				mainLayout.add(emptyPanel); mainLayout.add(votePanel);
				mainLayout.add(tapePanel); mainLayout.add(qrPanel);
			
				this.setLayout(mainLayou);	
				this.setVisible(false);
			}
		
		
		@Override
	    	public int print(Graphics g, PageFormat pf, int i) throws PrinterException {
	        	if (i > 0) {
	            	return NO_SUCH_PAGE;
	        	}
	        	Graphics2D g2d = (Graphics2D) g;
	        	g2d.translate(pf.getImageableX(), pf.getImageableY());
	        	Ballot.this.printAll(g);
	        	return Printable.PAGE_EXISTS;
	    	}
		}
		//end of class
	
		Ballot b = new Ballot();		
		PrinterJob pj = PrinterJob.getPrinterJob();
    		PageFormat pf = pj.pageDialog(pj.defaultPage());
    		pj.setPrintable(b,pf);
   		try {
        	pj.print();
    		} catch (PrinterException pe) {
        	pe.printStackTrace(System.err);
    		} 
	}

}
