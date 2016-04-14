package Proof;


public class AndProof {
	public OrProof[] proofs;
	public int index1;
	public int index2;

	public AndProof(OrProof[] proofs, int index1, int index2) {
		super();
		this.proofs = proofs;
		this.index1 = index1;
		this.index2 = index2;
	}

	public String[][] ProofToString() {
		String[][] arr = new String[4][];
		for (int i = 0; i < arr.length; i++) {
			arr[i] = proofs[i].ProofToString();
		}
		return arr;
	}
}
