package verifier;


import java.math.BigInteger;
import java.util.Random;
import java.util.Set;
import java.util.TreeSet;

import ECCryptography.ECGroup;
import MixnetVerifierUtils.MixnetProofs;
import MixnetVerifierUtils.MixnetProofsContainer;
import OtherTeams.*;


/**
 * Created by Maor Elias on 21/02/16.
 */
public class MixnetVerifier implements IMixnetVerifier {

    private static int FULL = 100;
    private int currentLayerIndex;

    private int amountOfLayersToVerify;
    private int currentLayerRelativeIndex = 0;

    private byte[] g;
    private byte[] h;

    private int currentIndex1;
    private int currentIndex2;
    private int currentOrProofIndex;
    private String complaint = "";
    private int voteCount;

    private Group ecGroup = null;
    private int logOfVotesNumber;

    public MixnetVerifier(Group group) {
        ecGroup = group;
    }

    @Override
    public boolean verifyMixnetFully(MixnetProofs proofs) {
        return verifyProofs(proofs, FULL);
    }

    @Override
    public boolean verifyMixnetRandomlyByPercentage(MixnetProofs proofs, int percentage) {
        return verifyProofs(proofs, percentage);
    }

    @Override
    public String getComplaint() {
        return complaint;
    }

    @Override
    public int getVerificationProgressPercentage() {
        if (amountOfLayersToVerify <= 1) {
            return 100;
        }

        int layerCount = amountOfLayersToVerify;

        double progressFraction = ((double) currentLayerRelativeIndex / layerCount);
        int progressPercentage = (int) (progressFraction * 100);

        progressPercentage = Math.max(progressPercentage, 0);
        return progressPercentage;
    }

    public Group getGroup() {
        return ecGroup;
    }

    public void setGroup(ECGroup group) {
        ecGroup = group;
    }

    private boolean verifyProofs(MixnetProofs proofs, int percentage) {

        resetVerifierParameters();

        if (proofs == null) {
            complaint = "Deserialization of Mixnet proofs' file has failed." + "\n"
                    + "Could not perform verification of the Mixnet.";
            BulletinBoardApi.sendComplaint(complaint);
            return false;
        }

        if (ecGroup == null) {
            complaint = "Verification of mixnet could not start." + "\n"
                    + "Group operations are unavailable - group instance is null.";
            BulletinBoardApi.sendComplaint(complaint);
            return false;
        }

        voteCount = proofs.numberOfVotes;

        if (proofs.layers == null || proofs.voteLayers == null) {
            complaint = "Could not perform verification of the Mixnet, one of the elements in the Mixnet proofs is missing";
            BulletinBoardApi.sendComplaint(complaint);
            return false;
        }
        int totalNumberOfProofsLayers = proofs.layers.length;
        int totalNumbersOfVoteLayers = proofs.voteLayers.length;

        if (totalNumberOfProofsLayers == 0) {
            return true;
        }

        int expectedNumberOfVoteLayers = totalNumberOfProofsLayers + 1;
        if (totalNumbersOfVoteLayers != expectedNumberOfVoteLayers) {
            complaint = "Could not perform verification of the Mixnet, there are less vote layers than expected\n" +
                    "Actual number of layers of votes: " + totalNumbersOfVoteLayers + "\n" +
                    "Expected number of layers of votes: " + expectedNumberOfVoteLayers;
            BulletinBoardApi.sendComplaint(complaint);
            return false;
        }

        logOfVotesNumber = (int) Math.round(Math.log(voteCount) / Math.log(2));
        int expectedAmountOfLayers = 2 * logOfVotesNumber - 1;

        if (totalNumberOfProofsLayers != expectedAmountOfLayers) {
            complaint = "Mixnet verification has failed.\n" +
                    "Mixnet didn't generate enough switch layers.\n" +
                    "Number of votes : " + voteCount + "\n" +
                    "Actual number of switch layers : " + totalNumberOfProofsLayers + "\n" +
                    "But expected number of switch layers was : " + expectedAmountOfLayers;
            BulletinBoardApi.sendComplaint(complaint);
            return false;
        }

        amountOfLayersToVerify = (int) (Math.ceil(((double) percentage / 100) * totalNumberOfProofsLayers));

        if (g == null) {
            complaint = "Mixnet verification could not start.\n" +
                    "Failed retrieving group generator";

            BulletinBoardApi.sendComplaint(complaint);
            return false;
        }

        if (h == null) {
            complaint = "Mixnet verification could not start.\n" +
                    "Failed retrieving public key";

            BulletinBoardApi.sendComplaint(complaint);
            return false;
        }

        if (amountOfLayersToVerify == totalNumberOfProofsLayers) {
            for (int layerIndex = 0; layerIndex < totalNumberOfProofsLayers - 1; layerIndex++) {
                if (!verifyLayerProofs(proofs, layerIndex)) {
                    return false;
                }
                currentLayerRelativeIndex++;
            }
        } else {
            Set<Integer> randomLayersToVerify = generateRandomLayersIndices(amountOfLayersToVerify, totalNumberOfProofsLayers);
            for (Integer layerIndex : randomLayersToVerify) {
                if (!verifyLayerProofs(proofs, layerIndex)) {
                    return false;
                }
                currentLayerRelativeIndex++;
            }
        }

        return true;
    }

