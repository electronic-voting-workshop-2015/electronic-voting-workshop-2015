package workshop;

import workshop.ECCryptography.ECClientCryptographyModule;
import workshop.ECCryptography.ECGroup;
import workshop.ECCryptography.ECPoint;
import workshop.ECCryptography.EllipticCurve;

import java.util.*;
import org.json.*;

import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.UnsupportedEncodingException;
import java.math.BigInteger;

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

	/**
	 * Maps the candidates to group elements, and update the corresponding field
	 * 
	 * @param candidates
	 */
	private static HashMap<String, byte[]> mapCandidates(TreeSet<String> candidates) {
		Arrays.toString(candidates.toArray());
		HashMap<String, byte[]> result = new HashMap<String, byte[]>();
		HashMap<String, Integer> tempMap = new HashMap<String, Integer>();
		int n = 1;
		for (String name : candidates) {
			tempMap.put(name, n);
			n++;
		}
		Map<Integer, byte[]> mapToGroupElem = cryptoClient.getCandidateToMemebrMapping(candidates.size());
		for (String name : candidates) {
			result.put(name, mapToGroupElem.get(tempMap.get(name)));
		}
		// for (String s: result.keySet()) {
		// System.out.println(s + " ");
		// for (byte b: result.get(s)) {
		// System.out.print(b + " ");
		// }
		// System.out.println();
		// System.out.println();
		// System.out.println();
		// }

		return result;
	}

	private static void setParameters(String _order, String _ElementSizeInBytes, String _a, String _b, String _p,
			String _generator_X, String _generator_Y, int _numOfMachines, ArrayList<RaceProperties> _racesProperties,
			int _timeStampLevel, byte[] _publicKey) {
		numOfMachines = _numOfMachines;
		timeStampLevel = _timeStampLevel;
		racesProperties = _racesProperties;
		BigInteger a;
		BigInteger b;
		BigInteger p;
		if (_a.matches("[-+]?\\d*\\.?\\d+")) { // Base 10.
			a = new BigInteger(_a);
		} else { // Base 16.
			a = new BigInteger(_a, 16);
		}
		if (_b.matches("[-+]?\\d*\\.?\\d+")) { // Base 10.
			b = new BigInteger(_b);
		} else { // Base 16.
			b = new BigInteger(_b, 16);
		}
		if (_p.matches("[-+]?\\d*\\.?\\d+")) { // Base 10.
			p = new BigInteger(_p);
		} else { // Base 16.
			p = new BigInteger(_p, 16);
		}
		EllipticCurve curve = new EllipticCurve(a, b, p);
		BigInteger gx;
		if (_generator_X.matches("[-+]?\\d*\\.?\\d+")) { // Base 10.
			gx = new BigInteger(_generator_X);
		} else { // Base 16.
			gx = new BigInteger(_generator_X, 16);
		}
		BigInteger gy;
		if (_generator_Y.matches("[-+]?\\d*\\.?\\d+")) { // Base 10.
			gy = new BigInteger(_generator_Y);
		} else { // Base 16.
			gy = new BigInteger(_generator_Y, 16);
		}
		ECPoint g = new ECPoint(curve, gx, gy);
		int sizeInBytes = Integer.parseInt(_ElementSizeInBytes);
		BigInteger order = new BigInteger(_order);
		if (_order.matches("[-+]?\\d*\\.?\\d+")) { // Base 10.
			order = new BigInteger(_order);
		} else { // Base 16.
			order = new BigInteger(_order, 16);
		}
		ourGroup = new ECGroup(curve.toByteArray(), g.toByteArray(32), sizeInBytes, order.toByteArray());
		cryptoClient = new ECClientCryptographyModule((ECGroup) ourGroup, (ECGroup) ourGroup);
		candidatesNames = new TreeSet<>();
		for (RaceProperties race : racesProperties) {
			for (String name : race.getPossibleCandidates()) {
				candidatesNames.add(name);
			}
		}
		candidatesMap = mapCandidates(candidatesNames);
		mapMachineToSignature = setMachinesSignatures();

		publicKey = _publicKey;
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
	private static ArrayList<RaceProperties> parseRaceProps(JSONArray jsonRepr) throws JSONException {
		ArrayList<RaceProperties> res = new ArrayList<RaceProperties>();
		for (int i = 0; i < jsonRepr.length(); i++) {// go over races
			JSONObject curElement = jsonRepr.getJSONObject(i);
			String name = curElement.getString("position");
			int slotsNum = 1;
			if (curElement.has("slots")) {
				slotsNum = curElement.getInt("slots");
			}
			boolean order = false;
			if (curElement.getInt("type") == 2) {
				order = true;
			}
			JSONArray names = curElement.getJSONArray("candidates");
			Set<String> namesPool = new LinkedHashSet<String>();
			for (int j = 0; j < names.length(); j++) {
				namesPool.add(names.getJSONObject(j).getString("name"));
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
		ArrayList<RaceProperties> racesProperties = null;
		try {
			racesProperties = parseRaceProps(initFormat.getJSONObject(0).getJSONArray("RaceProperties"));
		} catch (JSONException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		JSONArray group = null;
		try {
			group = initFormat.getJSONObject(1).getJSONArray("Group");
		} catch (JSONException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		String order = null;
		try {
			order = group.getJSONObject(0).getString("Order");
		} catch (JSONException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		String elementSizeInBytes = null;
		try {
			elementSizeInBytes = group.getJSONObject(1).getString("ElementSizeInBytes");
		} catch (JSONException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		JSONArray ec = null;
		try {
			ec = group.getJSONObject(2).getJSONArray("EC");
		} catch (JSONException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		String a = null;
		try {
			a = ec.getJSONObject(0).getString("a");
		} catch (JSONException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		String b = null;
		try {
			b = ec.getJSONObject(1).getString("b");
		} catch (JSONException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		String p = null;
		try {
			p = ec.getJSONObject(2).getString("p");
		} catch (JSONException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		String totalgen = null;
		try {
			totalgen = group.getJSONObject(3).getString("Generator");
		} catch (JSONException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		String[] gen = totalgen.split(",");
		String generator_X = gen[0];
		String generator_Y = gen[1];
		int numOfMachines = 0;
		try {
			numOfMachines = initFormat.getJSONObject(2).getInt("NumOfMachines");
		} catch (JSONException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		int timeStampLevel = 0;
		try {
			timeStampLevel = initFormat.getJSONObject(3).getInt("TimeStampLevel");
		} catch (JSONException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		setParameters(order, elementSizeInBytes, a, b, p, generator_X, generator_Y, numOfMachines, racesProperties,
				timeStampLevel, pkey);
	}

	/**
	 * get admin's selections (the JSON file) and the public key from the server
	 * - - and initialize all fields (call parseJSONInit method above)
	 * 
	 * @throws JSONException
	 * @throws UnsupportedEncodingException
	 */
	public static void init() {
		// read from adminJson and publicKey and call to eliran's method
		String line = null;

		// read admin parameters from file
		FileInputStream fin = null;
		try {
			fin = new FileInputStream("adminJson");
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
		System.out.println(adminString);
		System.out.println("first char: " + adminString.charAt(0));
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
			fin = new FileInputStream("./publicKey");
		} catch (FileNotFoundException e2) {
			// TODO Auto-generated catch block
			e2.printStackTrace();
		}
		try {
			br = new BufferedReader(new InputStreamReader(fin, "ISO-8859-1"));
		} catch (IOException e) {
			e.printStackTrace();
		}
		try {
			pkeyArray = new JSONArray(br.readLine());
		} catch (IOException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
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
			pkeyByte = ((String) ((JSONObject) pkeyArray.get(0)).get("content")).getBytes("ISO-8859-1");
		} catch (UnsupportedEncodingException | JSONException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

		// initialize
		parseJSONInit(adminArray, pkeyByte);
		// all parameters are initialized
	}
}
