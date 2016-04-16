//package Vote;
package Vote;
import java.nio.charset.Charset;
import java.util.Base64;

public class GroupMember {
	public byte[] member;
	private static final Charset UTF8_CHARSET = Charset.forName("UTF-8");
	
	public GroupMember(byte[] member) {
		super();
		this.member = member;
	}
	
	public String toString()
	{
		byte[] member64 = Base64.getEncoder().encode(member);
		return new String(member64, UTF8_CHARSET);
	}
}
