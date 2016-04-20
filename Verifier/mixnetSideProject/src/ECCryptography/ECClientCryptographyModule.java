package ECCryptography;

import java.io.FileNotFoundException;
import java.io.UnsupportedEncodingException;
import java.math.BigInteger;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

import workshop.ClientCryptographyModule;

public class ECClientCryptographyModule implements ClientCryptographyModule {
    private final ECGroup encryptGroup;
    private final ECGroup signGroup;
    private SecureRandom random;

    // We added a lot of printouts, so if you find a bug, you can send us the logs. The random values will always be printed.
    // If you find a bug, please set the 'r' value on line 130 to be the printed 'r', set this to 'true and send the
    // printouts to feld.noa@gmail.com.
    private static boolean logEncryptionMethods = false;

    public ECClientCryptographyModule(ECGroup encryptGroup, ECGroup signGroup) {
        this.encryptGroup = encryptGroup;
        this.signGroup = signGroup;
        this.random = new SecureRandom();
    }

    public static void main(String[] args) throws NoSuchAlgorithmException, FileNotFoundException, UnsupportedEncodingException {
//        BigInteger a = new BigInteger("-3");
//        BigInteger b = new BigInteger("5ac635d8aa3a93e7b3ebbd55769886bc651d06b0cc53b0f63bce3c3e27d2604b", 16);
//        BigInteger p = new BigInteger("115792089210356248762697446949407573530086143415290314195533631308867097853951");
//        EllipticCurve curve = new EllipticCurve(a, b, p);
//        BigInteger gx = new BigInteger("6b17d1f2e12c4247f8bce6e563a440f277037d812deb33a0f4a13945d898c296", 16);
//        BigInteger gy = new BigInteger("4fe342e2fe1a7f9b8ee7eb4a7c0f9e162bce33576b315ececbb6406837bf51f5", 16);
//        ECPoint g = new ECPoint(curve, gx, gy);
//        int integerSize = 256 / 8;
//        BigInteger order = new BigInteger("115792089210356248762697446949407573529996955224135760342422259061068512044369");
//        ECGroup group = new ECGroup(curve.toByteArray(), g.toByteArray(integerSize), integerSize * 2 + 2, order.toByteArray());
//        group.logEncryptionMethods = logEncryptionMethods;
//        byte[] publicKey = group.getElement(new BigInteger("5").toByteArray()); // Just some example public key. Notice you will not know the
//        // exponent of the real publicKey.
//        if (logEncryptionMethods) System.out.println("Set group, x = 5, h = " + ECPoint.fromByteArray(curve, publicKey).toString());
//        ECClientCryptographyModule module = new ECClientCryptographyModule(group, group); // TODO change signGroup!!!!!!!
//
//        byte[] message = group.getElement(new BigInteger("4444").toByteArray());
//        byte[][] encrypted = module.encryptGroupMember(publicKey, message);
//        byte[] certificate = module.sign(new BigInteger("123456").toByteArray(), encrypted[0]);
//        byte[] pKey = group.getElement(new BigInteger("123456").toByteArray());
//        boolean verified = module.verifyCertificate(pKey,  encrypted[0], certificate);
//        System.out.println("verified: " + verified);
//        PrintWriter file = new PrintWriter("encrypted.txt");
//        Base64.Encoder base64Encoder = Base64.getEncoder();
//        byte[] base64 = base64Encoder.encode(encrypted[0]);
//        String utf8 = new String(base64, "UTF-8");
//        byte[] base64cer = base64Encoder.encode(certificate);
//        String utf8cer = new String(base64cer, "UTF-8");
//        file.println("message:");
//        file.println(utf8);
//        file.println("certificate:");
//        file.println(utf8cer);
//        file.close();
    }

    // TODO used for debugging, remove when done.
    private static void printArray(byte[] array) {
        for (int i = 0; i < array.length; i++) {
            System.out.print(array[i] + ", ");
            if (i % 16 == 0) System.out.println();
        }
        System.out.println();
    }

    @Override
    public byte[] encrypt(byte[] publicKey, byte[] message) {
        return encrypt(publicKey, message, encryptGroup);
    }

