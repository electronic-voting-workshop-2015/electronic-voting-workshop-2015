package com.cryptoVerifier;

import android.util.Pair;

import java.math.BigInteger;
import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.List;

import ECCryptography.ECClientCryptographyModule;
import ECCryptography.ECGroup;
import parametersMain.ParametersMain;


/**
 * Created by ori on 29/12/15.
 */
public class MainQR {

    public String getRawScan() {
        return rawScan;
    }

    private String rawScan = null;
    private ErrorCode errorCode = ErrorCode.NO_ERROR;
    public int level = 0;
    private static boolean DEBUG = false;

    // each entry is of the form (g^r,mh^r)
    private List<Pair<byte[], byte[]>> encryptions = new ArrayList<>();
    private byte[] certificate;
    private String timestamp;
    private int partyId;

    public MainQR(String rawScan) {

        this.rawScan = rawScan;
        if (DEBUG) {
            printByteArr("Raw barcode scan:", rawScan);
        }
        level = 1;

        if (rawScan == null) {
            errorCode = ErrorCode.QR_NULL_ERROR;
            return;
        }
        checkQRLength();

        if (errorCode == ErrorCode.NO_ERROR) {
            level = 2;
            parseEncryptions();
        }

        if (errorCode == ErrorCode.NO_ERROR) {
            level = 3;
            parseSignature();
        }

        if (errorCode == ErrorCode.NO_ERROR) {
            level = 4;
            parsePartyId();
        }

        if (errorCode == ErrorCode.NO_ERROR) {
            level = 5;
            parseTimestamp();
        }

    }

    private void printByteArr(String header, String str) {
        System.out.println(header);
        byte[] byteArr = QR_ParsingUtils.getByteArrFromString(str);
        for (byte b : byteArr) {
            System.out.print(((int) b) + ",");
        }

        System.out.println();
    }

    private void parsePartyId() {
        int numberOfCandidates = QR_ParsingUtils.getNumberOfCandidateSelectionsPerBallot();
        int elementSize = QR_ParsingUtils.getElementSize();
        int signatureSize = QR_ParsingUtils.getCertificateSize();

        int partyIdStartOffset = numberOfCandidates * 2 * elementSize + signatureSize;
        int partyIDsize = QR_ParsingUtils.PARTY_ID_SIZE;
        String partyIdString = rawScan.substring(partyIdStartOffset, partyIdStartOffset + partyIDsize);
        byte[] partyIdByteArr = QR_ParsingUtils.getByteArrFromString(partyIdString);


        if (partyIdByteArr == null) {
            errorCode = ErrorCode.STRING_TO_BYTE_ARRAY_PARSING_FAILED;
            System.out.println("error in: parsePartyId()");
        } else {
            partyId = ByteBuffer.wrap(partyIdByteArr).getInt();
        }

        if (DEBUG) {
            printByteArr("PartyId:", partyIdString);
            System.out.println("PartyId int: " + partyId + "\n");
        }
    }

    private void checkQRLength() {
        int expectedSize = QR_ParsingUtils.getMainQRSize();
        if (this.rawScan.length() != expectedSize) {
            errorCode = ErrorCode.INVALID_QR_LENGTH;
            System.out.println("error in: checkQRLength");
        }
    }

    public ErrorCode getErrorCode() {
        return errorCode;
    }

