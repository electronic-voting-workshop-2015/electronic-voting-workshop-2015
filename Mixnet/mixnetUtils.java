package workshop;

import java.util.Random;

public class mixnetUtils {
	public Group group;
	public History history;
	public int groupSize;
	public GroupMember g, h;
	private Random rando;

	
	
	public mixnetUtils(Group group, GroupMember g, GroupMember h) {
		super();
		this.rando = new Random();
		this.group = group;
		this.history = new History();
		this.groupSize = ByteArrayToInt(group.getOrder());
		this.g = g;
		this.h = h;
	}

	public mixnetUtils() {
		super();
		this.rando = new Random();
		this.group = new MyGroup();
		this.history = new History();
		this.groupSize = ByteArrayToInt(group.getOrder());
		System.out.println(groupSize + "\n");
		this.g = new GroupMember(group.getGenerator());
		this.h = new GroupMember(group.groupPow(g.member, intToByteArray(42)));
	}

	public static int ByteArrayToInt(byte[] bytes) {
		return MyGroup.groupMemberToInt(bytes);
		//return bytes[0] << 24 | (bytes[1] & 0xFF) << 16 | (bytes[2] & 0xFF) << 8 | (bytes[3] & 0xFF);
	}

	public static byte[] intToByteArray(int value) {
		return MyGroup.intToGroupMember(value);
		//return new byte[] { (byte) (value >>> 24), (byte) (value >>> 16), (byte) (value >>> 8), (byte) value };
	}

	public Vote[] mix2Votes(Vote[] votes, int index[], boolean permutation) {
		int[] keys = new int[2];
		Vote[] reEncryptedVotes = new Vote[2];
		for (int i = 0; i < keys.length; i++)
			keys[i] = rand();
		reEncryptedVotes = re_encrypt(keys, votes, permutation);

		OrProof[] profs_2and3 = makeProfs(reEncryptedVotes, votes, keys, permutation);
		OrProof[] profs_1and4 = makeProfs(votes, reEncryptedVotes, invers(keys), permutation);
		OrProof[] profs = { profs_2and3[0], profs_2and3[1], profs_1and4[0], profs_1and4[1] };
		AndProof allProfs = new AndProof(profs, index[0], index[1]);
		ArrayAndProf.add(allProfs);
		return reEncryptedVotes;
	}

	private int[] invers(int[] keys) {
		int[] inv = new int[keys.length];
		for (int i = 0; i < inv.length; i++) {
			inv[i] = groupSize - keys[i];
		}
		return inv;
	}

	private OrProof[] makeProfs(Vote[] reEncryptedVotes, Vote[] votes, int[] keys, boolean permutation) {

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

	private OrProof makeProf(Vote[] reEncryptedVotes, Vote vote, int key, int place) {

		int[] r = new int[reEncryptedVotes.length];
		int[] d = new int[reEncryptedVotes.length];
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
		int c = hash(history.getHistory());
		int d_tag = updateD(c, d, place);
		int r_tag = updateR(key, d[place], d_tag);
		d[place] = d_tag;
		r[place] = r_tag;
		history.UpdateHistory(d, r);
		OrProof prof = new OrProof(a, b, c, d, r);
		return prof;
	}

	private int hash(Byte[] obj) {
		return obj.hashCode() % groupSize;
	}

	private int rand() {
		return rando.nextInt(groupSize);
	}

	private int updateR(int r, int d, int d_tag) {

		return r * (d - d_tag);
	}

	private int updateD(int c, int[] d, int place) {
		int d_tag = c;
		for (int i = 0; i < d.length; i++) {
			if (i != place)
				d_tag -= d[i];
		}
		return d_tag;
	}

	private GroupMember compute(GroupMember xi, GroupMember x, GroupMember g, int di, int ri) {
		return new GroupMember(group.groupMult(
				group.groupPow(group.groupMult(xi.member, group.completing(x.member)), intToByteArray(di)),
				group.groupPow(g.member, intToByteArray(ri))));
	}

	private Vote[] re_encrypt(int[] keys, Vote[] votes, boolean permutation) {
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

	private byte[] getUnit() {
		return group.getElement(intToByteArray(0));
	}

	private Vote encrypt(byte[] msg, int i) {
		GroupMember key = new GroupMember(group.groupPow(g.member, intToByteArray(i)));
		GroupMember message = new GroupMember(group.groupMult(msg, group.groupPow(h.member, intToByteArray(i))));
		Vote vote = new Vote(key, message);
		return vote;
	}
	private Vote encMult(Vote enc1, Vote enc2) {
		byte[][] mul = new byte[2][];
		mul[0] = group.groupMult(enc1.key.member, enc2.key.member);
		mul[1] = group.groupMult(enc1.message.member, enc2.message.member);
		return new Vote(mul);
	}
}
