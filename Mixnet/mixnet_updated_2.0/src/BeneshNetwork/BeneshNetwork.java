package BeneshNetwork;

import Proof.mixnetUtils;
import Vote.Vote;

public class BeneshNetwork {
	public Vote[][] layers;
	public int length;
	public int depth;
	public mixnetUtils mix;

	public BeneshNetwork(Vote[] votes, mixnetUtils mix) {
		this.mix = mix;
		this.pad(votes);
		Permutation newPermutation = Permutation.generateRandomPermutation(this.length);
		BeneshNetworkLayer.INITIATE(this.length);
		BeneshNetworkLayer.UPDATE(newPermutation);
		this.updateFromLayers(0, this.length, 0);
	}
	
	public void pad(Vote[] votes)
	{
		this.length = votes.length - 1;
		this.depth = 0;
		for (int i = 1; i < this.length; i *= 2)
			depth++;
		this.length = 1;
		for (int i = 0; i < this.depth; i++)
			this.length *= 2;
		// now we know the real length and depth
		this.layers = new Vote[this.depth * 2][];
		for (int i = 0; i < layers.length; i++)
			this.layers[i] = new Vote[this.length];
		for (int i = 1; i < votes.length; i++)
			this.layers[0][i - 1] = votes[i];
		for (int i = votes.length - 1; i < this.length; i++)
			this.layers[0][i] = votes[0];
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