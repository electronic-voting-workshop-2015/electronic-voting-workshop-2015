package Proof;

import java.math.BigInteger;

import Vote.GroupMember;

//mix_the_choclate
public class OrProof {
	public GroupMember[] a;
	public GroupMember[] b;
	public BigInteger challenge;
	public BigInteger[] r_power;
	public BigInteger[] d_power;

	public OrProof(GroupMember[] a, GroupMember[] b, BigInteger challenge, BigInteger[] d_power, BigInteger[] r_power) {
		super();
		this.a = a;
		this.b = b;
		this.challenge = challenge;
		this.d_power = d_power;
		this.r_power = r_power;
	}
/*
	private String intToString(int n) {
		return (new GroupMember(History.intToByteArray(n))).toString();
	}
*/

	private String BigIntegerToString(BigInteger n) {
		return (new GroupMember(n.toByteArray())).toString();
	}

	public String[] ProofToString() {
		String[] arr = { this.a[0].member.toString(), this.b[0].toString(), this.a[1].toString(), this.b[1].toString(),
				BigIntegerToString(this.challenge), BigIntegerToString(this.r_power[0]),
				BigIntegerToString(this.d_power[0]), BigIntegerToString(this.r_power[1]),
				BigIntegerToString(this.d_power[1]) };
		return arr;
	}
}
