package workshop;

public class ToIddan {
/*
	public static void main(String[] args) {
		splitInpot("[{\"vote_id\":1,\"vote_value\":\"123456\",\"ballot_box\":10,\"serial_number\":23456,\"race_id\":1}"
				+ ",{\"vote_id\":2,\"vote_value\":\"538495\",\"ballot_box\":10,\"serial_number\":23456,\"race_id\":2}"
				+ ",{\"vote_id\":3,\"vote_value\":\"4572\",\"ballot_box\":10,\"serial_number\":23456,\"race_id\":3}]");
		
	}*/

	public static Vote[] splitInpot(String str) {
		str = str.replace("[{", "").replace("}]", "").replace(",", "");
		String[] splited = str.split("\\}\\{");
		Vote[] votes = new Vote[splited.length];
		for (int i = 0; i < splited.length; i++) {
			votes[i] = stringToVote(getFromInpot(splited[i]));
			System.out.println(votes[i].key.member.length);
		}
		return votes;
	}

	private static String getFromInpot(String str) {
		String[] splited = str.split(":");
		splited = splited[2].split("\"");
		return splited[1];
	}

	private static Vote stringToVote(String str) {
		byte[] a = GroupMember.stringToByte(str);
		byte[][] b = new byte[2][(a.length)/2];
		System.arraycopy(a, 0, b[0], 0, b[0].length);
		System.arraycopy(a, b[0].length, b[1], 0, b[1].length);
		Vote vote = new Vote(b);
		return vote;
		
	}

}