    /**
     * Encrypts the message by dividing it to chunks and encrypting each chunk using ElGamal.
     * This method supports only encrypting for groups with order highr than 2^8 (i.e, we don't split the data's bytes),
     * and smaller than (2^(8 * 2^31)) / 2, (i.e., the groups order can be represented by 2^31 bytes - a positive int,
     * and so is the size of the encrypted message).
     *
     * @param publicKey - The key provided by the infrastructure team, represents an elemnt in the group.
     * @param message   - the message to encrypt.
     */
    private byte[] encrypt(byte[] publicKey, byte[] message, ECGroup group) {
        int chunkByteSize = (int) Math.log(new BigInteger(group.getOrder()).doubleValue()) / 8;
        int extendedMessageSize = message.length % chunkByteSize == 0 ? message.length
                : (message.length / chunkByteSize + 1) * chunkByteSize;
        byte[] extendedMessage = new byte[extendedMessageSize];
        System.arraycopy(message, 0, extendedMessage, 0, message.length);

        if (logEncryptionMethods) {
            System.out.println("Encrypting message:");
            System.out.println("-----------------------------------------------------------------------------------");
            printArray(message);
            System.out.println("-----------------------------------------------------------------------------------");
            System.out.println("Chunk size in bytes: " + chunkByteSize);
            System.out.println("Extended message size: " + extendedMessageSize);
            System.out.println("Loops to go: " + extendedMessageSize / chunkByteSize);
        }

        int groupElementSize = publicKey.length;
        int encryptedMessageSize = (extendedMessageSize / chunkByteSize) * groupElementSize * 2; // Each chunk is
        // encrypted to 2 group elements in ElGamal.
        byte[] encryptedMessage = new byte[encryptedMessageSize];
        int encryptedChunksNum = 0;

        while (encryptedChunksNum < extendedMessageSize / chunkByteSize) {
            byte[] chunk = Arrays.copyOfRange(extendedMessage, encryptedChunksNum * chunkByteSize,
                    encryptedChunksNum * chunkByteSize + chunkByteSize);
            // Turn message into a encryptGroup member.
            if (logEncryptionMethods) System.out.print("m = ");
            byte[] m = group.getElement(chunk);
            byte[] encryptedChunk = elgamalEncrypt(group, publicKey, m)[0];
            if (logEncryptionMethods) {
                System.out.println("Encrypting sub-message: ");
                System.out.println("-----------------------------------------------------------------------------------");
                printArray(Arrays.copyOfRange(extendedMessage, encryptedChunksNum * chunkByteSize,
                        encryptedChunksNum * chunkByteSize + chunkByteSize));
                System.out.println("-----------------------------------------------------------------------------------");
                System.out.println("Encrypted sub-message result: ");
                System.out.println("-----------------------------------------------------------------------------------");
                printArray(encryptedChunk);
                System.out.println("-----------------------------------------------------------------------------------");

            }
            System.arraycopy(encryptedChunk, 0, encryptedMessage, encryptedChunksNum * encryptedChunk.length, encryptedChunk.length);
            encryptedChunksNum++;
            if (logEncryptionMethods) {
                System.out.println("Encrypted chunks number = " + encryptedChunksNum + ", result so far: ");
                System.out.println("-----------------------------------------------------------------------------------");
                printArray(encryptedMessage);
                System.out.println("-----------------------------------------------------------------------------------");
            }
        }

        return encryptedMessage;
    }

    /**
     * Encrypts the message asusuming that m is a valid group member of signGroup.
     *
     * @param publicKey
     * @param m         - a valid group member of signGroup. will be assigned with the new encrypted message.
     * @return Value at result[0] = encrypted message, result[1] = the randomness r.
     */
    public byte[][] encryptGroupMember(byte[] publicKey, byte[] m) {
        return elgamalEncrypt(encryptGroup, publicKey, m);
    }

