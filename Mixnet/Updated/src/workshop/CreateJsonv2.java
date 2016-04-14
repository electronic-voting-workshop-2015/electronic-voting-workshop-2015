package workshop;

import java.io.File;
import java.io.PrintWriter;

import org.json.*;

public class CreateJsonv2 {
	
	static public void stringToFile(String filepath, String s)
	{
		/* creating empty file */
		File file = null;
		try {
			/* create new file */
			file = new File(filepath);
			file.createNewFile();
			boolean goodFile = file.canWrite();
			if (!goodFile) {
				System.out.println("can't make new file...");
				return;
			}
		} catch (Exception e) {
			e.printStackTrace();
			return;
		}

		/* prepare for writing to the file */
		PrintWriter writer;
		try {
			writer = new PrintWriter(filepath, "UTF-8");
		} catch (Exception e) {
			e.printStackTrace();
			return;
		}
		writer.write(s);
		//String string = JSON.stringify(mixnet); /* javascript :( */
		writer.close();
		/* in JAVA we can't close file. or open it. */
	}
	
	static public void createJsonFile(String filepath, int n, String[][][] codes, String [][][][] proofs) throws JSONException
	{
		JSONObject mixnet = makeMixnet(filepath, n, codes, proofs);
		stringToFile(filepath, mixnet.toString());
		//String string = JSON.stringify(mixnet); /* javascript :( */

	}

	static public JSONObject makeMixnet(String filepath, int n, String[][][] codes, String[][][][] proofs) throws JSONException {

		/*
		 * numberOfVotes = 2^n
		 * 
		 * codes[2n][2^n][2]
		 * 
		 * codes = list of 2n layers. in each layer, 2^n votes. in each vote, 2
		 * group-elements.
		 * 
		 * proofs[2n-1][2^(n-1)][4][9]
		 * 
		 * proofs = list of 2n-1 "intervals between layers" in each "interval"
		 * there are 2^(n-1) AND-proofs. in each AND-proof there are 4
		 * OR-proofs. in each OR-proof there are 9 strings of proof.
		 */

		int numberOfLayers = codes.length;
		assert numberOfLayers == 2 * n;
		int numberOfVotes = codes[0].length;
		assert numberOfVotes == Math.pow(2, n);
		int voteSize = codes[0][0].length;
		assert voteSize == 2;
		int numberOfOrProofs = proofs[0][0].length;
		assert numberOfOrProofs == 4;


		JSONObject mixnet = new JSONObject();
		mixnet.put("numberOfVotes", numberOfVotes);

		JSONArray vLayers = new JSONArray();
		for (int i = 1; i <= numberOfLayers; i++) {
			JSONObject layer = new JSONObject();
			layer.put("layerNum", i);
			JSONArray votesArr = new JSONArray();
			for (int j = 1; j <= numberOfVotes; j++) {
				JSONObject v = new JSONObject();
				v.put("code1", codes[i-1][j-1][0]);
				v.put("code2", codes[i-1][j-1][1]);
				votesArr.put(v); //TODO .toString());
			}
			//layer.put("layerVotes", votesArr);
			layer.put("layerVotes", votesArr); //TODO .toString());
			vLayers.put(layer);
		}
		//mixnet.put("voteLayers", (Object)vLayers);
		mixnet.put("voteLayers", vLayers);//TOSO .toString());

		JSONArray pLayers = new JSONArray();
		for (int i = 1; i <= numberOfLayers - 1; i++) {
			JSONObject olp = createOlp(numberOfVotes, numberOfOrProofs, voteSize, i, proofs, numberOfLayers);
			//			pLayers.put(olp);
			pLayers.put(olp); //TODO .toString());
		}

		//		mixnet.put("layers", pLayers);
		mixnet.put("layers", pLayers); //TODO .toString());

		return mixnet;
	}

	static private int[] NirsFormula(int n, int i, int j) {
		assert 1 <= i && i <= 2 * n && 1 <= j && j <= Math.pow(2, n - 1);
		i = Math.min(i, n - i);
		int b = (int) (j + (((j - 1) / (int)(Math.pow(2, i-1))) * Math.pow(2, i-1)));
		return new int[] { b, i + b };
	}

	static private JSONObject createOlp(int numberOfVotes, int numberOfOrProofs, int voteSize, int i, String[][][][] proofs, int nol) throws JSONException {
		JSONObject olp = new JSONObject();
		olp.put("layerNumber", i);

		JSONArray andPArray = new JSONArray();

		for (int j = 1; j <= numberOfVotes / 2; j++){
			JSONObject andP = new JSONObject();
			int [] ind = NirsFormula(nol, i, j);
			andP.put("index1", ind[0]);
			andP.put("index2", ind[1]);

			for (int k = 1; k <= 4; k++) {
				JSONObject orP = new JSONObject();
				//System.out.println((i-1) + "|" + (j-1)  + "|" + (k-1));
				orP.put("a1", proofs[i-1][j-1][k-1][0]);
				orP.put("b1", proofs[i-1][j-1][k-1][1]);
				orP.put("a2", proofs[i-1][j-1][k-1][2]);
				orP.put("b2", proofs[i-1][j-1][k-1][3]);
				orP.put("c", proofs[i-1][j-1][k-1][4]);
				orP.put("r1", proofs[i-1][j-1][k-1][5]);
				orP.put("d1", proofs[i-1][j-1][k-1][6]);
				orP.put("r2", proofs[i-1][j-1][k-1][7]);
				orP.put("d2", proofs[i-1][j-1][k-1][8]);

				andP.put("orProof" + Integer.toString(k), orP); //TODO .toString() );
			}

			//			andPArray.put(andP);
			andPArray.put(andP); //TODO .toString());
		}

		olp.put("andProofs", andPArray); //TODO .toString());

		return olp;
	}

}
