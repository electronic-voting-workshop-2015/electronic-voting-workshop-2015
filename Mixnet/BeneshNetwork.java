package workshop;

public class BeneshNetwork {
	public Vote[][] layers;
	public int length;
	public int depth;
	public mixnetUtils mix;

	public BeneshNetwork(Vote[] votes, mixnetUtils mix) {
		this.mix = mix;
		this.length = votes.length;
		this.depth = 0;
		for (int i = 1; i < length; i *= 2)
			depth++;
		this.layers = new Vote[this.depth * 2][];
		this.layers[0] = votes;
		for (int i = 1; i < layers.length; i++)
			this.layers[i] = new Vote[length];
		Permutation newPermutation = Permutation.generateRandomPermutation(this.length);
		BeneshNetworkLayer.INITIATE(this.length);
		BeneshNetworkLayer.UPDATE(newPermutation);
		this.updateFromLayers(0, this.length, 0);
	}

	public void updateFromLayers(int start, int length, int depth) {
		int half = length / 2;
		if (depth >= this.depth)
			return;
		for (int i = 0; i < half; i++) {
			int firstIndex = i + start;
			int secondIndex = firstIndex + half;
			boolean permute = BeneshNetworkLayer.getState(depth, start / 2 + i);
			mixVotes(depth, firstIndex, secondIndex, permute);
		}
		if (depth == this.depth - 1)
			return;
		updateFromLayers(start, length / 2, depth + 1);
		updateFromLayers(start + length / 2, length / 2, depth + 1);
		depth = 2 * this.depth - depth - 2;
		for (int i = 0; i < half; i++) {
			int firstIndex = i + start;
			int secondIndex = firstIndex + half;
			boolean permute = BeneshNetworkLayer.getState(depth, start / 2 + i);
			mixVotes(depth, firstIndex, secondIndex, permute);
		}
	}

	public void mixVotes(int depth, int firstIndex, int secondIndex, boolean permute) {
		Vote firstVote = this.layers[depth][firstIndex];
		Vote secondVote = this.layers[depth][secondIndex];
		Vote[] votes = { firstVote, secondVote };
		int[] index = { firstIndex, secondIndex };
		votes = this.mix.mix2Votes(votes, index, permute);
		this.layers[depth + 1][firstIndex] = votes[0];
		this.layers[depth + 1][secondIndex] = votes[1];
	}

	public String[][][] VotesToString() {
		String[][][] codes = new String[this.depth * 2][this.length][2];
		for (int i = 0; i < this.depth * 2; i++)
			for (int j = 0; j < this.length; j++) {
				codes[i][j] = this.layers[i][j].toArrString();
			}
		return codes;
	}
}