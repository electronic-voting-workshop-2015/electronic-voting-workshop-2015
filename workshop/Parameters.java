package workshop;

import ECCryptography.ECClientCryptographyModule;
import java.util.*;

/**
 * The fixed parameters file, to be edited by all teams The initial system
 * parameters, which are chosen by the initializer before the process starts
 * Needs to be initialized once(!) before the actual elections.
 */
public class Parameters {
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
	public static HashMap<Integer,byte[]> mapMachineToSignature;
	// Set of the names of all the candidates in these elections - for the
	// mapping.
	public static HashSet<String> candidatesNames;
	// List of objects of type RaceProperties, which contains the properties on
	// each race of the elections.
	// (important mainly for those who read the QR)
	public static ArrayList<RaceProperties> racesProperties;
	// The mapping between candidates and group elements
	public static HashMap<String, byte[]> candidatesMap;
	// The time-stamp accuracy level, either 1 or 2, 1 for HH:MM format, 2 for
	// HH:MM:SS format
	public static int timeStampLevel;

	/**
	 * Maps the candidates to group elements, and update the corresponding field
	 * 
	 * @param candidates
	 */
	public static HashMap<String, byte[]> mapCandidates(
			HashSet<String> candidates) {
		HashMap<String, byte[]> result = new HashMap<String, byte[]>();
		HashMap<String, Integer> tempMap = new HashMap<String, Integer>();
		int n = 0;
		for (String name : candidates) {
			tempMap.put(name, n++);
		}
		Map<Integer, byte[]> mapToGroupElem = cryptoClient.getCandidateToMemebrMapping(candidates.size());
		for (String name : candidates) {
			result.put(name, mapToGroupElem.get(tempMap.get(name)));
		}
		return result;
	}

	public static void setParameters(Group ourGroup1, ClientCryptographyModule cryptoClient1, byte[] publicKey1, int topQRLevel1,
			int bottomQRLevel1, HashSet<String> candidatesNames1,
			ArrayList<RaceProperties> racesProperties1, int timeStampLevel1, int numOfMachines1) {
		ourGroup = ourGroup1;
		cryptoClient = cryptoClient1;
		publicKey = publicKey1;
		topQRLevel = topQRLevel1;
		bottomQRLevel = bottomQRLevel1;
		candidatesNames = candidatesNames1;
		candidatesMap = mapCandidates(candidatesNames);
		racesProperties = racesProperties1;
		timeStampLevel = timeStampLevel1;
		numOfMachines = numOfMachines1;
		mapMachineToSignature = setMachinesSignatures();
	}
	
	public static HashMap<Integer,byte[]> setMachinesSignatures() {
		HashMap<Integer,byte[]> map = new HashMap<>();
		Random rn = new Random();
		boolean validSignature = true; // valid that each machine's signature is different from the previous ones
		for (int i = 1; i <= numOfMachines; i++) {
			byte[] signature = new byte[ourGroup.getElementSize()];
			new Random().nextBytes(signature);
			// loop to check that the signature of machine #i is different from all the previous machines' signatures
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
	
	public static boolean isSameArray(byte[] arr1, byte[] arr2) {
		for (int i = 0; i < arr1.length; i++) {
			if (arr1[i] != arr2[i])
				return false;
		}
		return true;
	}
	
		/**
	 * Parsing the initialization JSON into the main array which defines the
	 * election system
	 * 
	 * @param jsonRepr
	 * @return
	 * @throws JSONException
	 */
	public static ArrayList<RaceProperties> parseInitJSON(JSONArray jsonRepr) throws JSONException {
		ArrayList<RaceProperties> res = new ArrayList<RaceProperties>();
		for (int i = 0; i < jsonRepr.length(); i++) {
			JSONObject curElement = jsonRepr.getJSONObject(i);
			String name = curElement.getString("position");
			int slotsNum = curElement.getInt("slots");
			boolean order = false;
			if (curElement.getInt("type") == 3) {
				order = true;
			}
			JSONArray names = curElement.getJSONArray("candidatesPool");
			Set<String> namesPool = new HashSet<String>();
			for (int j = 0; j < names.length(); j++) {
				namesPool.add(names.getString(j));
			}
			RaceProperties cur = new RaceProperties(namesPool, name, slotsNum, order);
			res.add(cur);
		}
		return res;
	}
}
