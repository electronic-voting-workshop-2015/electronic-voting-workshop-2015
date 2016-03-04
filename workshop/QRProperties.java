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
	static private ErrorCorrectionLevel calculateECC(int numberOfBytes) {
		if (numberOfBytes < 890) {
			return ErrorCorrectionLevel.H;
		}
		if (numberOfBytes < 1160) {
			return ErrorCorrectionLevel.Q;
		}
		if (numberOfBytes < 1620) {
			return ErrorCorrectionLevel.M;
		} // >= 1620
		return ErrorCorrectionLevel.L;
	}

}
