package ECCryptography;

import workshop.Group;

import java.math.BigInteger;

/**
 * A group based on an elliptic curve.
 */
public class ECGroup extends Group {

    private EllipticCurve curve;
    private ECPoint generator;
    private int elementByteSize;
    private BigInteger order;
    // TODO used for debugging, remove when done.
    public boolean logEncryptionMethods;

    public ECGroup(byte[] curve, byte[] generator, int elementByteSize, byte[] order) {
        this.curve = EllipticCurve.fromByteArray(curve);
        this.generator = ECPoint.fromByteArray(this.curve, generator);
        this.elementByteSize = elementByteSize;
        this.order = new BigInteger(order);
    }

    @Override
    public byte[] getElement(byte[] exponent) {
        if (logEncryptionMethods) {
            System.out.println("get elemnt no " + exponent[0]);
            System.out.println("resulr: " + curve.multiply(generator, new BigInteger(exponent)).toString());
        }
        return curve.multiply(generator, new BigInteger(exponent)).toByteArray(elementByteSize / 2);
    }

    byte[] getMember(BigInteger x, BigInteger y) {
        return new ECPoint(curve, x, y).toByteArray(elementByteSize / 2);
    }
    
    @Override
    public byte[] getOrder() {
        return order.toByteArray();
    }

    @Override
    public byte[] getGenerator() {
        return generator.toByteArray(elementByteSize / 2);
    }

    @Override
    public int getElementSize() {
        return elementByteSize;
    }

    @Override
    public byte[] groupPow(byte[] g, byte[] n) {
        ECPoint g1 = ECPoint.fromByteArray(curve, g);
        BigInteger n1 = new BigInteger(n);
        if (logEncryptionMethods) {
            System.out.println("groupPow: " + g1.toString() +"^" + n1);
            System.out.println("Result: " + curve.multiply(g1, n1).toString());
        }
        return curve.multiply(g1, n1).toByteArray(elementByteSize / 2);
    }

    @Override
    public byte[] groupMult(byte[] g, byte[] q) {
        ECPoint g1 = ECPoint.fromByteArray(curve, g);
        ECPoint q1 = ECPoint.fromByteArray(curve, q);
        if (logEncryptionMethods) {
            System.out.println("groupMult of " + g1.toString()+ " and " + q1.toString());
            System.out.println("Result: " + curve.add(g1, q1).toString());
        }
        return curve.add(g1, q1).toByteArray(elementByteSize / 2);
    }

    @Override
    public byte[] completing(byte[] member) {
        ECPoint point = ECPoint.fromByteArray(curve, member);
        return point.completing().toByteArray(elementByteSize / 2);
    }

    // Returns the x value of a point which is a group member.
    public BigInteger getX(byte[] ecpoint) {
        ECPoint point = ECPoint.fromByteArray(curve, ecpoint);
        return point.getX();
    }

    // Returns the y value of a point which is a group member.
    public BigInteger getY(byte[] ecpoint) {
        ECPoint point = ECPoint.fromByteArray(curve, ecpoint);
        return point.getY();
    }
}
