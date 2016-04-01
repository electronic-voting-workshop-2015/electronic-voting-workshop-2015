package workshop;

import java.util.ArrayList;

public class ArrayAndProf {
	public static ArrayList<AndProof> proofs = new ArrayList<AndProof>();

	public static void add(AndProof proof) {
		proofs.add(proof);
	}
	public static String[][][][] ProofToString(int n) {
		int index = 0;
		String[][][][] arr = new String[2*n-1][2^(n-1)][][];
		for (int i = 0; i < arr.length; i++) {
			for (int j = 0; j < arr[i].length; j++) {
				arr[i][j] = proofs.get(index).ProofToString();
			}
		}
		return arr;
	}

}
