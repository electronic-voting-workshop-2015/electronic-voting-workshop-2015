package verifier;

import MixnetVerifierUtils.MixnetProofs;
import OtherTeams.*;

import java.io.File;
import java.io.IOException;
import java.util.Scanner;

public class Main {
    public static final String PATH_TO_PROOFS_FILE = "./Proofs.json";
    public static void main(String[] args) {    	
        ParametersMain.init();

        Group group = ParametersMain.ourGroup;
        final MixnetVerifier mixnetVerifier = new MixnetVerifier(group);

        String proofsJson = null;
        try {
            proofsJson = readFile(getFile(PATH_TO_PROOFS_FILE));
            MixnetProofs mixnetProofs = MixnetVerifier.deserializeProofs(proofsJson);
            final boolean verificationResult = mixnetVerifier.verifyMixnetRandomlyByPercentage(mixnetProofs, 10);
            System.out.println("verificationResult = " + verificationResult);
        } catch (IOException e) {
            e.printStackTrace();
        }

    }

    private static String readFile(File file) throws IOException {
        StringBuilder fileContents = new StringBuilder((int) file.length());
        Scanner scanner = new Scanner(file);
        String lineSeparator = System.getProperty("line.separator");

        try {
            while (scanner.hasNextLine()) {
                fileContents.append(scanner.nextLine() + lineSeparator);
            }
            return fileContents.toString();
        } finally {
            scanner.close();
        }
    }


    public static File getFile(String s) {
        return new File(PATH_TO_PROOFS_FILE);
    }
}