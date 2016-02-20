package ECCryptography;

import java.math.BigInteger;
import java.util.Arrays;

public class EllipticCurve {
    private static final BigInteger TWO = new BigInteger("2");
    private static final BigInteger THREE = new BigInteger("3");

    //a and b parameters
    private final BigInteger a;
    private final BigInteger b;

    //the prime number, all operations are modulo p
    private final BigInteger p;

    private final ECPoint zeroMember = new ECPoint(this, BigInteger.ZERO, BigInteger.ZERO);

    public EllipticCurve(BigInteger a, BigInteger b, BigInteger p) {
        this.a = a;
        this.b = b;
        this.p = p;
    }

    /**
     * Constructs a point out of a byte array. Assumes that the array is of the following format:
     * The first byte, array[0] represents the number of bytes representing x.
     * Following this byte are the bytes representing x.
     * The next byte represents the number of bytes representing y, followed by the bytes representing y, and following
     * the same format for p.
     * The byte representation matches the byte representation of the BigInteger.toByteArray() method.
     */
    public static EllipticCurve fromByteArray(byte[] array) {
        int aSize = (int) array[0];
        BigInteger a = new BigInteger(Arrays.copyOfRange(array, 1, aSize + 1));
        int bSize = (int) array[aSize + 1];
        BigInteger b = new BigInteger(Arrays.copyOfRange(array, aSize + 2, aSize + bSize + 2));
        int pSize = (int) array[aSize + bSize + 2];
        BigInteger p = new BigInteger(Arrays.copyOfRange(array, aSize + bSize + 3, array.length));
        return new EllipticCurve(a, b, p);
    }

    /**
     * Returns a byte[] representation of the point, consistent with the format described at
     * fromByteArray() above.
     */
    public byte[] toByteArray() {
        byte[] aBytes = a.toByteArray();
        int aLen = aBytes.length;
        byte[] bBytes = b.toByteArray();
        int bLen = bBytes.length;
        byte[] pBytes = p.toByteArray();
        int pLen = pBytes.length;
        byte[] result = new byte[aLen + bLen + pLen + 3];
        result[0] = (byte) aLen;
        System.arraycopy(aBytes, 0, result, 1, aLen);
        result[aLen + 1] = (byte) bLen;
        System.arraycopy(bBytes, 0, result, aLen + 2, bLen);
        result[aLen + bLen + 2] = (byte) pLen;
        System.arraycopy(pBytes, 0, result, aLen + bLen + 3, pLen);
        return result;
    }



    public BigInteger getA() {
        return a;
    }

    public BigInteger getB() {
        return b;
    }

    public BigInteger getP() {
        return p;
    }

    public ECPoint getZeroMember() {
        return zeroMember;
    }

    /**
     * Adds the points p and q over the group formed by this curve.
     */
    public ECPoint add(ECPoint g, ECPoint q) {
        if (!this.equals(g.getCurve()) || !this.equals(q.getCurve())) {
          throw new IllegalArgumentException("Points added are not on the curve.");
        }
        // Edge cases
        if (g.equals(q.completeing())) {
            return zeroMember;
        }
        if (g.equals(zeroMember)) {
            return q;
        }
        if (q.equals(zeroMember)) {
            return g;
        }

        BigInteger Xg = g.getX();
        BigInteger Yg = g.getY();
        BigInteger Xq = q.getX();
        BigInteger Yq = q.getY();

        BigInteger m;
        if (g.equals(q)) {
            if (Yg.equals(BigInteger.ZERO)) {
                return zeroMember;
            }
            // m = (3x^2 + a)/2y
            m = Xg.modPow(TWO, p).multiply(THREE).mod(p).add(a).mod(p)
                    .multiply(Yg.multiply(TWO).mod(p).modInverse(p)).mod(p);
        } else {
            // m = (Yg - Yq)/(Xg - Xq)
            m = Yg.subtract(Yq).mod(p).multiply(Xg.subtract(Xq).mod(p).modInverse(p)).mod(p);
        }
        // Xr = m^2 - Xg - Xq
        BigInteger Xr = m.modPow(TWO, p).subtract(Xg).subtract(Xq).mod(p);
        // Yr = -Yresult = Yg + m(Xr - Xg)
        BigInteger Yr = m.multiply(Xr.subtract(Xg)).add(Yg).mod(p);
        return (new ECPoint(this, Xr, Yr)).completeing();
    }

    /**
     * Returns the point 'q' multiplied by n over the curve.
     */
    public ECPoint multiply(ECPoint q, BigInteger n) {
        String binaryRep = n.toString(2);
        ECPoint result = zeroMember;
        ECPoint qMult2i = q; // q * 2^i;
        for (int i = 0; i < binaryRep.length(); i++) {
            if (binaryRep.charAt(binaryRep.length() - 1 - i) == '1') {
                result = add(result, qMult2i);
            }
            qMult2i = add(qMult2i, qMult2i);
        }
        return result;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        EllipticCurve that = (EllipticCurve) o;

        if (getA() != null ? !getA().equals(that.getA()) : that.getA() != null) return false;
        if (getB() != null ? !getB().equals(that.getB()) : that.getB() != null) return false;
        return getP() != null ? getP().equals(that.getP()) : that.getP() == null;

    }
}