    private void resetVerifierParameters() {
        currentLayerIndex = -1;
        amountOfLayersToVerify = -1;
        currentLayerRelativeIndex = -1;
        currentIndex1 = -1;
        currentIndex2 = -1;
        currentOrProofIndex = -1;
        complaint = "";
        voteCount = -1;
        logOfVotesNumber = -1;
        g = g();
        h = h();
    }

    private boolean verifyLayerProofs(MixnetProofs proofs, int layerIndex) {

        if (proofs.layers[layerIndex] == null || proofs.layers[layerIndex].andProofs == null) {
            complaint = "Could not perform verification of the Mixnet, one of the elements in the Mixnet proofs is missing";
            BulletinBoardApi.sendComplaint(complaint);
            return false;

        }
        MixnetProofs.Layer.AndProof[] layerProofs = proofs.layers[layerIndex].andProofs;

        if (proofs.voteLayers == null ||
                proofs.voteLayers[layerIndex] == null ||
                proofs.voteLayers[layerIndex].layerVotes == null ||
                proofs.voteLayers[layerIndex + 1] == null ||
                proofs.voteLayers[layerIndex + 1].layerVotes == null) {
            complaint = "Could not perform verification of the Mixnet, one of the elements in the Mixnet proofs is missing";
            BulletinBoardApi.sendComplaint(complaint);
            return false;
        }
        MixnetProofs.VoteLayer.LayerVote[] currentLayerVotes = proofs.voteLayers[layerIndex].layerVotes;
        MixnetProofs.VoteLayer.LayerVote[] nextLayerVotes = proofs.voteLayers[layerIndex + 1].layerVotes;

        boolean[] indexSwitched = new boolean[voteCount];

        currentLayerIndex = layerIndex;

        int mask;
        if (layerIndex <= logOfVotesNumber - 1) {
            mask = 0x1 << layerIndex;
        } else {
            mask = 0x1 << (logOfVotesNumber - 1 - ((layerIndex + 1) % logOfVotesNumber));
        }


        for (MixnetProofs.Layer.AndProof andProof : layerProofs) {
            int index1 = andProof.index1 - 1;
            int index2 = andProof.index2 - 1;
            currentIndex1 = index1;
            currentIndex2 = index2;

            int expectedIndex2 = index1 ^ mask;
            if (index2 != expectedIndex2) {
                complaint = "Mixnet verification has failed.\n" +
                        "Mixnet performed a wrong switch at:\n" +
                        "layer index: " + currentLayerIndex + "\n" +
                        "index1 : " + currentIndex1 + "\n" +
                        "index2 : " + currentIndex2 + "\n" +
                        "But expected index2 was: " + expectedIndex2;

                BulletinBoardApi.sendComplaint(complaint);
                return false;
            }

            MixnetProofs.VoteLayer.LayerVote vote1 = currentLayerVotes[index1];
            MixnetProofs.VoteLayer.LayerVote vote2 = currentLayerVotes[index2];
            MixnetProofs.VoteLayer.LayerVote nextLayerVote1 = nextLayerVotes[index1];
            MixnetProofs.VoteLayer.LayerVote nextLayerVote2 = nextLayerVotes[index2];

            if (vote1 == null || vote2 == null || nextLayerVote1 == null || nextLayerVote2 == null) {
                complaint = "Could not perform verification of the Mixnet, one of the elements in the Mixnet proofs is missing";
                BulletinBoardApi.sendComplaint(complaint);
                return false;
            }

            if (andProof.orProof1 == null ||
                    andProof.orProof2 == null ||
                    andProof.orProof3 == null ||
                    andProof.orProof4 == null) {
                complaint = "Could not perform verification of the Mixnet, one of the elements in the Mixnet proofs is missing";
                BulletinBoardApi.sendComplaint(complaint);
                return false;
            }

            currentOrProofIndex = 1;
            boolean orProof1Verified = verifyOrProof(andProof.orProof1, vote1, nextLayerVote1, vote1, nextLayerVote2);

            currentOrProofIndex = 2;
            boolean orProof2Verified = verifyOrProof(andProof.orProof2, vote2, nextLayerVote1, vote2, nextLayerVote2);

            currentOrProofIndex = 3;
            boolean orProof3Verified = verifyOrProof(andProof.orProof3, vote1, nextLayerVote1, vote2, nextLayerVote1);

            currentOrProofIndex = 4;
            boolean orProof4Verified = verifyOrProof(andProof.orProof4, vote1, nextLayerVote2, vote2, nextLayerVote2);

            boolean andProofValue = orProof1Verified && orProof2Verified && orProof3Verified && orProof4Verified;
            if (!andProofValue) {
                return false;
            }

            indexSwitched[index1] = indexSwitched[index2] = true;
        }

        if (!verifyAllIndicesWereSwitched(indexSwitched)) {
            return false;
        }

        return true;
    }