    private void parseEncryptions() {
        int elementSize = QR_ParsingUtils.getElementSize();
        int numberOfCandidates = QR_ParsingUtils.getNumberOfCandidateSelectionsPerBallot();

        if (elementSize == QR_ParsingUtils.ERROR || numberOfCandidates == QR_ParsingUtils.ERROR) {
            errorCode = ErrorCode.MISSING_CRYPTOGRAPHY_PARAMETER;
            System.out.println("error in: parseEncryptions() - missing cryptography partner");

            return;
        }

        for (int candIndex = 0; candIndex < numberOfCandidates; candIndex++) {

            int messageStartOffset = 2 * candIndex * elementSize;
            String generatorEnc = rawScan.substring(messageStartOffset, messageStartOffset + elementSize);
            String MessageEnc = rawScan.substring(messageStartOffset + elementSize, messageStartOffset + 2 * elementSize);

            if (DEBUG) {
                printByteArr("Encrypted vote:", generatorEnc + MessageEnc);
            }

            byte[] generatorEncByteArr = QR_ParsingUtils.getByteArrFromString(generatorEnc);
            if (generatorEncByteArr == null) {
                errorCode = ErrorCode.STRING_TO_BYTE_ARRAY_PARSING_FAILED;
                System.out.println("error in: parseEncryptions() - qr parsing error 1");
                return;
            }
            byte[] messageEncByteArr = QR_ParsingUtils.getByteArrFromString(MessageEnc);
            if (messageEncByteArr == null) {
                errorCode = ErrorCode.STRING_TO_BYTE_ARRAY_PARSING_FAILED;
                System.out.println("error in: parseEncryptions() - qr parsing error 2");
                return;
            }

            Pair<byte[], byte[]> messageEncryptionPair = new Pair<>(generatorEncByteArr, messageEncByteArr);
            encryptions.add(messageEncryptionPair);
        }
    }

    private void parseSignature() {
        int numberOfCandidates = QR_ParsingUtils.getNumberOfCandidateSelectionsPerBallot();
        int elementSize = QR_ParsingUtils.getElementSize();
        int signatureSize = QR_ParsingUtils.getCertificateSize();

        int signatureStartOffset = numberOfCandidates * 2 * elementSize;
        String signatureString = rawScan.substring(signatureStartOffset, signatureStartOffset + signatureSize);

        certificate = QR_ParsingUtils.getByteArrFromString(signatureString);

        if (certificate == null) {
            errorCode = ErrorCode.STRING_TO_BYTE_ARRAY_PARSING_FAILED;
            System.out.println("error in: parseSignature()");
        }
    }

    private void parseTimestamp() {
        int numberOfCandidates = QR_ParsingUtils.getNumberOfCandidateSelectionsPerBallot();
        int elementSize = QR_ParsingUtils.getElementSize();
        int signatureSize = QR_ParsingUtils.getCertificateSize();
        int timeStampSize = QR_ParsingUtils.getTimeStampSize();
        int partyIdSize = QR_ParsingUtils.PARTY_ID_SIZE;

        int timeStampStartOffset = numberOfCandidates * 2 * elementSize + signatureSize + partyIdSize;

        timestamp = rawScan.substring(timeStampStartOffset, timeStampStartOffset + timeStampSize);
        if (DEBUG) {
            printByteArr("Timestamp:", timestamp);
        }
    }

    public List<Pair<byte[], byte[]>> getEncryptions() {
        return encryptions;
    }

    public String getTimestamp() {
        return timestamp;
    }

