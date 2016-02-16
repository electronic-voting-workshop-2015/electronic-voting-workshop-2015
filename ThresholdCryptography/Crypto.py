package GroupOperations;


import java.math.BigInteger;

//implements a member of an Elliptic Curve group.
//algorithms from http://andrea.corbellini.name/2015/05/23/elliptic-curve-cryptography-finite-fields-and-discrete-logarithms
public class ECGroupMember extends GroupMember {

    private final EllipticCurve curve;
    private final BigInteger x;
    private final BigInteger y;

    public ECGroupMember(EllipticCurve curve, BigInteger x, BigInteger y) {
        this.curve = curve;
        this.x = x;
        this.y = y;
    }

    public EllipticCurve getCurve() {
        return curve;
    }

    public BigInteger getX() {
        return x;
    }

    public BigInteger getY() {
        return y;
    }

    public static ECGroupMember groupAdd(ECGroupMember g1, ECGroupMember g2) {
        //check both points are from the same curve
        if (!(g1.getCurve().equals(g2.getCurve()))) {
            throw new IllegalArgumentException("The group members are not from the same curve");
        }

        //one of points is zero
        if (g1.equals(g1.getCurve().getZeroMember())) {
            return g2;
        }
        if (g2.equals(g2.getCurve().getZeroMember())) {
            return g1;
        }
        //two points are symmetric (complements)
        if (g1.getX().equals(g2.getX()) && g1.getY().equals(g2.getY().negate())) {
            return g1.getCurve().getZeroMember();
        }

        BigInteger x1 = g1.getX();
        BigInteger x2 = g2.getX();
        BigInteger y1 = g1.getY();
        BigInteger y2 = g2.getY();
        BigInteger p = g1.getCurve().getP();
        BigInteger a = g1.getCurve().getA();
        BigInteger two = BigInteger.valueOf((long) 2);
        BigInteger three = BigInteger.valueOf((long) 3);
        BigInteger temp1;
        BigInteger temp2;

        //compute slope
        BigInteger m;
        if (g1.equals(g2)) {
            temp1 = x1.pow(2).multiply(three).add(a).mod(p);
            temp2 = y1.multiply(two).modInverse(p);
            m = temp1.multiply(temp2).mod(p);
        }
        else {
            temp1 = y1.subtract(y2).mod(p);
            temp2 = x1.subtract(x2).modInverse(p);//TODO: handle case when x1 = x2 and y1 != +-y2
            m = temp1.multiply(temp2).mod(p);
        }

        //compute result
        BigInteger xr, yr;
        xr = m.modPow(two, p).subtract(x1).subtract(x2).mod(p);
        yr = y2.negate().mod(p);
        yr = yr.add(m.multiply(x2.subtract(xr))).mod(p);

        return new ECGroupMember(g1.getCurve(), xr, yr);
    }

    public static ECGroupMember groupMultiply(ECGroupMember g, BigInteger n) {
        String binaryRep = n.toString(2);
        ECGroupMember res = g.getCurve().getZeroMember();
        ECGroupMember addend = g;

        for (int i = binaryRep.length() - 1; i >= 0; i--) {
            if (binaryRep.charAt(i) == '1') {
                res = groupAdd(res, addend);
            }
            addend = groupAdd(addend, addend);
        }
        return res;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        ECGroupMember that = (ECGroupMember) o;

        if (getCurve() != null ? !getCurve().equals(that.getCurve()) : that.getCurve() != null) return false;
        if (getX() != null ? !getX().equals(that.getX()) : that.getX() != null) return false;
        return getY() != null ? getY().equals(that.getY()) : that.getY() == null;

    }



}
