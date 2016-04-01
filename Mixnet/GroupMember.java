package workshop;
import java.nio.charset.Charset;


public class GroupMember {
	public byte[] member;
	private static final Charset UTF8_CHARSET = Charset.forName("UTF-8");
	
	public GroupMember(byte[] member) {
		super();
		this.member = member;
	}
	
	public String toString()
	{
		return new String(member, UTF8_CHARSET);
	}

	public static byte[] stringToByte(String string) {
	    return string.getBytes(UTF8_CHARSET);
	}
	public boolean isZero()
	{
		for (int i = 0; i < this.member.length; i++)
			if (this.member[i] != 0)
				return false;
		return true;
	}
}
