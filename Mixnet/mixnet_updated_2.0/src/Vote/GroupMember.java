//package Vote;
package Vote;
import java.nio.charset.Charset;
import java.util.Arrays;
import java.util.Base64;

public class GroupMember {
	public byte[] member;
	private static final Charset ISO_CHARSET = Charset.forName("ISO-8859-1");
	
	public GroupMember(byte[] member) {
		super();
		this.member = member;
	}
	
    public boolean equals(GroupMember g) {
       return Arrays.equals(this.member, g.member);
    }
	
	public String toString()
	{
		byte[] member64 = Base64.getEncoder().encode(member);
		return new String(member64, ISO_CHARSET);
	}
}