package workshop;

import com.google.zxing.qrcode.decoder.ErrorCorrectionLevel;

public class QRProperties {

	private final ErrorCorrectionLevel ecc;
	private final int width;
	private final int height;

	public ErrorCorrectionLevel getEcc() {
		return ecc;
	}
	
	public int getWidth() {
		return width;
	}
	
	public int getHeight() {
		return height;
	}

	public QRProperties(int numberOfBytes, int qrWidth, int qrHeight) {
		this.ecc = calculateECC(numberOfBytes);
		this.width = qrWidth;
		this.height = qrHeight;
	}

	/*
	 * Returns the highest possible ECC for the given number of bytes,
	 */
	static public ErrorCorrectionLevel calculateECC(int numberOfBytes) {
		if (numberOfBytes < 440) {
			return ErrorCorrectionLevel.H;
		}
		if (numberOfBytes < 550) {
			return ErrorCorrectionLevel.Q;
		}
		if (numberOfBytes < 800) {
			return ErrorCorrectionLevel.M;
		} // >= 800
		return ErrorCorrectionLevel.L;
	}

}
