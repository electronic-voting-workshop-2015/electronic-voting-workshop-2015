package com.cryptoVerifier;

/**
 * Created by Maor Elias on 25/02/16.
 */
public class BBVotesContainer {

    public Vote[] votes;

    public static class Vote {

        public String vote_value;
    }

    public boolean veryfiyVoteValueExists(String vote_value) {
        if (votes != null) {
            for (Vote v : votes) {
                if (v != null && v.vote_value != null) {
                    if (v.vote_value.equals(vote_value))
                        return true;
                }
            }
        }


        return false;
    }
}
