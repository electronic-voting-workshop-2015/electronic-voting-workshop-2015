package Proof;

import java.math.BigInteger;
import java.util.Random;

import Vote.MyGroup;
import Vote.GroupMember;
import Vote.Vote;

public class mixnetUtils {
	public History history;
	public BigInteger groupSize;
	public GroupMember g, h;
	private Random rando;

	
	
	public mixnetUtils(GroupMember g, GroupMember h) {
		super();
		this.rando = new Random();
		this.history = new History();
		this.groupSize = new BigInteger(MyGroup.getOrder());
		this.g = g;
		this.h = h;
	}

	private static byte[] intToByteArray(int value) {
		int length = 1;
		for (long i = 1; i <= value; i *= 128)
			length++;
		byte[] result = new byte[length];
		int i = length - 1;
		while (value > 0)
		{
			result[i] = (byte) (value % 128);
			value /= 128;
			i--;
		}
		return result;
	}
	
	public Vote[] mix2Votes(Vote[] votes, int index[], boolean permutation) {
		BigInteger[] keys = new BigInteger[2];
		Vote[] reEncryptedVotes = new Vote[2];
		for (int i = 0; i < keys.length; i++)
			keys[i] = rand();
		reEncryptedVotes = re_encrypt(keys, votes, permutation);

		OrProof[] profs_2and3 = makeProfs(reEncryptedVotes, votes, keys, permutation);
		OrProof[] profs_1and4 = makeProfs(votes, reEncryptedVotes, inverse(keys), permutation);
		OrProof[] profs = { profs_2and3[0], profs_2and3[1], profs_1and4[0], profs_1and4[1] };
		AndProof allProfs = new AndProof(profs, index[0], index[1]);
		ArrayAndProf.add(allProfs);
		return reEncryptedVotes;
	}

	private BigInteger[] inverse(BigInteger[] keys) {
		BigInteger[] inv = new BigInteger[keys.length];
		for (int i = 0; i < inv.length; i++) {
			inv[i] = groupSize.subtract(keys[i]);
		}
		return inv;
	}

	private OrProof[] makeProfs(Vote[] reEncryptedVotes, Vote[] votes, BigInteger[] keys, boolean permutation) {

		OrProof[] profs = new OrProof[votes.length];
		for (int i = 0; i < profs.length; i++) {
			profs[i] = makeProf(reEncryptedVotes, votes[i], keys[i], permute(i, permutation));
		}

		return profs;
	}

	private int permute(int i, boolean permutation) {
		if (permutation)
			return 1 - i;
		return i;
	}

	private OrProof makeProf(Vote[] reEncryptedVotes, Vote vote, BigInteger key, int place) {

		BigInteger[] r = new BigInteger[reEncryptedVotes.length];
		BigInteger[] d = new BigInteger[reEncryptedVotes.length];
		GroupMember[] a = new GroupMember[reEncryptedVotes.length];
		GroupMember[] b = new GroupMember[reEncryptedVotes.length];
		for (int i = 0; i < r.length; i++)
			r[i] = rand();
		for (int i = 0; i < d.length; i++)
			d[i] = rand();
		for (int i = 0; i < a.length; i++) {
			a[i] = compute(reEncryptedVotes[i].key, vote.key, g, d[i], r[i]);
		}
		for (int i = 0; i < b.length; i++) {
			b[i] = compute(reEncryptedVotes[i].message, vote.message, h, d[i], r[i]);
		}
		history.UpdateHistory(reEncryptedVotes, vote, a, b);
		BigInteger c = hash(history.getHistory());
		BigInteger d_tag = updateD(c, d, place);
		BigInteger r_tag = updateR(key, d[place], d_tag);
		d[place] = d_tag;
		r[place] = r_tag;
		history.UpdateHistory(d, r);
		OrProof prof = new OrProof(a, b, c, d, r);
		return prof;
	}

	private BigInteger hash(Byte[] obj) {
		return new BigInteger(intToByteArray(obj.hashCode()));
	}

	private BigInteger rand() {
		int r = rando.nextInt();
		if (r < 0)
			r = -r;
		return new BigInteger(intToByteArray(r));
	}

	private BigInteger updateR(BigInteger r, BigInteger d, BigInteger d_tag) {

		return r.multiply(d.subtract(d_tag));
	}

	private BigInteger updateD(BigInteger c, BigInteger[] d, int place) {
		BigInteger d_tag = c;
		for (int i = 0; i < d.length; i++) {
			if (i != place)
				d_tag = d_tag.subtract(d[i]);
		}
		return d_tag;
	}

	private GroupMember compute(GroupMember xi, GroupMember x, GroupMember g, BigInteger di, BigInteger ri) {
		return MyGroup.groupMult(
				MyGroup.groupPow(MyGroup.groupMult(xi, MyGroup.completing(x)), di.toByteArray()),
				MyGroup.groupPow(g, ri.toByteArray()));
	}

	private Vote[] re_encrypt(BigInteger[] keys, Vote[] votes, boolean permutation) {
		Vote[] reEncryptedVotes = new Vote[votes.length];
		for (int i = 0; i < votes.length; i++)
		{
			Vote temp = encrypt(getUnit(), keys[i]);
			reEncryptedVotes[i] = encMult(votes[i], temp);
		}
		if (permutation) {
			Vote temp = reEncryptedVotes[0];
			reEncryptedVotes[0] = reEncryptedVotes[1];
			reEncryptedVotes[1] = temp;
		}
		return reEncryptedVotes;
	}

	private GroupMember getUnit() {
		return MyGroup.getElement(intToByteArray(0));
	}

	private Vote encrypt(GroupMember msg, BigInteger i) {
		GroupMember key = MyGroup.groupPow(g, i.toByteArray());
		GroupMember message = MyGroup.groupMult(msg, MyGroup.groupPow(h, i.toByteArray()));
		Vote vote = new Vote(key, message);
		return vote;
	}
	
	private Vote encMult(Vote enc1, Vote enc2) {
		GroupMember[] mul = new GroupMember[2];
		mul[0] = MyGroup.groupMult(enc1.key, enc2.key);
		mul[1] = MyGroup.groupMult(enc1.message, enc2.message);
		return new Vote(mul);
	}
}
