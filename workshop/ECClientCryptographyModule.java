package ECCryptography;

import workshop.ClientCryptographyModule;

import java.math.BigInteger;
import java.security.SecureRandom;
import java.util.ArrayList;
import java.util.Arrays;

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

    public static void main(String[] args) {
        BigInteger a = new BigInteger("-3");
        BigInteger b = new BigInteger("5ac635d8aa3a93e7b3ebbd55769886bc651d06b0cc53b0f63bce3c3e27d2604b", 16);
        BigInteger p = new BigInteger("115792089210356248762697446949407573530086143415290314195533631308867097853951");
        EllipticCurve curve = new EllipticCurve(a, b, p);
        BigInteger gx = new BigInteger("6b17d1f2e12c4247f8bce6e563a440f277037d812deb33a0f4a13945d898c296", 16);
        BigInteger gy = new BigInteger("4fe342e2fe1a7f9b8ee7eb4a7c0f9e162bce33576b315ececbb6406837bf51f5", 16);
        ECPoint g = new ECPoint(curve, gx, gy);
        int integerSize = 256;
        BigInteger order = new BigInteger("115792089210356248762697446949407573529996955224135760342422259061068512044369");
        ECGroup group = new ECGroup(curve.toByteArray(), g.toByteArray(integerSize), integerSize * 2, order.toByteArray());
        group.logEncryptionMethods = logEncryptionMethods;
        byte[] publicKey = group.getElement(new BigInteger("5").toByteArray()); // Just some example public key. Notice you will not know the
            // exponent of the real publicKey.
        if (logEncryptionMethods) System.out.println("Set group, x = 5, h = " + ECPoint.fromByteArray(curve, publicKey).toString());
        ECClientCryptographyModule module = new ECClientCryptographyModule(group, null);
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
     * @param publicKey - The key provided by the infrastructure team, represents an elemnt in the group.
     * @param message - the message to encrypt.
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

        while(encryptedChunksNum < extendedMessageSize / chunkByteSize) {
            byte[] encryptedChunk = ElgamalEncrypt(group, publicKey,
                    Arrays.copyOfRange(extendedMessage, encryptedChunksNum * chunkByteSize,
                            encryptedChunksNum * chunkByteSize + chunkByteSize));
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
     * Encrypts a part of a message in the size of one element in encryptGroup.
     * PublicKey
     */
    private byte[] ElgamalEncrypt(ECGroup group, byte[] publicKey, byte[] message) {
        // Generate the private key r.
        byte[] r = new byte[group.getElementSize()];
        random.nextBytes(r);
        BigInteger temp = new BigInteger(r);
        // If you find a bug, comment out this line and change it to:
        // temp = new BigInteger(<String of the printed r>);
        temp = temp.mod(new BigInteger(group.getOrder()));
        r = temp.toByteArray();
        System.out.println("Infrastructure - ElGamal encryption: r = " + temp);

        // Turn message into a encryptGroup member.
        if (logEncryptionMethods)System.out.print("m = ");
        byte[] m = group.getElement(message);
        if (logEncryptionMethods) System.out.print("c1 = ");
        byte[] c1 = group.getElement(r);
        if (logEncryptionMethods) System.out.print("c2 = ");
        byte[] c2 = group.groupMult(m, group.groupPow(publicKey, r));
        byte[] result = new byte[c1.length + c2.length];
        System.arraycopy(c1, 0, result, 0, c1.length);
        System.arraycopy(c2, 0, result, c1.length, c2.length);
        return result;
    }

    @Override
    public byte[] sign(byte[] privateKey, byte[] message) {
        // TODO
        return new byte[0];
    }

    @Override
    public boolean verifyCertificate(byte[] publicKey, byte[] encryptedMessage, byte[] certificate) {
        // TODO
        return false;
    }

    // Uses Euclid's method to find the GCD of k and l. returns true if it is 1.
    private boolean gcdEqualsOne(BigInteger k, BigInteger l) {
        return gcd(k, l).equals(BigInteger.ONE);
    }

    private BigInteger gcd(BigInteger k, BigInteger l) {
        if (k.equals(BigInteger.ZERO)) {
            return l;
        }
        if (l.equals(BigInteger.ZERO)) {
            return k;
        }
        if (k.compareTo(l) == 1) {
            return gcd(l, k.mod(l));
        }
        return gcd(k, l.mod(k));
    }
}
