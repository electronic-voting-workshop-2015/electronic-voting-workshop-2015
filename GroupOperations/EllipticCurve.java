package GroupOperations;

import java.math.BigInteger;


public class EllipticCurve {
    //a and b parameters
    private final BigInteger a;
    private final BigInteger b;

    //the prime number, all operations are modulo p
    private final BigInteger p;

    private final ECGroupMember zeroMember = new ECGroupMember(this, BigInteger.ZERO, BigInteger.ZERO);

    private ECGroupMember generator = null;

    public EllipticCurve(BigInteger a, BigInteger b, BigInteger p) {
        this.a = a;
        this.b = b;
        this.p = p;
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

    public ECGroupMember getZeroMember() {
        return zeroMember;
    }

    public ECGroupMember getGenerator() {
        return generator;
    }

    public void setGenerator(ECGroupMember generator) {
        this.generator = generator;
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
