package com.cryptoVerifier;

import android.util.Pair;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import parametersMain.ParametersMain;
import workshop.Group;
import workshop.RaceProperties;

public class AuditQR {

    private String rawScan = null;
    private ErrorCode errorCode = ErrorCode.NO_ERROR;
    private Group group;
    private String complaint;
    private String candidatesSelectedMessage = "";
    public ArrayList<String> racesNames;

    // each entry is of the form (m,r) where m is a message, r is the randomness
    private List<Pair<byte[], byte[]>> msgs_rands = new ArrayList<>();
    private ArrayList<ArrayList<String>> chosenCandidatesByRace = new ArrayList<>();

    public AuditQR(String rawScan, Group group) {
        this.rawScan = rawScan;
        this.group = group;
        racesNames = null;
        parseAuditQR();
    }


    private void parseAuditQR() {

        if (rawScan == null) {
            errorCode = ErrorCode.QR_NULL_ERROR;
            return;
        }

        int expectedSize = QR_ParsingUtils.getAuditQRSize();
        if (rawScan.length() != expectedSize) {
            errorCode = ErrorCode.INVALID_QR_LENGTH;
            return;
        }

        int elementSize = QR_ParsingUtils.getElementSize();
        int numberOfCandidates = QR_ParsingUtils.getNumberOfCandidateSelectionsPerBallot();

        if (elementSize == QR_ParsingUtils.ERROR || numberOfCandidates == QR_ParsingUtils.ERROR) {
            errorCode = ErrorCode.MISSING_CRYPTOGRAPHY_PARAMETER;
            return;
        }

        for (int candIndex = 0; candIndex < numberOfCandidates; candIndex++) {

            int messageStartOffset = 2 * candIndex * elementSize;
            String message = rawScan.substring(messageStartOffset, messageStartOffset + elementSize);
            String randomness = rawScan.substring(messageStartOffset + elementSize, messageStartOffset + 2 * elementSize);

            byte[] messageByteArr = QR_ParsingUtils.getByteArrFromString(message);
            if (messageByteArr == null) {
                errorCode = ErrorCode.STRING_TO_BYTE_ARRAY_PARSING_FAILED;
                return;
            }

            byte[] randomnessByteArr = QR_ParsingUtils.getByteArrFromString(randomness);
            if (randomnessByteArr == null) {
                errorCode = ErrorCode.STRING_TO_BYTE_ARRAY_PARSING_FAILED;
                return;
            }

            Pair<byte[], byte[]> message_and_randomness = new Pair<>(messageByteArr, randomnessByteArr);
            msgs_rands.add(message_and_randomness);
        }
    }

    public ErrorCode getErrorCode() {
        return errorCode;
    }

    public boolean compareToMainQR(MainQR mainQR) {
        List<Pair<byte[], byte[]>> encryptions = mainQR.getEncryptions();
        List<Pair<byte[], byte[]>> messages_randomnesses = this.msgs_rands;

        if (encryptions.size() != messages_randomnesses.size()) {
            errorCode = ErrorCode.QR_COMPARING_ERROR;
            complaint = "Audit test has failed.\nReason: QR1,QR2 do not contain same amount of ciphers" +
                    "\nTimestamp: " + mainQR.getTimestamp();
            BulletinBoardApi.sendComplaint(complaint);
            return false;
        }

        if (group == null) {
            errorCode = ErrorCode.QR_COMPARING_ERROR;
            complaint = "Audit test has failed.\nReason: group is null";
            BulletinBoardApi.sendComplaint(complaint);
            return false;
        }

        if (!verifyCiphersMatchMsgsAndRands(mainQR, encryptions, messages_randomnesses)) {
            return false;
        }

        if (!buildChosenCandidatesMessage(mainQR)) {
            return false;
        }

        return true;
    }

