package workshop;
import java.awt.image.BufferedImage;

public interface VotingBooth {
	/**
	 * sets the desired Top QR parameters, Version , ECC level, number of bits to be decoded, relevant only to the VB team
	 * @param numberOfCandidates
	 * @param timeStampLevel
	 * @param signSize
	 * @return
	 */
	public void calcTopQRParameters(int numberOfCandidates, int timeStampLevel, int signSize);
	/**
	 * sets the desired Bottom QR parameters, Version , ECC level, number of bits to be decoded, relevant only to the VB team
	 * @param numberOfCandidates
	 * @param timeStampLevel
	 * @param signSize
	 * @return
	 */
	public void calcBottomQRParameters(int numberOfCandidates, int timeStampLevel, int signSize);
	/**
	 * Receiving the original vote from the GUI team, encrypting it and encoding to QR
	 * return type is still unknown, for now we use BufferedImage, we wait for the GUI team to publish their Interface
	 * @param curVote
	 * @return
	 */
	public BufferedImage currentVoteQR(String[] curVote);	
}
