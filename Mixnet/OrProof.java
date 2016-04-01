package workshop;

//mix_the_choclate
public class OrProof {
	public GroupMember[] a;
	public GroupMember[] b;
	public int challenge;
	public int[] r_power;
	public int[] d_power;

	public OrProof(GroupMember[] a, GroupMember[] b, int challenge, int[] d_power, int[] r_power) {
		super();
		this.a = a;
		this.b = b;
		this.challenge = challenge;
		this.d_power = d_power;
		this.r_power = r_power;
	}

	private String intToString(int n) {
		return (new GroupMember(History.intToByteArray(n))).toString();
	}

	public String[] ProofToString() {
		String[] arr = { this.a[0].member.toString(), this.b[0].toString(), this.a[1].toString(), this.b[1].toString(),
				intToString(this.challenge), intToString(this.r_power[0]), intToString(this.d_power[0]),
				intToString(this.r_power[1]), intToString(this.d_power[1]) };
		return arr;
	}
}