    private boolean verifyAllIndicesWereSwitched(boolean[] indexSwitched) {
        for (int index = 0; index < indexSwitched.length; index++) {
            if (indexSwitched[index] == false) {
                complaint = "Mixnet verification has failed.\n" +
                        "At layer index: " + currentLayerIndex + "\n" +
                        "Reason: vote at index: " + index + " was not switched during transition to next layer.";

                BulletinBoardApi.sendComplaint(complaint);
                return false;
            }
        }

        return true;
    }

    private boolean verifyOrProof(MixnetProofs.Layer.AndProof.OrProof orProof,
                                  MixnetProofs.VoteLayer.LayerVote vote1,
                                  MixnetProofs.VoteLayer.LayerVote nextLayerVote1,
                                  MixnetProofs.VoteLayer.LayerVote vote2,
                                  MixnetProofs.VoteLayer.LayerVote nextLayerVote2) {


        if (orProof.a1 == null || orProof.b1 == null || orProof.r1 == null || orProof.d1 == null ||
                orProof.a2 == null || orProof.b2 == null || orProof.d2 == null || orProof.r2 == null) {
            complaint = "Could not perform verification of the Mixnet, one of the elements in the Mixnet proofs is missing";
            BulletinBoardApi.sendComplaint(complaint);
            return false;
        }

        byte[] a1 = QR_ParsingUtils.getByteArrFromStringForMixnet(orProof.a1);
        byte[] b1 = QR_ParsingUtils.getByteArrFromStringForMixnet(orProof.b1);
        byte[] r1 = QR_ParsingUtils.getByteArrFromStringForMixnet(orProof.r1);
        byte[] d1 = QR_ParsingUtils.getByteArrFromStringForMixnet(orProof.d1);

        byte[] a2 = QR_ParsingUtils.getByteArrFromStringForMixnet(orProof.a2);
        byte[] b2 = QR_ParsingUtils.getByteArrFromStringForMixnet(orProof.b2);
        byte[] r2 = QR_ParsingUtils.getByteArrFromStringForMixnet(orProof.r2);
        byte[] d2 = QR_ParsingUtils.getByteArrFromStringForMixnet(orProof.d2);

        byte[] c = QR_ParsingUtils.getByteArrFromStringForMixnet(orProof.c);


        if (!validateParametersLength(a1, b1, a2, b2)) {
            return false;
        }

        if (a1 == null || b1 == null || r1 == null || d1 == null ||
                a2 == null || b2 == null || r2 == null || d2 == null || c == null) {

            complaint = "Mixnet verification has failed at layer index: " + currentLayerIndex + "\n" +
                    "Verification of challenge has failed at: \n" +
                    "index1 = " + currentIndex1 + "\n" +
                    "index2 = " + currentIndex2 + "\n" +
                    "Or proof number: " + currentOrProofIndex + "\n" +
                    "Reason: failed to transform one of the parameters into array of bytes";

            BulletinBoardApi.sendComplaint(complaint);
            return false;

        }

        boolean challengeSatisfied = isChallengeSatisfied(d1, d2, c);
        if (!challengeSatisfied) {
            complaint = "Mixnet verification has failed at layer index: " + currentLayerIndex + "\n" +
                    "Verification of challenge has failed at: \n" +
                    "index1 = " + currentIndex1 + "\n" +
                    "index2 = " + currentIndex2 + "\n" +
                    "Or proof number: " + currentOrProofIndex;

            BulletinBoardApi.sendComplaint(complaint);
            return false;
        }

        if (!verifyTransition(vote1, nextLayerVote1, a1, b1, d1, r1)) {
            return false;
        }

        if (!verifyTransition(vote2, nextLayerVote2, a2, b2, d2, r2)) {
            return false;
        }

        return true;
    }

