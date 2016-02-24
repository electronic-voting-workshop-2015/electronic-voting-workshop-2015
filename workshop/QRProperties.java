package workshop;

import com.google.zxing.qrcode.decoder.ErrorCorrectionLevel;

public class QRProperties {

	private int level;
	private ErrorCorrectionLevel ecc;
	private int maxNumOfBits;
	private int numberOfPixels;

	public int getNumberOfPixels() {
		return this.numberOfPixels;
		//TODO: calculate number of pixels according to QR level.
	}

	public int getLevel() {
		return level;
	}

	public void setLevel(int level) {
		this.level = level;
	}

	public ErrorCorrectionLevel getEcc() {
		return ecc;
	}

	public void setEcc(ErrorCorrectionLevel ecc) {
		this.ecc = ecc;
	}

	public int getMaxNumOfBits() {
		return maxNumOfBits;
	}

	public void setMaxNumOfBits(int maxNumOfBits) {
		this.maxNumOfBits = maxNumOfBits;
	}

	public QRProperties(int level, ErrorCorrectionLevel ecc, int maxNumOfBits) {
		this.level = level;
		this.ecc = ecc;
		this.maxNumOfBits = maxNumOfBits;
	}

	public QRProperties() {
		this.level = 0;
		this.ecc = null;
		this.maxNumOfBits = 0;
	}
	
}
