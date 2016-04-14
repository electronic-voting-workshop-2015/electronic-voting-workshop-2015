package Vote;

import java.math.BigInteger;

import OtherTeams.Group;

public class MyGroup {
	public static Group group;
	
	public static void INITIATE(Group group)
	{
		MyGroup.group = group;
	}
	
	public static byte[] getOrder()
	{
		return group.getOrder();
	}
	
	public static GroupMember getMember(BigInteger x, BigInteger y)
	{
		return new GroupMember(MyGroup.group.getMember(x, y));
	}
	
	public static GroupMember groupMult(GroupMember a, GroupMember b)
	{
		return new GroupMember(group.groupMult(a.member, b.member));
	}
	
	public static GroupMember getElement(byte[] exponent)
	{
		return new GroupMember(group.getElement(exponent));
	}
	
	public static GroupMember getGenerator()
	{
		return new GroupMember(group.getGenerator());
	}
	
	public static int getElementSize()
	{
		return group.getElementSize();
	}
	
	public static GroupMember groupPow(GroupMember g, byte[] n)
	{
		return new GroupMember(group.groupPow(g.member, n));
	}
	
	public static GroupMember completing(GroupMember member)
	{
		return new GroupMember(group.completing(member.member));
	}
}
