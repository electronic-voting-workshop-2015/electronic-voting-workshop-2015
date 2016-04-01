package workshop;

/*
 * We only use the group interface, the cryptographic infrastructure should recode it to their dedicated use
 */
public class MyGroup extends Group{
	private final static int p = 97;
	private final static int generator = 5;
	private static final int length = 8;

	public byte[] getUnit(){
		byte[] one = new byte[length];
		one[0] = 1;
		return one;
	}
	public static byte[] intToGroupMember(int number)
	{
		byte[] value = new byte[length];
		for (int i = length - 1; i >= 0 && number > 0; i--, number /= 128)
			value[i] = (byte)(number % 128);
		return value;
	}
	public static int groupMemberToInt(byte[] g)
	{
		int number = 0;
		for (int i = 0; i < length; i++)
		{
			number *= 128;
			number += g[i];
			number %= p;
		}
		return number % p;
	}
	public byte[] getElement(byte[] exponent)
	{
		return groupPow(intToGroupMember(generator), exponent);
	}
	public byte[] getOrder()
	{
		return intToGroupMember(p - 1);
	}
	public byte[] getGenerator()
	{
		return intToGroupMember(generator);
	}
	public int getElementSize()
	{
		return length;
	}
	public byte[] groupPow(byte[] g, byte[] n)
	{
		int power = groupMemberToInt(n);
		power %= (p - 1);
		return intToGroupMember(intPower(groupMemberToInt(g), power));
	}
	private static int intPower(int g, int power)
	{
		if (power == 0)
			return 1;
		if (power == 1)
			return g;
		if (power % 2 == 1)
			return (intPower(g, power - 1) * g) % p;
		int result = intPower(g, power / 2);
		return (result * result) % p;
	}
	public byte[] groupMult(byte[] g, byte[] q)
	{
		return intToGroupMember((groupMemberToInt(g) * groupMemberToInt(q)) % p);
	}
	public byte[] completing(byte[] member)
	{
		return intToGroupMember(intPower(groupMemberToInt(member), p - 2));
	}
}
