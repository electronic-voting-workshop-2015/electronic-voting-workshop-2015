package Proof;

import java.math.BigInteger;
import java.util.ArrayList;

import Vote.GroupMember;
import Vote.Vote;

public class History {
	private ArrayList<Byte> history = new ArrayList<Byte>();

	public Byte[] getHistory() {
		return (Byte[]) history.toArray(new Byte[history.size()]);
	}

	public void UpdateHistory(BigInteger[] d, BigInteger[] r) {
		history.addAll(toArrayListByte(d));
		history.addAll(toArrayListByte(r));
	}
	
	private ArrayList<Byte> toArrayListByte(BigInteger[] d) {
		ArrayList<Byte> list = new ArrayList<Byte>();
		for (int i = 0; i < d.length; i++) {
			list.addAll(toArrayListByte(d[i].toByteArray()));
		}
		return list;
	}

	public void UpdateHistory(byte[][][] reEncryptedVotes, byte[][] vote,
			byte[][] a, byte[][] b) {
		history.addAll(toArrayListByte(reEncryptedVotes));
		history.addAll(toArrayListByte(vote));
		history.addAll(toArrayListByte(a));
		history.addAll(toArrayListByte(b));
	}

	public void UpdateHistory(int[] d, int[] r) {
		history.addAll(toArrayListByte(d));
		history.addAll(toArrayListByte(r));
	}
	
	public void UpdateHistory(GroupMember[] d, GroupMember[] r) {
		history.addAll(toArrayListByte(d));
		history.addAll(toArrayListByte(r));
	}
	
	public void UpdateHistory(Vote[] reEncryptedVotes, Vote vote, GroupMember[] a, GroupMember[] b) {
		history.addAll(toArrayListByte(reEncryptedVotes));
		history.addAll(toArrayListByte(vote.getArr()));
		history.addAll(toArrayListByte(a));
		history.addAll(toArrayListByte(b));
		
	}
	
	private ArrayList<Byte> toArrayListByte(GroupMember[] arr) {
		ArrayList<Byte> list = new ArrayList<Byte>();
		for (int i = 0; i < arr.length; i++) {
			list.addAll(toArrayListByte(arr[i].member));
		}
		
		return list;
	}
	
	private ArrayList<Byte> toArrayListByte(Vote[] arr) {
		ArrayList<Byte> list = new ArrayList<Byte>();
		for (int i = 0; i < arr.length; i++) {
			list.addAll(toArrayListByte(arr[i].getArr()));
		}
		
		return list;
	}

	private   ArrayList<Byte> toArrayListByte(int[] arr) {
		ArrayList<Byte> list = new ArrayList<Byte>();
		for (int i = 0; i < arr.length; i++) {
			list.addAll(toArrayListByte(intToByteArray(arr[i])));
		}
		
		return list;
	}
	
	private   ArrayList<Byte> toArrayListByte(byte[] arr) {
		ArrayList<Byte> list = new ArrayList<Byte>();
		for (int i = 0; i < arr.length; i++) {
			list.add((Byte)(arr[i]));
		}
		
		return list;
	}
	
	private   ArrayList<Byte> toArrayListByte(byte[][] arr) {
		ArrayList<Byte> list = new ArrayList<Byte>();
		for (int i = 0; i < arr.length; i++) {
			list.addAll(toArrayListByte(arr[i]));
		}
		
		return list;
	}

	private   ArrayList<Byte> toArrayListByte(byte[][][] arr) {
		ArrayList<Byte> list = new ArrayList<Byte>();
		for (int i = 0; i < arr.length; i++) {
			list.addAll(toArrayListByte(arr[i]));
		}
		
		return list;
	}

	public static  byte[] intToByteArray(int value) {
	    return new byte[] {
	            (byte)(value >>> 24),
	            (byte)(value >>> 16),
	            (byte)(value >>> 8),
	            (byte)value};
	}



	
}
