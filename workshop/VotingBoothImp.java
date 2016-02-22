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

	private static QRProperties topQR; //Valid vote QR properties
	private static QRProperties bottomQR;// Audit QR properties

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
	/**
	 * set the required QR Properties to the given data QR Properties
	 * @param qr
	 * @param data
	 */
	public static void setQR(QRProperties qr, QRProperties data){
		qr.setLevel(data.getLevel());
		qr.setEcc(data.getEcc());
		qr.setMaxNumOfBits(data.getMaxNumOfBits());
	}
	/**
	 * Our specific QR map, can be further expanded
	 * @return
	 */
	public static HashMap<Integer,Integer[]> ourQRMap(){
		HashMap<Integer,Integer[]> qrMap = new HashMap<Integer,Integer[]>();		
		Integer[] curSizes=new Integer[4];
		curSizes[0]=1091;curSizes[1]=857;curSizes[2]=611;curSizes[3]=461;
		qrMap.put(23, curSizes.clone());
		curSizes[0]=1171;curSizes[1]=911;curSizes[2]=661;curSizes[3]=511;
		qrMap.put(24, curSizes.clone());
		curSizes[0]=1273;curSizes[1]=997;curSizes[2]=715;curSizes[3]=535;
		qrMap.put(25, curSizes.clone());
		curSizes[0]=1367;curSizes[1]=1059;curSizes[2]=751;curSizes[3]=593;
		qrMap.put(26, curSizes.clone());
		curSizes[0]=1465;curSizes[1]=1125;curSizes[2]=805;curSizes[3]=625;
		qrMap.put(27, curSizes.clone());
		curSizes[0]=1528;curSizes[1]=1190;curSizes[2]=868;curSizes[3]=658;
		qrMap.put(28, curSizes.clone());
		curSizes[0]=1628;curSizes[1]=1264;curSizes[2]=908;curSizes[3]=698;
		qrMap.put(29, curSizes.clone());
		curSizes[0]=1732;curSizes[1]=1370;curSizes[2]=982;curSizes[3]=742;
		qrMap.put(30, curSizes.clone());
		curSizes[0]=1840;curSizes[1]=1452;curSizes[2]=1030;curSizes[3]=790;
		qrMap.put(31, curSizes.clone());
		curSizes[0]=1952;curSizes[1]=1538;curSizes[2]=1112;curSizes[3]=842;
		qrMap.put(32, curSizes.clone());
		curSizes[0]=2068;curSizes[1]=1628;curSizes[2]=1168;curSizes[3]=898;
		qrMap.put(33, curSizes.clone());	
		return qrMap;
	}
	
	/**
	 * Given size of group element in bytes, the max QR version allowed, required QR (Audit or Valid vote), the QR map (versions to DATA+ECC bytes allowance)
	 * Calculate the wanted QR parameters, which are error correction level, version, and maximal capacity in bits
	 * Maximizes and minimizes the ECC and QR version respectively 
	 * In our scheme, maximal version for top QR is 33, for bottom QR is 27
	 * @param sizeOfElemInBytes
	 * @param maxVersion
	 * @param isTop
	 * @param qrSpecs
	 * @return
	 */
	public static QRProperties calcQRsettings(int sizeOfElemInBytes, int maxVersion, boolean isTop, HashMap<Integer,Integer[]> qrSpecs){
		int totalNumOfElements=0;
		int totalLengthInBytes=0;
		ECCLevel ecc;
		int curQRLevel;					
		curQRLevel=maxVersion;
		ecc=ECCLevel.LOW;
		for(RaceProperties rc : Parameters.racesProperties){
			totalNumOfElements+=rc.getNumOfSlots();
		}
		//ElGamal outputs 2 elements for each element given in top QR, and randomness+message in bottom QR
		totalNumOfElements*=2;
		if(isTop){
			totalNumOfElements++;//One more element for the signature
			switch(Parameters.timeStampLevel){
			case(1)://HH:MM format, byte for each unit
				totalLengthInBytes+=2;
				break;		
			case(2)://HH:MM:SS format
				totalLengthInBytes+=3;
				break;
			default:
				break;
			}
		}							
		totalLengthInBytes+=totalNumOfElements*sizeOfElemInBytes;
		boolean canImprove=true;
		while(canImprove){
			canImprove=false;
			while(totalLengthInBytes<qrSpecs.get(curQRLevel)[ecc.ordinal()]){//can increase ECC level
				canImprove=true;//we manage to improve the ecc at least once
				if(!(ecc.equals(ECCLevel.HIGH))&&(totalLengthInBytes<=qrSpecs.get(curQRLevel)[ecc.ordinal()+1])){
					switch(ecc){
						case LOW:
							ecc=ECCLevel.MEDIUM;
							break;
						case MEDIUM:
							ecc=ECCLevel.HIGH;
							break;
						case QUALITY:
							ecc=ECCLevel.HIGH;
							break;
						default:
							break;	
					}
				}
			}
			if(canImprove && ecc.equals(ECCLevel.HIGH)){//if we didnt manage to improve ecc at all, or got stuck in some mid-level, we wont lower the QRlevel
				if(curQRLevel==23){
					break;
				}
				if(totalLengthInBytes<=qrSpecs.get(curQRLevel-1)[ECCLevel.LOW.ordinal()]){
					curQRLevel++;
					ecc=ECCLevel.LOW;
				}
				else{
					canImprove=false;
				}
			}		
		}
		QRProperties result = new QRProperties(curQRLevel,ecc,(qrSpecs.get(curQRLevel)[ecc.ordinal()])*8);
		return result;		
	}

}
