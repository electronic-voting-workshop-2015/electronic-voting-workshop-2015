package parametersMain;

import android.os.Environment;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.UnsupportedEncodingException;
import java.math.BigInteger;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Map;
import java.util.Random;
import java.util.Set;
import java.util.TreeSet;

import ECCryptography.ECClientCryptographyModule;
import ECCryptography.ECGroup;
import ECCryptography.ECPoint;
import ECCryptography.EllipticCurve;
import workshop.ClientCryptographyModule;
import workshop.Group;
import workshop.RaceProperties;

/**
 * The fixed parameters file, to be edited by all teams The initial system
 * parameters, which are chosen by the initializer before the process starts
 * Needs to be initialized once(!) before the actual elections.
 */
public class ParametersMain {

    // The group we use for encrypting the votes
    public static Group ourGroup;
    // The cryptography module we use
    public static ClientCryptographyModule cryptoClient;
    // The public key for the encryption
    public static byte[] publicKey;
    // The cipher-text QR version
    public static int topQRLevel;
    // The audit QR version
    public static int bottomQRLevel;
    // number of voting machines
    public static int numOfMachines;
    // maps machine to its signature
    public static HashMap<Integer, byte[]> mapMachineToSignature;
    // Set of the names of all the candidates in these elections - for the
    // mapping.
    public static TreeSet<String> candidatesNames;
    // List of objects of type RaceProperties, which contains the properties on
    // each race of the elections.
    // (important mainly for those who read the QR)
    public static ArrayList<RaceProperties> racesProperties;
    // The mapping between candidates and group elements
    public static HashMap<String, byte[]> candidatesMap;
    // The time-stamp accuracy level, either 1 or 2, 1 for HH:MM format, 2 for
    // HH:MM:SS format
    public static int timeStampLevel;

    public static void main(String[] args) {
        boolean READ_FROM_FILES = true;
        if (READ_FROM_FILES == false) {
            hardCodedInit();
        } else
            init();
    }

    public static String theAdminJson = "[{\"0\":{\"RaceProperties\":{\"0\":{\"position\":\"מירוץ 1\",\"candidates\":{\"0\":{\"id\":\"1\",\"name\":\"הבית היהודי\",\"image\":\"habait_hayeudi.jpg\"},\"1\":{\"id\":\"2\",\"name\":\"חדש\",\"image\":\"hadash.jpg\"},\"2\":{\"id\":\"3\",\"name\":\"הליכוד\",\"image\":\"halikud.jpg\"},\"3\":{\"id\":\"0\",\"name\":\"fake candidate\",\"image\":\"noImage.jpg\"}},\"type\":\"0\"},\"1\":{\"position\":\"מירוץ 2\",\"candidates\":{\"0\":{\"id\":\"1\",\"name\":\"הבית היהודי\",\"image\":\"habait_hayeudi.jpg\"},\"1\":{\"id\":\"2\",\"name\":\"חדש\",\"image\":\"hadash.jpg\"},\"2\":{\"id\":\"3\",\"name\":\"הליכוד\",\"image\":\"halikud.jpg\"},\"3\":{\"id\":\"0\",\"name\":\"fake candidate\",\"image\":\"noImage.jpg\"}},\"type\":\"1\",\"slots\":\"2\"},\"2\":{\"position\":\"מירוץ 3\",\"candidates\":{\"0\":{\"id\":\"1\",\"name\":\"הבית היהודי\",\"image\":\"habait_hayeudi.jpg\"},\"1\":{\"id\":\"2\",\"name\":\"חדש\",\"image\":\"hadash.jpg\"},\"2\":{\"id\":\"3\",\"name\":\"הליכוד\",\"image\":\"halikud.jpg\"},\"3\":{\"id\":\"0\",\"name\":\"fake candidate\",\"image\":\"noImage.jpg\"}},\"type\":\"2\",\"slots\":\"2\"}}},\"1\":{\"Group\":{\"0\":{\"Order\":\"115792089210356248762697446949407573529996955224135760342422259061068512044369\"},\"1\":{\"ElementSizeInBytes\":\"66\"},\"2\":{\"EC\":{\"0\":{\"a\":\"-3\"},\"1\":{\"b\":\"41058363725152142129326129780047268409114441015993725554835256314039467401291\"},\"2\":{\"p\":\"115792089210356248762697446949407573530086143415290314195533631308867097853951\"}}},\"3\":{\"Generator\":\"48439561293906451759052585252797914202762949526041747995844080717082404635286,36134250956749795798585127919587881956611106672985015071877198253568414405109\"}}},\"2\":{\"NumOfMachines\":\"1\"},\"3\":{\"TimeStampLevel\":\"1\"}}]";
    public static byte[] thePublicKey = {-120, -65, 92, 30, -102, 95, -123, 60, -46, -96, 78, 32, 52, 15, -110, -67, 69, 3, -20, 121, 36, -9, 107, -86, -77, 109, -114, -123, -9, 60, -10, 90, -10, 102, 27, -86, -62, 41, 92, 29, -60, 68, -107, 116, 45, -72, -64, -110, -18, 19, 75, -121, -98, -79, -86, -59, -6, 77, 91, -87, -126, -40, -50, -44};

