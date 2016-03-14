package workshop;

import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.util.Hashtable;

import javax.imageio.ImageIO;

import com.google.zxing.BarcodeFormat;
import com.google.zxing.EncodeHintType;
import com.google.zxing.WriterException;
import com.google.zxing.common.BitMatrix;
import com.google.zxing.qrcode.QRCodeWriter;
import com.google.zxing.qrcode.decoder.ErrorCorrectionLevel;

public class QRGenerator {
	public static final String ENCODING = "ISO-8859-1";

	private QRProperties topQR;
	private QRProperties bottomQR;

	public QRGenerator(QRProperties topQR, QRProperties bottomQR) {
		this.topQR = topQR;
		this.bottomQR = bottomQR;
	}

	@SuppressWarnings({ "unchecked", "rawtypes" })
	public File createQRImage(String qrCodeText, boolean isAudit) throws WriterException, IOException {
		int length = qrCodeText.length();
		QRProperties qr = isAudit ? bottomQR : topQR;
		ErrorCorrectionLevel l = qr.getEcc();
		Hashtable hintMap = new Hashtable();
		File qrFile = new File("qrCode.png");
		hintMap.put(EncodeHintType.ERROR_CORRECTION, l);
		hintMap.put(EncodeHintType.CHARACTER_SET, ENCODING);
		QRCodeWriter qrCodeWriter = new QRCodeWriter();
		BitMatrix byteMatrix = qrCodeWriter.encode(qrCodeText, BarcodeFormat.QR_CODE, qr.getWidth(), qr.getHeight(), hintMap);
		// Make the BufferedImage that are to hold the QRCode
		int matrixWidth = byteMatrix.getWidth();
		BufferedImage image = new BufferedImage(matrixWidth, matrixWidth, BufferedImage.TYPE_INT_RGB);
		image.createGraphics();

		Graphics2D graphics = (Graphics2D) image.getGraphics();
		graphics.setColor(Color.WHITE);
		graphics.fillRect(0, 0, matrixWidth, matrixWidth);
		// Paint and save the image using the ByteMatrix
		graphics.setColor(Color.BLACK);
		for (int i = 0; i < matrixWidth; i++) {
			for (int j = 0; j < matrixWidth; j++) {
				if (byteMatrix.get(i, j)) {
					graphics.fillRect(i, j, 1, 1);
				}
			}
		}
		ImageIO.write(image, "png", qrFile);
		return qrFile;
	}
}
