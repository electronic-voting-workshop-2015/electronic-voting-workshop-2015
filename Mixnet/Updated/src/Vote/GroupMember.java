package Vote;
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
}