    private boolean isChallengeSatisfied(byte[] d1, byte[] d2, byte[] c) {
        byte[] sum = add(d1, d2);
        BigInteger int_c = new BigInteger(c);
        BigInteger order = new BigInteger(ecGroup.getOrder());
        byte[] c_mod_groupOrder = int_c.mod(order).toByteArray();
        return QR_ParsingUtils.isSameArray(c_mod_groupOrder, sum);
    }

    private boolean validateParametersLength(byte[] a1, byte[] a2, byte[] b1, byte[] b2) {
        int elementSize = ecGroup.getElementSize();
        if (a1.length != elementSize) {
            complaint = "Mixnet verification has failed at layer index: " + currentLayerIndex + "\n" +
                    "index1 = " + currentIndex1 + "\n" +
                    "index2 = " + currentIndex2 + "\n" +
                    "Or proof number: " + currentOrProofIndex + "\n" +
                    "Because a1 length is: " + a1.length + ", expected length: " + elementSize;
            BulletinBoardApi.sendComplaint(complaint);
            return false;
        }

        if (b1.length != elementSize) {
            complaint = "Mixnet verification has failed at layer index: " + currentLayerIndex + "\n" +
                    "index1 = " + currentIndex1 + "\n" +
                    "index2 = " + currentIndex2 + "\n" +
                    "Or proof number: " + currentOrProofIndex + "\n" +
                    "Because b1 length is: " + b1.length + ", expected length: " + elementSize;
            BulletinBoardApi.sendComplaint(complaint);
            return false;
        }

//        if (orProof.d1.length() != elementSize) {
//            complaint = "Mixnet verification has failed at layer index: " + currentLayerIndex + "\n" +
//                    "Verification of challenge has failed at: \n" +
//                    "index1 = " + currentIndex1 + "\n" +
//                    "index2 = " + currentIndex2 + "\n" +
//                    "Or proof number: " + currentOrProofIndex + "\n" +
//                    "Because d1 length is: " + orProof.a1.length() + ", expected length: " + elementSize;
//            BulletinBoardApi.sendComplaint(complaint);
//            return false;
//        }

//        if (orProof.r1.length() != elementSize) {
//            complaint = "Mixnet verification has failed at layer index: " + currentLayerIndex + "\n" +
//                    "Verification of challenge has failed at: \n" +
//                    "index1 = " + currentIndex1 + "\n" +
//                    "index2 = " + currentIndex2 + "\n" +
//                    "Or proof number: " + currentOrProofIndex + "\n" +
//                    "Because r1 length is: " + orProof.a1.length() + ", expected length: " + elementSize;
//            BulletinBoardApi.sendComplaint(complaint);
//            return false;
//        }

        if (a2.length != elementSize) {
            complaint = "Mixnet verification has failed at layer index: " + currentLayerIndex + "\n" +
                    "index1 = " + currentIndex1 + "\n" +
                    "index2 = " + currentIndex2 + "\n" +
                    "Or proof number: " + currentOrProofIndex + "\n" +
                    "Because a2 length is: " + a2.length + ", expected length: " + elementSize;
            BulletinBoardApi.sendComplaint(complaint);
            return false;
        }

        if (b2.length != elementSize) {
            complaint = "Mixnet verification has failed at layer index: " + currentLayerIndex + "\n" +
                    "index1 = " + currentIndex1 + "\n" +
                    "index2 = " + currentIndex2 + "\n" +
                    "Or proof number: " + currentOrProofIndex + "\n" +
                    "Because b2 length is: " + b2.length + ", expected length: " + elementSize;
            BulletinBoardApi.sendComplaint(complaint);
            return false;
        }

//        if (orProof.d2.length() != elementSize) {
//            complaint = "Mixnet verification has failed at layer index: " + currentLayerIndex + "\n" +
//                    "Verification of challenge has failed at: \n" +
//                    "index1 = " + currentIndex1 + "\n" +
//                    "index2 = " + currentIndex2 + "\n" +
//                    "Or proof number: " + currentOrProofIndex + "\n" +
//                    "Because d2 length is: " + orProof.a1.length() + ", expected length: " + elementSize;
//            BulletinBoardApi.sendComplaint(complaint);
//            return false;
//        }

//        if (orProof.r2.length() != elementSize) {
//            complaint = "Mixnet verification has failed at layer index: " + currentLayerIndex + "\n" +
//                    "Verification of challenge has failed at: \n" +
//                    "index1 = " + currentIndex1 + "\n" +
//                    "index2 = " + currentIndex2 + "\n" +
//                    "Or proof number: " + currentOrProofIndex + "\n" +
//                    "Because r2 length is: " + orProof.a1.length() + ", expected length: " + elementSize;
//            BulletinBoardApi.sendComplaint(complaint);
//            return false;
//        }

//        if (orProof.c.length() != elementSize) {
//            complaint = "Mixnet verification has failed at layer index: " + currentLayerIndex + "\n" +
//                    "Verification of challenge has failed at: \n" +
//                    "index1 = " + currentIndex1 + "\n" +
//                    "index2 = " + currentIndex2 + "\n" +
//                    "Or proof number: " + currentOrProofIndex + "\n" +
//                    "Because c length is: " + orProof.a1.length() + ", expected length: " + elementSize;
//            BulletinBoardApi.sendComplaint(complaint);
//            return false;
//        }


        return true;
    }

