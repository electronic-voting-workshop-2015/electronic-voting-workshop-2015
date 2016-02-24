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
	public enum QRSize {
		Small, Big
	}

	public static ErrorCorrectionLevel computeErrorCorrectionLevel(QRSize size, int stringLenght) {
		switch (size) {
		case Small:
			if (stringLenght <= 580) {
				return ErrorCorrectionLevel.H;
			} else if (stringLenght <= 750) {
				return ErrorCorrectionLevel.Q;
			} else if (stringLenght <= 1080) {
				return ErrorCorrectionLevel.M;
			} else if (stringLenght <= 1465) {
				return ErrorCorrectionLevel.L;
			}
			break;

		case Big:
			if (stringLenght <= 800) {
				return ErrorCorrectionLevel.H;
			} else if (stringLenght <= 1100) {
				return ErrorCorrectionLevel.Q;
			} else if (stringLenght <= 1550) {
				return ErrorCorrectionLevel.M;
			} else if (stringLenght <= 2068) {
				return ErrorCorrectionLevel.L;
			}
			break;
		}
		throw new IllegalArgumentException();
	}

		
		private QRProperties topQR;
		private QRProperties bottomQR;
		
		public QRGenerator(QRProperties topQR, QRProperties bottomQR) {
			this.topQR = topQR;
			this.bottomQR = bottomQR;
		}

		private File createQRImage(String qrCodeText, boolean isAudit) throws WriterException, IOException {
			QRProperties qr = isAudit ? bottomQR : topQR;
			ErrorCorrectionLevel l = qr.getEcc();
			int size = qr.getNumberOfPixels();
			Hashtable<EncodeHintType, ErrorCorrectionLevel> hintMap = new Hashtable<EncodeHintType, ErrorCorrectionLevel>();
			
			File qrFile = new File("qrCode.png");
			hintMap.put(EncodeHintType.ERROR_CORRECTION, l);
			QRCodeWriter qrCodeWriter = new QRCodeWriter();
			BitMatrix byteMatrix = qrCodeWriter.encode(qrCodeText, BarcodeFormat.QR_CODE, size, size, hintMap);
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