    /**
     * get admin's selections (the JSON file) and the public key from the server
     * - - and initialize all fields (call parseJSONInit method above)
     */
    // later change it to MAIN
    public static File getFile(String fileName) {
        return new File(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOCUMENTS),
                "parameters/" + fileName);

    }

    public static void init() {

        // else--> read from files==true --> the same as before (nothing
        // changed)

        // read from adminJson and publicKey and call to eliran's method
        String line = null;

        // read admin parameters from file
        FileInputStream fin = null;
        try {
            fin = new FileInputStream(getFile("adminJson"));
        } catch (FileNotFoundException e2) {
            // TODO Auto-generated catch block
            e2.printStackTrace();
        }
        BufferedReader br = null;
        try {
            br = new BufferedReader(new InputStreamReader(fin, "UTF-8"));
        } catch (UnsupportedEncodingException e2) {
            // TODO Auto-generated catch block
            e2.printStackTrace();
        }
        StringBuilder sb = new StringBuilder();
        try {
            line = br.readLine();
            // Find start of JSON array.
            while (line.charAt(0) != '[' && line.length() > 0) {
                line = line.substring(1);
            }
        } catch (IOException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
        while (line != null) {
            sb.append(line);
            try {
                line = br.readLine();
            } catch (IOException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            }
        }
        String adminString = sb.toString();
        try {
            fin.close();
        } catch (IOException e2) {
            // TODO Auto-generated catch block
            e2.printStackTrace();
        }
        try {
            br.close();
        } catch (IOException e1) {
            // TODO Auto-generated catch block
            e1.printStackTrace();
        }
        JSONArray adminArray = null;
        try {
            adminArray = new JSONArray(adminString);
        } catch (JSONException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }

        // get public key from file
        JSONArray pkeyArray = null;
        sb = new StringBuilder();
        try {
            fin = new FileInputStream(getFile("publicKey"));
        } catch (FileNotFoundException e2) {
            // TODO Auto-generated catch block
            e2.printStackTrace();
        }
        try {
            br = new BufferedReader(new InputStreamReader(fin, "ISO-8859-1"));
        } catch (IOException e) {
            e.printStackTrace();
        }
        line = null;
        try {
            line = br.readLine();
            // Find start of JSON array.
            while (line.charAt(0) != '[' && line.length() > 0) {
                line = line.substring(1);
            }
        } catch (IOException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
        try {
            pkeyArray = new JSONArray(line);
        } catch (JSONException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
        try {
            br.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
        byte[] pkeyByte = null;
        try {
            pkeyByte = ((((JSONObject) pkeyArray.get(0)).get("content")).toString()).getBytes("ISO-8859-1");
        } catch (UnsupportedEncodingException | JSONException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }

        // initialize
        parseJSONInit(adminArray, pkeyByte);
        // all parameters are initialized
    }

    private static void hardCodedInit() {
        String adminString, pkeyString;
        adminString = theAdminJson;

        JSONArray adminArray = null;
        try {
            adminArray = new JSONArray(adminString);
        } catch (JSONException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }


//        JSONArray pkeyArray = null;
//        try {
//            pkeyArray = new JSONArray(pkeyString);
//        } catch (JSONException e) {
//            // TODO Auto-generated catch block
//            e.printStackTrace();
//        }

        byte[] pkey = thePublicKey;
        // initialize
        parseJSONInit(adminArray, pkey);
        // all parameters are initialized


        return;
    }

    /**
     * Maps the candidates to group elements, and update the corresponding field
     *
     * @param candidates
     */
    private static HashMap<String, byte[]> mapCandidates(TreeSet<String> candidates) {
        HashMap<String, byte[]> result = new HashMap<String, byte[]>();
        HashMap<String, Integer> tempMap = new HashMap<String, Integer>();
        int n = 1;
        for (String name : candidates) {
            tempMap.put(name, n++);
        }
        Map<Integer, byte[]> mapToGroupElem = cryptoClient.getCandidateToMemebrMapping(candidates.size());
        for (String name : candidates) {
            result.put(name, mapToGroupElem.get(tempMap.get(name)));
        }
        Iterator it = result.entrySet().iterator();
        /*
         * while (it.hasNext()) { Map.Entry pair = (Map.Entry)it.next();
		 * System.out.println("line: " + pair.getKey() + " = " +
		 * pair.getValue()); }
		 */
        return result;
    }

    private static void setParameters(String _order, String _ElementSizeInBytes, String _a, String _b, String _p,
                                      String _generator_X, String _generator_Y, int _numOfMachines, ArrayList<RaceProperties> _racesProperties,
                                      int _timeStampLevel, byte[] _publicKey) {
        numOfMachines = _numOfMachines;
        timeStampLevel = _timeStampLevel;
        racesProperties = _racesProperties;
        BigInteger a = getBigInteger(_a);
        BigInteger b = getBigInteger(_b);
        BigInteger p = getBigInteger(_p);
        EllipticCurve curve = new EllipticCurve(a, b, p);
        BigInteger gx = getBigInteger(_generator_X);
        BigInteger gy = getBigInteger(_generator_Y);
        ECPoint g = new ECPoint(curve, gx, gy);
        int sizeInBytes = Integer.parseInt(_ElementSizeInBytes);
        BigInteger order = getBigInteger(_order);
        ourGroup = new ECGroup(curve.toByteArray(), g.toByteArray(32), sizeInBytes, order.toByteArray());
        cryptoClient = new ECClientCryptographyModule((ECGroup) ourGroup, (ECGroup) ourGroup);
        candidatesNames = new TreeSet<String>();
        for (RaceProperties race : racesProperties) {
            for (String name : race.getPossibleCandidates()) {
                candidatesNames.add(name);
            }
        }
        candidatesMap = mapCandidates(candidatesNames);
        mapMachineToSignature = setMachinesSignatures();
        publicKey = _publicKey;
    }

    private static BigInteger getBigInteger(String number) {
        BigInteger result;
        if (number.matches("[-+]?\\d*\\.?\\d+")) { // Base 10.
            result = new BigInteger(number);
        } else { // Base 16.
            result = new BigInteger(number, 16);
        }

        return result;
    }

    private static HashMap<Integer, byte[]> setMachinesSignatures() {
        HashMap<Integer, byte[]> map = new HashMap<>();
        Random rn = new Random();
        boolean validSignature = true; // valid that each machine's signature is
        // different from the previous ones
        for (int i = 1; i <= numOfMachines; i++) {
            byte[] signature = new byte[ourGroup.getElementSize()];
            rn.nextBytes(signature);
            // loop to check that the signature of machine #i is different from
            // all the previous machines' signatures
            for (int j = 1; j < i; j++) {
                if (isSameArray(signature, map.get(j))) {
                    validSignature = false;
                    i--;
                    break;
                }
            }
            if (validSignature)
                map.put(i, ourGroup.getElement(signature));
            validSignature = true;
        }
        return map;
    }

    private static boolean isSameArray(byte[] arr1, byte[] arr2) {
        for (int i = 0; i < arr1.length; i++) {
            if (arr1[i] != arr2[i])
                return false;
        }
        return true;
    }

    /**
     * Parsing the JSONArray which represents the races and their properties
     *
     * @param jsonRepr
     * @return
     * @throws JSONException
     */
    private static ArrayList<RaceProperties> parseRaceProps(JSONObject jsonRepr) throws JSONException {
        ArrayList<RaceProperties> res = new ArrayList<RaceProperties>();
        JSONObject races = jsonRepr.getJSONObject("RaceProperties");
        for (int i = 0; i < races.length(); i++) {// go over races
            JSONObject curElement = races.getJSONObject(String.valueOf(i));
            String name = curElement.getString("position");
            JSONObject candidates = curElement.getJSONObject("candidates");
            Set<String> namesPool = new HashSet<String>();
            for (int j = 0; j < candidates.length(); j++) {
                JSONObject candidate = candidates.getJSONObject(String.valueOf(j));
                namesPool.add(candidate.getString("name"));
            }
            int slotsNum = 1;
            if (curElement.has("slots")) {
                slotsNum = curElement.getInt("slots");
            }
            boolean order = false;
            if (curElement.getInt("type") == 2) {
                order = true;
            }
            res.add(new RaceProperties(namesPool, name, slotsNum, order));
        }
        return res;
    }

    /**
     * Parsing the initialization JSON into the main array which defines the
     * election system
     *
     * @param initFormat
     * @param pkey
     */
    private static void parseJSONInit(JSONArray initFormat, byte[] pkey) {
        JSONObject initFormatObject = null;
        try {
            initFormatObject = initFormat.getJSONObject(0);
        } catch (JSONException e1) {
            // TODO Auto-generated catch block
            e1.printStackTrace();
        }
        ArrayList<RaceProperties> racesProperties = null;
        JSONObject group = null;
        String order = null;
        String elementSizeInBytes = null;
        String a = null, b = null, p = null, totalgen = null;
        int numOfMachines = 0, timeStampLevel = 0;

        for (int k = 0; k < initFormatObject.length(); k++) {
            JSONObject current = null;
            try {
                current = initFormatObject.getJSONObject(String.valueOf(k));
            } catch (JSONException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            }
            if (current.has("RaceProperties")) {
                try {
                    racesProperties = parseRaceProps(current);
                } catch (JSONException e) {
                    // TODO Auto-generated catch block
                    e.printStackTrace();
                }
            } else if (current.has("Group")) {
                try {
                    group = initFormat.getJSONObject(0).getJSONObject("1").getJSONObject("Group");
                    //group = initFormat.getJSONObject(1).getJSONArray("Group");
                } catch (JSONException e) {
                    // TODO Auto-generated catch block
                    e.printStackTrace();
                }

                for (int i = 0; i < group.length(); i++) {
                    JSONObject curElement = null;
                    try {
                        curElement = group.getJSONObject(String.valueOf(i));
                    } catch (JSONException e) {
                        // TODO Auto-generated catch block
                        e.printStackTrace();
                    }
                    if (curElement.has("Order")) {
                        try {
                            order = curElement.getString("Order");
                        } catch (JSONException e) {
                            // TODO Auto-generated catch block
                            e.printStackTrace();
                        }
                    } else if (curElement.has("ElementSizeInBytes")) {
                        try {
                            elementSizeInBytes = curElement.getString("ElementSizeInBytes");
                        } catch (JSONException e) {
                            // TODO Auto-generated catch block
                            e.printStackTrace();
                        }
                    } else if (curElement.has("EC")) {
                        JSONObject ec = null;
                        try {
                            ec = curElement.getJSONObject("EC");
                        } catch (JSONException e) {
                            // TODO Auto-generated catch block
                            e.printStackTrace();
                        }
                        for (int j = 0; j < ec.length(); j++) {
                            JSONObject cur = null;
                            try {
                                cur = ec.getJSONObject(String.valueOf(j));
                            } catch (JSONException e) {
                                // TODO Auto-generated catch block
                                e.printStackTrace();
                            }
                            if (cur.has("a")) {
                                try {
                                    a = cur.getString("a");
                                } catch (JSONException e) {
                                    // TODO Auto-generated catch block
                                    e.printStackTrace();
                                }
                            } else if (cur.has("b")) {
                                try {
                                    b = cur.getString("b");
                                } catch (JSONException e) {
                                    // TODO Auto-generated catch block
                                    e.printStackTrace();
                                }
                            } else if (cur.has("p")) {
                                try {
                                    p = cur.getString("p");
                                } catch (JSONException e) {
                                    // TODO Auto-generated catch block
                                    e.printStackTrace();
                                }
                            }
                        }
                    } else if (curElement.has("Generator")) {
                        try {
                            totalgen = curElement.getString("Generator");
                        } catch (JSONException e) {
                            // TODO Auto-generated catch block
                            e.printStackTrace();
                        }
                    }
                }
            } else if (current.has("NumOfMachines")) {
                try {
                    numOfMachines = current.getInt("NumOfMachines");
                } catch (JSONException e) {
                    // TODO Auto-generated catch block
                    e.printStackTrace();
                }
            } else if (current.has("TimeStampLevel")) {
                try {
                    timeStampLevel = current.getInt("TimeStampLevel");
                } catch (JSONException e) {
                    // TODO Auto-generated catch block
                    e.printStackTrace();
                }
            } else {
                System.out.println("Error: unhandled object: " + current);
            }
        }

        String[] gen = totalgen.split(",");
        String generator_X = gen[0];
        String generator_Y = gen[1];
        setParameters(order, elementSizeInBytes, a, b, p, generator_X, generator_Y, numOfMachines, racesProperties,
                timeStampLevel, pkey);
    }
}