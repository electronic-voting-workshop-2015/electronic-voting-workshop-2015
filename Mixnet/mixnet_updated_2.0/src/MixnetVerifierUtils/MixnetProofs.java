package MixnetVerifierUtils;

/**
 * Created by Maor Elias on 21/02/16.
 */
public class MixnetProofs {

    public int numberOfVotes;
    public VoteLayer[] voteLayers;
    public Layer[] layers;

    public static class VoteLayer {

        public int layerNum;
        public LayerVote[] layerVotes;

        public static class LayerVote {

            public String code1;
            public String code2;
        }

    }

    public static class Layer {

        public int layerNumber;
        public AndProof[] andProofs;

        public static class AndProof {

            public int index1;
            public int index2;

            public OrProof orProof1;
            public OrProof orProof2;
            public OrProof orProof3;
            public OrProof orProof4;

            public static class OrProof {

                public String a1;
                public String b1;
                public String r1;
                public String d1;

                public String a2;
                public String b2;
                public String r2;
                public String d2;

                public String c;
            }
        }
    }
}