    public void verifyCertificate(String publicKeyJsonString) {
        PartyPublicKey certificatePublicKey = parsePublicKeyFromString(publicKeyJsonString);
        if (certificatePublicKey == null) {
            errorCode = ErrorCode.CERTIFICATE_VERIFICATION_FAILED;
            System.out.println("error in: verifyCertificate() - certificate verification failed 1");
        } else {
            ECClientCryptographyModule cryptoModule = (ECClientCryptographyModule) ParametersMain.cryptoClient;
            if (cryptoModule == null) {
                errorCode = ErrorCode.MISSING_CRYPTOGRAPHY_PARAMETER;
                System.out.println("error in: verifyCertificate() - missing crypto module");
                return;
            }
            int lengthOfEncryptedMsg = 2 * QR_ParsingUtils.getElementSize();
            String encryptedMsg = rawScan.substring(0, lengthOfEncryptedMsg);

            byte[] encMsgBytes = QR_ParsingUtils.getByteArrFromString(encryptedMsg);
            if (encMsgBytes == null) {
                errorCode = ErrorCode.CERTIFICATE_VERIFICATION_FAILED;
                System.out.println("error in: verifyCertificate() - certificate verification failed 2");
            }

            ECGroup signGroup = cryptoModule.getSignGroup();
            if (signGroup == null) {
                errorCode = ErrorCode.MISSING_CRYPTOGRAPHY_PARAMETER;
                return;
            }

            byte[] publicKey = signGroup.getMember(new BigInteger(certificatePublicKey.first), new BigInteger(certificatePublicKey.second));

            if (DEBUG) {
                printByteArrays(encMsgBytes, publicKey);
            }

            boolean isCertificateValid = cryptoModule.verifyCertificate(publicKey, encMsgBytes, certificate);
            if (!isCertificateValid) {
                errorCode = ErrorCode.CERTIFICATE_INVALID_ERROR;
            }
        }
    }

//    public void verifyCertificate(String publicKeyJsonString) {
////        PartyPublicKey certificatePublicKey = parsePublicKeyFromString(publicKeyJsonString);
////        if (certificatePublicKey == null) {
////            errorCode = ErrorCode.CERTIFICATE_VERIFICATION_FAILED;
////            System.out.println("error in: verifyCertificate() - certificate verification failed 1");
////        } else {
//        ECClientCryptographyModule cryptoModule = (ECClientCryptographyModule) ParametersMain.cryptoClient;
//        if (cryptoModule == null) {
//            errorCode = ErrorCode.MISSING_CRYPTOGRAPHY_PARAMETER;
//            System.out.println("error in: verifyCertificate() - missing crypto module");
//            return;
//        }
//        int lengthOfEncryptedMsg = 2 * QR_ParsingUtils.getElementSize();
//        String encryptedMsg = rawScan.substring(0, lengthOfEncryptedMsg);
//
//        byte[] encMsgBytes = QR_ParsingUtils.getByteArrFromString(encryptedMsg);
//        if (encMsgBytes == null) {
//            errorCode = ErrorCode.CERTIFICATE_VERIFICATION_FAILED;
//            System.out.println("error in: verifyCertificate() - certificate verification failed 2");
//        }
//
//        ECGroup signGroup = cryptoModule.getSignGroup();
//        if (signGroup == null) {
//            errorCode = ErrorCode.MISSING_CRYPTOGRAPHY_PARAMETER;
//            return;
//        }
//
//        byte[] publicKey = cryptoModule.getSignGroup().getElement(new BigInteger("84573498573485038498503485943584390570934853490850349805934").toByteArray());
//
//        if (DEBUG) {
//            printByteArrays(encMsgBytes, publicKey);
//        }
//
//        boolean isCertificateValid = cryptoModule.verifyCertificate(publicKey, encMsgBytes, certificate);
//        if (!isCertificateValid) {
//            errorCode = ErrorCode.CERTIFICATE_INVALID_ERROR;
//        }
//    }

    private void printByteArrays(byte[] encMsgBytes, byte[] publicKey) {

        System.out.println("Signature key:");
        for (byte b : publicKey) {
            System.out.print(((int) b) + ",");
        }

        System.out.println();

        System.out.println("Signature:");
        for (byte b : certificate) {
            System.out.print(((int) b) + ",");
        }

        System.out.println();

        System.out.println("Message that was signed:");
        for (byte b : encMsgBytes) {
            System.out.print(((int) b) + ",");
        }

        System.out.println();

        ECClientCryptographyModule cryptoModule = (ECClientCryptographyModule) ParametersMain.cryptoClient;
        System.out.println("Sign group parameters");
        System.out.println("Element size: " + cryptoModule.getSignGroup().getElementSize());
        System.out.println("Order: " + new BigInteger(cryptoModule.getSignGroup().getOrder()));

        System.out.println("Sign group generator:");
        for (byte b : cryptoModule.getSignGroup().getGenerator()) {
            System.out.print(((int) b) + ",");
        }
        System.out.println();
    }

    private PartyPublicKey parsePublicKeyFromString(String publicKeyJson) {
        if (publicKeyJson == null) {
            return null;
        }

        IJsonDeserializer<PartyPublicKey> des = new BBJsonDeserializer<>(PartyPublicKey.class);
        PartyPublicKey res = null;
        try {
            res = des.deserialize(publicKeyJson);
        } catch (Exception e) {
        }

        return res;
    }

    public int getPartyId() {
        return partyId;
    }
}