    // match every m from the auditQR to an actual candidate name, in case of mismatch to any candidate return false
    private boolean buildChosenCandidatesMessage(MainQR mainQR) {
        HashMap<String, byte[]> candToElement = ParametersMain.candidatesMap;
        String cand;

        if (candToElement == null) {
            errorCode = ErrorCode.MISSING_CRYPTOGRAPHY_PARAMETER;
            return false;
        }

        ArrayList<RaceProperties> raceProps = ParametersMain.racesProperties;
        racesNames = new ArrayList<>();
        if (raceProps == null || raceProps.size() == 0) {
            errorCode = ErrorCode.MISSING_CRYPTOGRAPHY_PARAMETER;
            return false;
        }


        int totalCandidatesInPreviousRaces = 0;
        for (int raceIndex = 0; raceIndex < raceProps.size(); raceIndex++) {
            RaceProperties currentRaceProps = raceProps.get(raceIndex);
            racesNames.add(currentRaceProps.getNameOfRace());
            if (raceProps.size() != 1) {
                candidatesSelectedMessage += "מירוץ מספר " + (raceIndex + 1) + ":\n";
            }

            ArrayList<String> raceChosenCandidates = new ArrayList<>();
            for (int msg_rand_index = 0; msg_rand_index < currentRaceProps.getNumOfSlots(); msg_rand_index++) {
                int msgAbsoluteIndex = msg_rand_index + totalCandidatesInPreviousRaces;

                Pair<byte[], byte[]> msg_rand = msgs_rands.get(msgAbsoluteIndex);
                cand = getCandByElement(candToElement, msg_rand.first, mainQR);
                if (cand == null) {
                    return false;
                } else {
                    raceChosenCandidates.add(cand);
                    candidatesSelectedMessage += cand + "\n";
                }

            }

            candidatesSelectedMessage += "\n";
            chosenCandidatesByRace.add(raceChosenCandidates);
            totalCandidatesInPreviousRaces += currentRaceProps.getNumOfSlots();
        }

        return true;
    }

    private String getCandByElement(HashMap<String, byte[]> candsToElements, byte[] element, MainQR mainQR) {
        for (Map.Entry<String, byte[]> candElem : candsToElements.entrySet()) {
            if (QR_ParsingUtils.isSameArray(element, candElem.getValue())) {
                String cand = candElem.getKey();
                return cand;
            }
        }

        errorCode = ErrorCode.ELEMENT_CANDIDATE_MISMATCH;
        complaint = "Audit test has failed.\nReason: one of the messages in the audit ballot doesn't match any of the candidates" +
                "\nBallot Timestamp: " + mainQR.getTimestamp();
        BulletinBoardApi.sendComplaint(complaint);
        return null;
    }

    // assert that the encryptions (g^r, mh^r) in MainQR match m,r of AuditQR
    private boolean verifyCiphersMatchMsgsAndRands(MainQR mainQR,
                                                   List<Pair<byte[], byte[]>> encryptions,
                                                   List<Pair<byte[], byte[]>> messages_randomnesses) {
        byte[] g = group.getGenerator();
        byte[] h = ParametersMain.publicKey;

        for (int index = 0; index < encryptions.size(); index++) {
            Pair<byte[], byte[]> msg_randomness = messages_randomnesses.get(index);
            Pair<byte[], byte[]> ciphers = encryptions.get(index);

            byte[] rand = msg_randomness.second;

            byte[] expectedFirstCipher = group.groupPow(g, rand);
            if (!QR_ParsingUtils.isSameArray(expectedFirstCipher, ciphers.first)) {
                errorCode = ErrorCode.QR_COMPARING_ERROR;
                complaint = "Audit test has failed.\nReason: the " + (index + 1) + " th candidate selection first cipher (g^r) in QR1 does not equal the encryption of m,r as provided in QR2" +
                        "\nBallot Timestamp: " + mainQR.getTimestamp();
                BulletinBoardApi.sendComplaint(complaint);
                return false;
            }

            byte[] msg = msg_randomness.first;
            byte[] expectedSecondCipher = group.groupMult(msg, group.groupPow(h, rand));
            if (!QR_ParsingUtils.isSameArray(expectedSecondCipher, ciphers.second)) {
                errorCode = ErrorCode.QR_COMPARING_ERROR;
                complaint = "Audit test has failed.\nReason: the " + (index + 1) + " th candidate selection second cipher (mh^r) in QR1 does not equal the encryption of m,r as provided in QR2" +
                        "\nBallot Timestamp: " + mainQR.getTimestamp();
                BulletinBoardApi.sendComplaint(complaint);
                return false;
            }
        }
        return true;
    }

    public ArrayList<ArrayList<String>> getChosenCandidatesByRace() {
        return chosenCandidatesByRace;
    }

    public String getChosenCandidatesMessage() {
        return candidatesSelectedMessage;
    }
}
