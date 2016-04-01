package workshop;

public class RunMixnet {
	
	
	
	public static void bain(String[] args) {
		String s = ExecuteShellCommand.executeCommand("wget -q  -O- http://localhost:4567/getBBVotes/-1");
		Vote[] votes = ToIddan.splitInpot(s);
		mixnetUtils mix = new mixnetUtils();// need to be ****mixnetUtils(Group group, GroupMember g, GroupMember h)****
		BeneshNetwork layers = new BeneshNetwork(votes, mix);
		CreateJson yotam = new CreateJson();
		int n = layers.depth;
		String[][][] codes = layers.VotesToString();
		yotam.createJsonFile(n, codes, ArrayAndProf.ProofToString(n));
		ExecuteShellCommand.commandWithFileContent("", "wget -q --header \"Content-Type: application/json\" --post-data='{\"vote_id\": 32, \"race_id\": 1, \"party_id\": 1, zkp: ", ", \"signature\": \"ekjfkjansfads\"}'  -O- http://localhost:4567/publishZKP");
	}
	public static void main(String[] args) {
		String s = "[{\"vote_id\":1,\"vote_value\":\"3355443233554432\",\"ballot_box\":10,\"serial_number\":23456,\"race_id\":1}"
				+ ",{\"vote_id\":2,\"vote_value\":\"3355443333554433\",\"ballot_box\":10,\"serial_number\":23456,\"race_id\":2}"
				+ ",{\"vote_id\":3,\"vote_value\":\"3355443433554434\",\"ballot_box\":10,\"serial_number\":23456,\"race_id\":3}"
				+ ",{\"vote_id\":4,\"vote_value\":\"3355443533554435\",\"ballot_box\":10,\"serial_number\":23456,\"race_id\":4}]";
		Vote[] votes = ToIddan.splitInpot(s);
		BeneshNetwork layers = new BeneshNetwork(votes, new mixnetUtils());
		CreateJson yotam = new CreateJson();
		int n = layers.depth;
		String[][][] codes = layers.VotesToString();
		yotam.createJsonFile(n, codes, ArrayAndProf.ProofToString(n));
	}

}
