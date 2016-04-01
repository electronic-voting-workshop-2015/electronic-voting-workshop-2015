package workshop;

public class HoldProfAndVotes {
	public OrProof[] profs;
	public Vote[] votes;
	
	public HoldProfAndVotes(OrProof[] profs, Vote[] votes) {
		super();
		this.profs = profs;
		this.votes = votes;
	}
	
	public HoldProfAndVotes(OrProof[] profs, byte[][][] votes) {
		super();
		this.profs = profs;
		this.votes = new Vote[votes.length];
		for (int i = 0; i < votes.length; i++) {
			this.votes[i] = new Vote(votes[i]);
		}
	}

}