    /**
     * Encrypts a part of a message in the size of one element in encryptGroup.
     * PublicKey
     */
    private byte[][] elgamalEncrypt(ECGroup group, byte[] publicKey, byte[] m) {
        // Generate the private key r.
        byte[] r = new byte[group.getElementSize()];
        byte[] tempR = new byte[r.length - 1];
        random.nextBytes(tempR);
        BigInteger temp = new BigInteger(tempR);
        // If you find a bug, comment out this line and change it to:
        // temp = new BigInteger(<String of the printed r>);
        temp = temp.mod(new BigInteger(group.getOrder()));
        System.arraycopy(temp.toByteArray(), 0, r, r.length - temp.toByteArray().length, temp.toByteArray().length);
        byte[] result = encryptForRandomness(group, publicKey, m, r);
        byte[][] resultAndR = new byte[2][];
        resultAndR[0] = result;
        resultAndR[1] = r;
        return resultAndR;
    }

    /**
     * Encrypts the message using Elgamal, for a known randomness.
     *
     * @param publicKey - The public encryption key published by the BB.
     * @param m         - The message to encrypt.
     * @param r         - The chosen randomness.
     */
    public byte[] elgamalReencryptForMixnet(byte[] publicKey, byte[] m, BigInteger r) {
        return encryptForRandomness(encryptGroup, publicKey, m, r.toByteArray());
    }

    private byte[] encryptForRandomness(ECGroup group, byte[] publicKey, byte[] m, byte[] r) {
        byte[] c1 = group.getElement(r);
        byte[] c2 = group.groupMult(m, group.groupPow(publicKey, r));
        byte[] result = new byte[c1.length + c2.length];
        System.arraycopy(c1, 0, result, 0, c1.length);
        System.arraycopy(c2, 0, result, c1.length, c2.length);
        return result;
    }

    /**
     * Signs the message, converts the result numbers to an unsigned, little endian form and returns an array of the
     * formatted result.
     *
     * @param privateKey       The party's key, given physically by the infrastructure team.
     * @param encryptedMessage
     * @return
     */
    @Override
    public byte[] sign(byte[] privateKey, byte[] encryptedMessage) {
        try {
            byte[] e = sha256(encryptedMessage);
            int lN = (int) Math.log(new BigInteger(signGroup.getOrder()).doubleValue());
            byte[] certificate = new byte[0];
            BigInteger n = new BigInteger(signGroup.getOrder());
            BigInteger z = fromUnsignedLittleEndian(Arrays.copyOfRange(e, 0, lN));
            if (logEncryptionMethods) {
                System.out.println("Sign: lN = " + lN + ", z = " + z);
            }
            while (certificate.length == 0) {
                byte[] kBytes = new byte[0];
                BigInteger k = BigInteger.ZERO;
                // If something fails, replace the next loop with:
                // k = new BigInteger(<String of the k printed out before failing.>);
                while (k.equals(BigInteger.ZERO)) {
                    kBytes = new byte[lN];
                    random.nextBytes(kBytes);
                    k = new BigInteger(kBytes).mod(n);
                }
                byte[] kG = signGroup.getElement(kBytes);
                BigInteger r = signGroup.getX(kG);
                if (r.equals(BigInteger.ZERO)) {
                    continue;
                }
                BigInteger s = k.modInverse(n).multiply(
                        z.add(r.multiply(new BigInteger(privateKey)).mod(n)).mod(n)).mod(n);
                if (s.equals(BigInteger.ZERO)) {
                    continue;
                }
                if (logEncryptionMethods) {
                    System.out.println("Sign: k = " + k + "r = " + r + "\ns = " + s);
                }
                byte[] rBytes = toUnsignedLittleEndian(r, signGroup.getElementSize() / 2);
                byte[] sBytes = toUnsignedLittleEndian(s, signGroup.getElementSize() / 2);
                certificate = new byte[rBytes.length + sBytes.length];
                System.arraycopy(rBytes, 0, certificate, 0, rBytes.length);
                System.arraycopy(sBytes, 0, certificate, rBytes.length, sBytes.length);
            }
            if (logEncryptionMethods) {
                System.out.println("Sign: certificate: ");
                System.out.println("---------------------------------------------------------------------------------");
                printArray(certificate);
                System.out.println("---------------------------------------------------------------------------------");
            }
            return certificate;
        } catch (NoSuchAlgorithmException exception) {
            System.out.println(exception.getMessage());
            System.out.println("Sign failed.");
            return new byte[0];
        }
    }

