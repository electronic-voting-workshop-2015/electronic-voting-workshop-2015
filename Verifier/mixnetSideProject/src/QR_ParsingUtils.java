
import java.util.Base64;
import java.util.List;

import workshop.Group;
import workshop.RaceProperties;

/**
 * Created by Maor Elias on 04/03/16.
 */
public class QR_ParsingUtils {

    public static int PARTY_ID_SIZE = 4;
    public static int ERROR = -1;

    public static int getNumberOfCandidateSelectionsPerBallot() {
        List<RaceProperties> raceProps = ParametersMain.racesProperties;
        if (raceProps == null) {
            return ERROR;
        }

        int numOfCands = 0;
        for (RaceProperties raceProp : raceProps) {
            numOfCands += raceProp.getNumOfSlots();
        }
        return numOfCands;
    }

    public static int getElementSize() {
        Group group = ParametersMain.ourGroup;
        if (group == null) {
            return ERROR;
        }

        return group.getElementSize();
    }

    public static int getTimeStampSize() {
        int precisionLevel = ParametersMain.timeStampLevel;
        int timeStampSize = precisionLevel == 1 ? 2 : 3;

        return timeStampSize;
    }

    public static int getCertificateSize() {
        return getElementSize();
    }

    public static int getAuditQRSize() {
        int numOfCandidates = getNumberOfCandidateSelectionsPerBallot();
        int elementSize = getElementSize();

        if (numOfCandidates == ERROR | elementSize == ERROR) {
            return ERROR;
        }
        return numOfCandidates * 2 * elementSize;
    }

    public static int getMainQRSize() {
        int numOfCandidates = getNumberOfCandidateSelectionsPerBallot();
        if (numOfCandidates == ERROR) {
            return ERROR;
        }
        int elementSize = getElementSize();
        if (elementSize == ERROR) {
            return ERROR;
        }
        int encriptionsSectionSize = numOfCandidates * 2 * elementSize;
        int timeStampSize = getTimeStampSize();
        int signatureSize = getCertificateSize();

        return encriptionsSectionSize + signatureSize + PARTY_ID_SIZE + timeStampSize;
    }

    public static boolean isSameArray(byte[] arr1, byte[] arr2) {
        if (arr1.length != arr2.length) {
            return false;
        }
        for (int i = 0; i < arr1.length; i++) {
            if (arr1[i] != arr2[i])
                return false;
        }
        return true;
    }

    public static byte[] getByteArrFromString(String str) {
        byte[] arr;
        try {
            arr = str.getBytes("ISO-8859-1");
            return arr;
        } catch (Exception e) {
            return null;
        }
    }

    public static byte[] getByteArrFromStringForMixnet(String str) {
        return getByteArrFromStringFromBase64(str);
//        byte[] arr;
//        try {
//            arr = str.getBytes("UTF-8");
//            return arr;
//        } catch (Exception e) {
//            return null;
//        }
    }

    public static byte[] getByteArrFromStringFromBase64(String str) {
        return Base64.getDecoder().decode(str);
    }
}