    private boolean verifyTransition(MixnetProofs.VoteLayer.LayerVote currentVote,
                                     MixnetProofs.VoteLayer.LayerVote nextLayerVote,
                                     byte[] a1,
                                     byte[] b1,
                                     byte[] d1,
                                     byte[] r1) {


        byte[] nextCode1 = QR_ParsingUtils.getByteArrFromStringForMixnet(nextLayerVote.code1);
        byte[] nextCode2 = QR_ParsingUtils.getByteArrFromStringForMixnet(nextLayerVote.code2);
        byte[] currentCode1 = QR_ParsingUtils.getByteArrFromStringForMixnet(currentVote.code1);
        byte[] currentCode2 = QR_ParsingUtils.getByteArrFromStringForMixnet(currentVote.code2);

        int elementSize = QR_ParsingUtils.getElementSize();

        if (elementSize == QR_ParsingUtils.ERROR) {
            complaint = "Mixnet verification failed.Could not get one of the encryption parameters: element size";
            BulletinBoardApi.sendComplaint(complaint);
            return false;
        }

        if (nextCode1 == null || nextCode2 == null || currentCode1 == null || currentCode2 == null) {
            complaint = "Mixnet verification has failed at layer index: " + currentLayerIndex + "\n" +
                    "Verification of the encryptions failed at: \n" +
                    "index1 = " + currentIndex1 + "\n" +
                    "index2 = " + currentIndex2 + "\n" +
                    "Or proof number: " + currentOrProofIndex + "\n" +
                    "Reason: failed to transform one or more of the cipher strings into byte array";
            BulletinBoardApi.sendComplaint(complaint);
            return false;

        }
        if (nextCode1.length != elementSize ||
                nextCode2.length != elementSize ||
                currentCode1.length != elementSize ||
                currentCode2.length != elementSize) {

            complaint = "Mixnet verification has failed at layer index: " + currentLayerIndex + "\n" +
                    "Verification of the encryptions failed at: \n" +
                    "index1 = " + currentIndex1 + "\n" +
                    "index2 = " + currentIndex2 + "\n" +
                    "Or proof number: " + currentOrProofIndex + "\n" +
                    "Reason: one or more of the ciphers has an invalid length (different than element size which is: " + elementSize;
            BulletinBoardApi.sendComplaint(complaint);
            return false;

        }

        byte[] u1 = multiply(nextCode1, inverse(currentCode1));
        byte[] v1 = multiply(nextCode2, inverse(currentCode2));


        byte[] actual_a1 = multiply(power(u1, d1), power(g, r1));
        if (!QR_ParsingUtils.isSameArray(actual_a1, a1)) {
            complaint = "Mixnet verification has failed at layer index: " + currentLayerIndex + "\n" +
                    "Verification of the encryptions failed at: \n" +
                    "index1 = " + currentIndex1 + "\n" +
                    "index2 = " + currentIndex2 + "\n" +
                    "Or proof number: " + currentOrProofIndex;
            BulletinBoardApi.sendComplaint(complaint);
            return false;
        }

        byte[] actual_b1 = multiply(power(v1, d1), power(h, r1));
        if (!QR_ParsingUtils.isSameArray(b1, actual_b1)) {
            complaint = "Mixnet verification has failed at layer index: " + currentLayerIndex + "\n" +
                    "Verification of the encryptions failed at: \n" +
                    "index1 = " + currentIndex1 + "\n" +
                    "index2 = " + currentIndex2 + "\n" +
                    "Or proof number: " + currentOrProofIndex;
            BulletinBoardApi.sendComplaint(complaint);
            return false;
        }

        return true;
    }

