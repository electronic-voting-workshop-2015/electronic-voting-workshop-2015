//package Vote;
package Vote;

import java.lang.String;
import java.math.BigInteger;
import java.nio.charset.Charset;
import java.util.Arrays;

import org.json.*;

import java.util.Base64;


public class StringVoteParser {

	private static final Charset ISO_CHARSET = Charset.forName("ISO-8859-1");
	
	public static Vote[] splitInput(String str) throws JSONException {
		
		JSONArray jsonVotes = new JSONArray(str);
		Vote[] votes = new Vote[jsonVotes.length()];
		
		for (int i = 0; i < jsonVotes.length(); i++) {
			//System.out.println(jsonVotes.getJSONObject(i).get("vote_value"));
			String s = jsonVotes.getJSONObject(i).get("vote_value").toString();
			//System.out.println(s.length());
			votes[i] = stringToVote(s);
		}
		return votes;
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
	
	public static GroupMember stringToGroupMember(String str) {
		byte[] decoded = Base64.getDecoder().decode(str.getBytes(ISO_CHARSET));
		return xyToGroupMember(decoded);
	}
	
	private static Vote stringToVote(String str) {
		byte[][] components = splitAtMiddle(Base64.getDecoder().decode(str.getBytes(ISO_CHARSET)));
		GroupMember key = new GroupMember(components[0]);
		GroupMember message = new GroupMember(components[1]);
		return new Vote(key, message);
	}

}