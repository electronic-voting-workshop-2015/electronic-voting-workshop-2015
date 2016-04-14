package Vote;
import java.lang.String;
import java.math.BigInteger;
import java.nio.charset.Charset;
import java.util.Arrays;

public class StringVoteParser {

	private static final Charset UTF8_CHARSET = Charset.forName("UTF-8");
	
	public static Vote[] splitInput(String str) {
		str = str.replace("[{", "").replace("}]", "").replace(",", "");
		String[] splited = str.split("\\}\\{");
		Vote[] votes = new Vote[splited.length];
		for (int i = 0; i < splited.length; i++) {
			votes[i] = stringToVote(getFromInput(splited[i]));
		}
		return votes;
	}

	private static String getFromInput(String str) {
		String[] splited = str.split(":");
		splited = splited[2].split("\"");
		return splited[1];
	}
	
	private static byte[][] splitAtMiddle(byte[] array)
	{
		byte[][] splitted = new byte[2][];
		int half = array.length / 2;
		splitted[0] = Arrays.copyOfRange(array, 0, half);
		splitted[1] = Arrays.copyOfRange(array, half, half * 2);
		return splitted;
	}

	private static GroupMember xyToGroupMember(byte[] values) {
		byte[][] XY = splitAtMiddle(values);
		BigInteger x = new BigInteger(XY[0]);
		BigInteger y = new BigInteger(XY[1]);
		return MyGroup.getMember(x, y);
	}
	
	public static GroupMember stringToGroupMember(String str)
	{
		return xyToGroupMember(str.getBytes(UTF8_CHARSET));
	}
	
	private static Vote stringToVote(String str) {
		byte[][] components = splitAtMiddle(str.getBytes(UTF8_CHARSET));
		GroupMember key = xyToGroupMember(components[0]);
		GroupMember message = xyToGroupMember(components[1]);
		return new Vote(key, message);
	}

}