    private byte[] power(byte[] base, byte[] exponent) {
        return ecGroup.groupPow(base, exponent);
    }

    private byte[] add(byte[] d1, byte[] d2) {
        BigInteger order = new BigInteger(ecGroup.getOrder());
        BigInteger d_1 = new BigInteger(d1);
        BigInteger d_2 = new BigInteger(d2);
        return ((d_1.add(d_2)).mod(order)).toByteArray();
    }

    private byte[] multiply(byte[] a, byte[] b) {
        return ecGroup.groupMult(a, b);
    }

    private byte[] inverse(byte[] a) {
        return ecGroup.completing(a);
    }


    private static Set<Integer> generateRandomLayersIndices(int numOfLayersToGenerate, int totalLayersNumber) {
        final Random random = new Random();
        final Set<Integer> intSet = new TreeSet();
        while (intSet.size() < numOfLayersToGenerate) {
            intSet.add(random.nextInt(totalLayersNumber));
        }
        return intSet;
    }

    private byte[] g() {
        if (ecGroup == null) {
            return null;
        }
        return ecGroup.getGenerator();
    }

    private byte[] h() {
        return ParametersMain.publicKey;
    }

    public static MixnetProofs deserializeProofs(String proofsJson) {
        BBJsonDeserializer<MixnetProofsContainer> des = new BBJsonDeserializer(MixnetProofsContainer.class);
        MixnetProofsContainer proofsContainer = des.deserialize(proofsJson);
        if (proofsContainer == null) {
            return null;
        } else {
            return proofsContainer.content;
        }
    }
}