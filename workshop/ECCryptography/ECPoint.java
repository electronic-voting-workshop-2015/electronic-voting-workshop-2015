package workshop.ECCryptography;

import java.math.BigInteger;
import java.util.Arrays;

/**
 * A point in a discreete group formed by an elliptic curve.
 */
public class ECPoint {

    private final EllipticCurve curve;
    private final BigInteger x;
    private final BigInteger y;

    ECPoint(EllipticCurve curve, BigInteger x, BigInteger y) {
        // TODO check that the curve is satisfied by the point.
        this.curve = curve;
        this.x = x;
        this.y = y;
    }

    /**
     * Constructs a point out of a byte array. Assumes that the first half of the array is x and the second half is y.
     * The representation of x and y in the array should match the BigInteger.toByteArray() method.
     */
    public static ECPoint fromByteArray(EllipticCurve curve, byte[] array) {
        // TODO verify if point lies on curve.
        int size = array.length / 2;
        BigInteger x = new BigInteger(Arrays.copyOfRange(array, 0, size));
        BigInteger y = new BigInteger(Arrays.copyOfRange(array, size, array.length));
        return new ECPoint(curve, x, y);
    }

    /**
     * Returns a byte[] representation of the point, consistent with the format described at
     * fromByteArray() above.
     * maxSize represents the maximum number of bytes to represent x or y
     */
    public byte[] toByteArray(int maxSize) {
        byte[] xBytes = x.toByteArray();
        byte[] yBytes = y.toByteArray();
        byte[] result = new byte[maxSize * 2];
        System.arraycopy(xBytes, 0, result, maxSize - xBytes.length, xBytes.length);
        System.arraycopy(yBytes, 0, result, maxSize * 2 - yBytes.length, yBytes.length);
        return result;
    }

    public BigInteger getX() {
        return x;
    }

    public BigInteger getY() {
        return y;
    }

    public EllipticCurve getCurve() {
        return curve;
    }

    /**
     * Returns the completing elemnt of this point in the group (-this).
     */
    public ECPoint completing() {
        return new ECPoint(curve, x, y.negate().mod(curve.getP()));
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof ECPoint)) return false;

        ECPoint ecPoint = (ECPoint) o;

        if (!getCurve().equals(ecPoint.getCurve())) return false;
        if (!getX().equals(ecPoint.getX())) return false;
        return getY().equals(ecPoint.getY());

    }

    // TODO for debugging, remove.
    public String toString() {
        return "(" + getX() + ", " + getY() + ")";
    }
}
