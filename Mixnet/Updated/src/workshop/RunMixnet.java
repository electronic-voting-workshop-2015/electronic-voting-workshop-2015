package workshop;

import java.math.BigInteger;
import java.util.Random;

import org.json.JSONException;

import OtherTeams.Group;
import OtherTeams.ParametersMain;
import Proof.ArrayAndProf;
import Proof.mixnetUtils;
import Vote.GroupMember;
import Vote.MyGroup;
import Vote.StringVoteParser;
import Vote.Vote;
import BeneshNetwork.BeneshNetwork;
import ECCryptography.ECGroup;
import ECCryptography.ECPoint;
import ECCryptography.EllipticCurve;

public class RunMixnet {
	
	public static void InitializeGroupInfo()
	{
		//THEIR CODE
		BigInteger a_curve = new BigInteger("-3");
        BigInteger b_curve = new BigInteger("5ac635d8aa3a93e7b3ebbd55769886bc651d06b0cc53b0f63bce3c3e27d2604b", 16);
        BigInteger p_curve = new BigInteger("115792089210356248762697446949407573530086143415290314195533631308867097853951");
        EllipticCurve curve = new EllipticCurve(a_curve, b_curve, p_curve);
        BigInteger generatorX = new BigInteger("6b17d1f2e12c4247f8bce6e563a440f277037d812deb33a0f4a13945d898c296", 16);
        BigInteger generatorY = new BigInteger("4fe342e2fe1a7f9b8ee7eb4a7c0f9e162bce33576b315ececbb6406837bf51f5", 16);
        ECPoint genrator = new ECPoint(curve, generatorX, generatorY);
        int integerSize = 256 / 4; // IT WAS 8
        BigInteger order = new BigInteger("115792089210356248762697446949407573529996955224135760342422259061068512044368");
        ECGroup eccgroup = new ECGroup(curve.toByteArray(), genrator.toByteArray(integerSize), integerSize * 2 + 2, order.toByteArray());
        
        // OUR CODE
		Group group = (Group)eccgroup;
        MyGroup.INITIATE(group);
	}
	
	public static Random rand = new Random();
	public static GroupMember getRandomGroupMember(int[] order, byte[] random)
	{
		int r;
		for (int i = 0; i < random.length; i++)
		{
			r = rand.nextInt(order[i]);
			if (r >= 128)
				r -= 128;
			random[i] = (byte)r;
		}
		return MyGroup.getElement(random);
	}
	public static Vote getRandomVote(int[] order, byte[] random)
	{
		GroupMember key = getRandomGroupMember(order, random);
		GroupMember message = getRandomGroupMember(order, random);
		return new Vote(key, message);
	}
	public static Vote[] getRandomVotes()
	{
		byte[] order = MyGroup.getOrder();
		byte[] random = new byte[order.length];
		int[] positiveOrder = new int[order.length];
		for (int i = 0; i < positiveOrder.length; i++)
		{
			positiveOrder[i] = (int)order[i];
			if (positiveOrder[i] < 0)
				positiveOrder[i] += 128;
			positiveOrder[i]++;
		}
		Vote[] votes = new Vote[15];
		for (int i = 0; i < votes.length; i++)
			votes[i] = getRandomVote(positiveOrder, random);
		return votes;
	}
	
	public static void main(String[] args) {
		//InitializeGroupInfo();
		System.out.println("Start Server");
		String filepath = "./adminJson";
		String s = ExecuteShellCommand.executeCommand("wget -q  -O- http://46.101.148.106:4567/retrieveParametersFile");
		CreateJsonv2.stringToFile(filepath, s);
		
		filepath = "./publicKey";
		s = ExecuteShellCommand.executeCommand("wget -q  -O- http://46.101.148.106:4567/retrieveVotingPublicKey");
		CreateJsonv2.stringToFile(filepath, s);
		
		filepath = "./Proofs.json";
		s = ExecuteShellCommand.executeCommand("wget -q  -O- http://46.101.148.106:4567/getBBVotes/-1");
		System.out.println(s);
		Vote[] votes = StringVoteParser.splitInput(s);
		System.out.println("Done Server");
		System.out.println("Start Random Votes");
		//Vote[] votes = getRandomVotes();
		System.out.println("Done Random Votes");
        
		ParametersMain.init();
		
		//Group group = ParametersMain.ourGroup;
		GroupMember g = MyGroup.getGenerator();
		//GroupMember h = new GroupMember(ParametersMain.publicKey);
		GroupMember h = StringVoteParser.stringToGroupMember("iL9cHppfhTzSoE4gNA+SvUUD7Hkk92uqs22Ohfc89lr2ZhuqwilcHcRElXQtuMCS7hNLh56xqsX6TVupgtjO1A==");
		mixnetUtils mix = new mixnetUtils(g, h);// need to be ****mixnetUtils(Group group, GroupMember g, GroupMember h)**** byte[]
		BeneshNetwork layers = new BeneshNetwork(votes, mix);
		int n = layers.depth;
		String[][][] codes = layers.VotesToString();
		try {
			System.out.println(n);
			CreateJsonv2.createJsonFile(filepath, n, codes, ArrayAndProf.ProofToString(n));
		} catch (JSONException e) {
			e.printStackTrace();
		}
		
		System.out.println("GREAT");
		//ExecuteShellCommand.commandWithFileContent(filepath, "wget -q --header \"Content-Type: application/json\" --post-data='", "'  -O- http://46.101.148.106:4567/PostProofsFile");
		
	}
	
	/*
	public static void bain(String[] args) {
		String filepath = "../../Json_Folder/Proofs.json";
		String s = "[{\"vote_id\":1,\"vote_value\":\"3355443233554432\",\"ballot_box\":10,\"serial_number\":23456,\"race_id\":1}"
				+ ",{\"vote_id\":2,\"vote_value\":\"3355443333554433\",\"ballot_box\":10,\"serial_number\":23456,\"race_id\":2}"
				+ ",{\"vote_id\":3,\"vote_value\":\"3355443433554434\",\"ballot_box\":10,\"serial_number\":23456,\"race_id\":3}"
				+ ",{\"vote_id\":4,\"vote_value\":\"3355443533554435\",\"ballot_box\":10,\"serial_number\":23456,\"race_id\":4}]";
		Vote[] votes = ToIddan.splitInpot(s);
		BeneshNetwork layers = new BeneshNetwork(votes, new mixnetUtils());
		CreateJson yotam = new CreateJson();
		int n = layers.depth;
		String[][][] codes = layers.VotesToString();
		yotam.createJsonFile(filepath, n, codes, ArrayAndProf.ProofToString(n));
	}
	*/

}
