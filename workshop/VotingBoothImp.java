package workshop;

import java.io.File;
import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Set;

import javax.swing.ImageIcon;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTextField;

import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.GridLayout;
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

	private static QRProperties topQR; // Valid vote QR properties
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
		int machineNum;
		try {
			curVote = parseJSON(jsonRepr); // parse the JSONArray to get info
											// about the vote
			// machineNum = 
			// TODO: get the machine number from the JSON !!
		} catch (JSONException e) {
			System.err.println("An error occured during the parse of JSONArray.");
		}

		// encrypt the vote
		for (Race race : curVote) {
			for (String name : race.getVotesArray()) {
				encryptResult = Parameters.cryptoClient.encryptGroupMember(
						Parameters.publicKey,
						Parameters.candidatesMap.get(name));
				char[] cipherChars = new char[encryptResult[0]];
				char[] randomnessChars = new char[encryptResult[1]];
				for (int i = 0; i < cipherChars.length; i++) {
					cipherChars[i] = (char)encryptResult[0][i];
				}
				for (int i = 0; i < randomnessChars.length; i++) {
					randomnessChars[i] = (char)encryptResult[1][i];
				}
				sbCiphertext.append(cipherChars);
				sbRandomness.append(randomnessChars); 
				// the opposite operation (for those who read the QR) is:
				// char[] ch = strFromQR.toCharArray();
				// and then cast it element by element to byte
			}
		}
		sbCiphertext.append(addSignatureAndTimeStamp(machineNum)); // add machine signature and timestamp to ciphertext
		ciphertext = sbCiphertext.toString();
		auditRandomness = sbRandomness.toString();
		File topQr = topQRCreator(ciphertext); // create the top QR containing the ciphertext
		printPage(topQr, curVote); // print the ballot
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
	 *            must preserve soundness, meaning invalid vote format must be rejected
	 * @return list of Race objects, representing the votes of the voter in each
	 *         race, parsed from the input JSON.
	 */
	private static ArrayList<Race> parseJSON(JSONArray jsonRepr) throws JSONException {
		ArrayList<Race> result = new ArrayList<Race>();
		int raceNum = 0;
		JSONObject curRace;
		String curCand;
		for (RaceProperties rp : Parameters.racesProperties) {
			curRace = jsonRepr.getJSONObject(raceNum);
			result.add(raceNum, new Race(rp));
			Set<String> validNames = rp.getPossibleCandidates();
			String[] curRaceArrayOfNames = new String[rp.getNumOfSlots()];
			if((rp.isOrdered() && curRace.get("type")!=2) || (rp.getNumOfSlots()>1 && curRace.get("type")>0)){//type check
				JSONException exp=new org.json.JSONException("Invalid vote format, mismatching types");
				throw(exp);
			}
			if(!(rp.getNameOfRace().equals(curRace.get("position")))){//race name match check
				JSONException exp=new org.json.JSONException("Invalid vote format, mismatching positions");
				throw(exp);
			}
			if(rp.getNumOfSlots()!=curRace.getJSONArray("chosenCandidates").length()){//candidates length check
				JSONException exp=new org.json.JSONException("Invalid vote format, mismatch in number of candidates chosen for : "+rp.getNameOfRace());
				throw(exp);
			}
			for (int i = 0; i < rp.getNumOfSlots(); i++) {
				curCand = curRace.getJSONArray("chosenCandidates").get(i).toString();											
				if (validNames.contains(curCand)) {//candidate name exists in the name pool check
					curRaceArrayOfNames[i] = curCand;
				}
				else{
					JSONException exp=new org.json.JSONException("Invalid name used");
					throw(exp);
				}
			}
			result.get(raceNum).setVotesArray(curRaceArrayOfNames);
			raceNum++;
		}
		return result;
	}

	/**
	 * prepares the machine signature of the current vote, by calling the Sign
	 * method. Prepares the current timestamp.
	 * 
	 * @return a string which is a concatenation of the signature and timestamp.
	 */
	private String addSignatureAndTimeStamp(int machineNum) {
		String signAndTimeStamp = "";
		// the machine's signature
		byte[] signatureByteArray = Parameters.mapMachineToSignature.get(machineNum);
		try {
			signAndTimeStamp += new String(signatureByteArray, "UTF-8");
		} catch (UnsupportedEncodingException e1) {
			// TODO Auto-generated catch block
			System.err.println("An error occured during byte-to-char conversion.");
		}
		// the timestamp according to the required precision (chosen in the parameters file)
		// precision level 1: only hour and minute (2 bytes total)
		// precision level 2: hour+minute+second (3 bytes total)
		Calendar cal = Calendar.getInstance();
		byte[] timeArray;
		if (Parameters.timeStampLevel == 1) {
			timeArray = new byte[2];
			timeArray[0] = (byte) cal.get(Calendar.HOUR_OF_DAY);
			timeArray[1] = (byte) cal.get(Calendar.MINUTE);
		}
		// == 2
		else {
			timeArray = new byte[3];
			timeArray[0] = (byte) cal.get(Calendar.HOUR_OF_DAY);
			timeArray[1] = (byte) cal.get(Calendar.MINUTE);
			timeArray[2] = (byte) cal.get(Calendar.SECOND);
		}
	    try {
			signAndTimeStamp += new String(timeArray, "UTF-8");
		} catch (UnsupportedEncodingException e) {
			// TODO Auto-generated catch block
			System.err.println("An error occured during byte-to-char conversion.");
		}
	    return signAndTimeStamp; // return the signature of the machine and the time stamp concatenated together.
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
 	* @param qrPng QR png file.
 	*/
/**
 	* prints the ballot.
 	* 
 	* @param votesInAllRaces
 	*            . a list of Race objects contains the names of candidates to
 	*            be chosen in each race.
 	* @param qrPng QR png file.
 	*/
	public void printPage(final File qrPng,final ArrayList<Race> votesInAllRaces) {
		class Ballot extends JPanel implements Printable{
		
			public Ballot(){
				GridLayout mainLayout = new GridLayout(4,1);
				this.setLayout(mainLayout);	
				JPanel emptyPanel, tapePanel, votePanel, qrPanel;
			
				int votePanelH = 54, width = 242, rows = 0;
			
				emptyPanel = new JPanel();
				emptyPanel.add(new JTextField("fuck"));
				votePanel = new JPanel();
				tapePanel = new JPanel();
				tapePanel.add(new JTextField("You"));
				qrPanel = new JPanel();
			  	this.setSize(new Dimension(242,312));
				emptyPanel.setSize(new Dimension(242,54));
				votePanel.setSize(new Dimension(242,54));
				tapePanel.setSize(new Dimension(242,13));
				qrPanel.setSize(new Dimension(242,191));
				
				for(Race r : votesInAllRaces){
					if(r.getCurRaceProp().getNumOfSlots() >= rows)
						rows = r.getCurRaceProp().getNumOfSlots();
				}
			
			
				GridLayout votePanelLayout = new GridLayout(1,votesInAllRaces.size());
				votePanel.setLayout(votePanelLayout);
				for(Race r : votesInAllRaces){
					JPanel p = new JPanel();
					p.setSize(new Dimension(width/votesInAllRaces.size(),votePanelH));
				
					GridLayout l = new GridLayout(rows+1,1);
					p.setLayout(l);
					JTextField raceName = new JTextField(r.getCurRaceProp().getNameOfRace());
					p.add(raceName);
					for(String s:r.getVotesArray()){
						JTextField jtf = new JTextField(s);
						p.add(jtf);								
					}				
					votePanel.add(p);
				}
			
				ImageIcon qrIcon = new ImageIcon(qrPng.getPath());
				JLabel imagelabel = new JLabel(qrIcon, JLabel.CENTER);
				qrPanel.add(imagelabel);
			
				this.add(emptyPanel);
				this.add(votePanel);
				this.add(tapePanel);
				this.add(qrPanel);

			
				this.setVisible(true);
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
		JFrame f = new JFrame();
		f.setSize(new Dimension(242,312));
		Ballot b = new Ballot();	
		f.add(b);
		f.setVisible(true);
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
				System.out.println("TimeStamp is not initialized");
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
							ecc=ECCLevel.QUALITY;
							break;
						case QUALITY:
							ecc=ECCLevel.HIGH;
							break;
						default:
							break;	
					}
				}
				else{
					break;
				}
			}
			if(canImprove){//if we didnt manage to improve ecc at all, we wont lower the QRlevel
				if(curQRLevel==minOfSet(qrSpecs.keySet())){
					break;
				}
				if(totalLengthInBytes<=qrSpecs.get(curQRLevel-1)[ecc.ordinal()]){
					curQRLevel--;					
				}
				else{
					canImprove=false;
				}
			}		
		}
		QRProperties result = new QRProperties(curQRLevel,ecc,(qrSpecs.get(curQRLevel)[ecc.ordinal()])*8);
		return result;		
	}
	
	private static int minOfSet(Set<Integer> set){
		int[] res= new int[set.size()];
		int i=0;
		for(int k: set){
			res[i]=k;
			i++;
		}
		int min=res[0];
		for(i=1;i<res.length;i++){
			if(res[i]<min){
				min=res[i];
			}
		}
		return min;		
	}

}
