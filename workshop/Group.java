package workshop;
/*
 * We only use the group interface, the cryptographic infrastructure should recode it to their dedicated use
 */
public abstract class Group{
	//  groupOrder is the order of the chosen group	
	public byte[] groupOrder;
	//  generator is the pre-chosen generator
	public byte[] generator;
	//   elementBitSize is the size in bits of each element in the group
	public int elementBytesSize;
	/**
	 * Returns the generator exponated by 'exponent'
	 * @param exponent
	 * @return
	 */
	public abstract byte[] getElement(byte[] exponent);	
	/**
	 * sets the group's order to 'order'
	 * @param order
	 */
	public abstract void setOrder(byte[] order);
	/**
	 * returns the group's order
	 * @return
	 */	
	public abstract byte[] getOrder();
	/**
	 * sets the group's generator to 'generator'
	 * @param generator
	 */
	public abstract void setGenerator(byte[] generator);
	/**
	 * returns the group's generator
	 * @return
	 */
	public abstract byte[] getGenerator();
	/**
	 * returns the group's element size in bytes
	 */
	public abstract int getElementSize();
	/**
	 * Sets the group's element size in bytes to 'size'
	 */
	public abstract void setElementSize(int size);
}	