    @Override
    public boolean verifyCertificate(byte[] publicKey, byte[] encryptedMessage, byte[] certificate) {
        byte[] rBytes = Arrays.copyOfRange(certificate, 0, certificate.length / 2);
        byte[] sBytes = Arrays.copyOfRange(certificate, certificate.length / 2, certificate.length);
        BigInteger r = fromUnsignedLittleEndian(rBytes);
        BigInteger s = fromUnsignedLittleEndian(sBytes);
        if (logEncryptionMethods) {
            System.out.println("Verify certificate: r = " + r + "\ns = " + s);
        }
        if (r.compareTo(BigInteger.ONE) == -1
                || r.compareTo(new BigInteger(signGroup.getOrder()).subtract(BigInteger.ONE)) == 1) {
            if (logEncryptionMethods) System.out.println("False over r < 1 or r > n-1");
            return false;
        }
        if (s.compareTo(BigInteger.ONE) == -1
                || s.compareTo(new BigInteger(signGroup.getOrder()).subtract(BigInteger.ONE)) == 1) {
            if (logEncryptionMethods) System.out.println("False over s < 1 or s > n-1");
            return false;
        }
        try {
            byte[] e = sha256(encryptedMessage);
            BigInteger n = new BigInteger(signGroup.getOrder());
            int lN = (int) Math.log(n.doubleValue());
            byte[] zBytes = Arrays.copyOfRange(e, 0, lN);
            BigInteger z = fromUnsignedLittleEndian(zBytes);
            BigInteger w = s.modInverse(n);
            BigInteger u1 = z.multiply(w).mod(n);
            BigInteger u2 = r.multiply(w).mod(n);
            byte[] x1y1 = signGroup.groupMult(signGroup.getElement(u1.toByteArray()), signGroup.groupPow(publicKey, u2.toByteArray()));
            BigInteger x1 = signGroup.getX(x1y1);

            if (logEncryptionMethods) {
                System.out.println("Verify certificate: ");
                System.out.println("n = " + n);
                System.out.println("lN = " + lN);
                System.out.println("z = " + z);
                System.out.println("w = " + w);
                System.out.println("u1 = " + u1);
                System.out.println("u2 = " + u2);
                System.out.println("x1 = " + x1);
                System.out.println("y1 = " + signGroup.getY(x1y1));
            }

            return r.equals(x1);

        } catch (NoSuchAlgorithmException exception) {
            System.out.println(exception.getMessage());
            System.out.println("Verify certificate failed.");
            return false;
        }
    }

    @Override
    public Map<Integer, byte[]> getCandidateToMemebrMapping(int candidateNum) {
        Map<Integer, byte[]> result = new HashMap();
        for (int i = 1; i <= candidateNum; i++) {
            result.put(i, encryptGroup.getElement(BigInteger.valueOf(i).toByteArray()));
        }
        return result;
    }

    public ECGroup getEncryptGroup() {
        return encryptGroup;
    }

    public ECGroup getSignGroup() {
        return signGroup;
    }

    // Turns a bigInteger to a little endian bytes array of fixed size. Not verified for negative BigIntegers.
    private static byte[] toUnsignedLittleEndian(BigInteger n, int arrlength) {
        byte[] result = new byte[arrlength];
        byte[] nBytes = n.toByteArray();
        for (int i = 0; i < nBytes.length; i++) {
            result[i] = nBytes[nBytes.length - i - 1];
        }
        return result;
    }

    // Turns a little endian bytes array to a BigInteger. Not verified for negative BigIntegers.
    private static BigInteger fromUnsignedLittleEndian(byte[] arr) {
        byte[] resBytes = new byte[arr.length + 1];
        for (int i = 0; i < arr.length; i++) {
            resBytes[resBytes.length - i - 1] = arr[i];
        }
        return new BigInteger(resBytes);
    }

    private static byte[] sha256(byte[] input) throws NoSuchAlgorithmException {
        MessageDigest mDigest = MessageDigest.getInstance("SHA-256");
        return mDigest.digest(input);
    }
}
