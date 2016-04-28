package workshop;

/*
 * We only use the group interface, the cryptographic infrastructure should recode it to their dedicated use
 */
public abstract class Group {

    /**
     * Returns the generator exponated by 'exponent'
     */
    public abstract byte[] getElement(byte[] exponent);

    /**
     * Returns the order of the group.
     */
    public abstract byte[] getOrder();

    /**
     * returns the group's generator
     */
    public abstract byte[] getGenerator();

    /**
     * returns the group's element size in bytes
     */
    public abstract int getElementSize();

    /**
     * Returns a byte[] representation of g^n under the group.g is a member of the group,
     * n is the exponent.
     */
    public abstract byte[] groupPow(byte[] g, byte[] n);

    /**
     * Returns a byte[] representation of g*q under the group. g and q are both members of the group.
     */
    public abstract byte[] groupMult(byte[] g, byte[] q);

    public abstract byte[] completing(byte[] member);
}
