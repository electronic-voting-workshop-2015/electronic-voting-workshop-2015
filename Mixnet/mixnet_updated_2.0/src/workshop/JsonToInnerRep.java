package workshop;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.math.BigInteger;
import java.nio.charset.Charset;
import java.util.Base64;
import Proof.AndProof;
import Proof.OrProof;
import org.json.*;

import Vote.GroupMember;
import Vote.Vote;


public class JsonToInnerRep {
	private static final Charset ISO_CHARSET = Charset.forName("ISO-8859-1");

	public Vote[][] jsonToVotes(String path) throws JSONException {
		JSONObject jn = new JSONObject(getStringFromPath(path));
		int n = jn.getJSONObject("content").getInt("numberOfVotes");
		n = (int) (Math.log(n) / Math.log(2));
		Vote[][] votes = new Vote[2*n][(int)(Math.pow(2, n))];
		JSONArray vl = jn.getJSONObject("content").getJSONArray("voteLayers");

		for (int i = 0; i < 2*n; i++) {
			for (int j = 0; j < Math.pow(2, n); j++) {
				String s1 = vl.getJSONObject(i).getJSONArray("layerVotes").getJSONObject(j).getString("code1");
				byte[] p1 = Base64.getDecoder().decode(s1.getBytes(ISO_CHARSET));
				String s2 = vl.getJSONObject(i).getJSONArray("layerVotes").getJSONObject(j).getString("code2");
				byte[] p2 = Base64.getDecoder().decode(s2.getBytes(ISO_CHARSET));
				
				votes[i][j] = new Vote(new GroupMember(p1), new GroupMember(p2));
			}
		}
		
		return votes;
	}
	
	public AndProof[][] jsonToProofs(String path) throws JSONException {
		JSONObject jn = new JSONObject(getStringFromPath(path));
		int n = jn.getJSONObject("content").getInt("numberOfVotes");
		n = (int) (Math.log(n) / Math.log(2));
		AndProof[][] aProofs = new AndProof[2*n-1][(int)(Math.pow(2, n-1))];
		JSONArray pl = jn.getJSONObject("content").getJSONArray("layers");
		
		for (int i = 0; i < 2*n-1; i++) {
			for (int j = 0; j < Math.pow(2, n-1); j++) {
				OrProof[] op = new OrProof[4];
				for (int k = 0; k < 4; k++) {
					GroupMember[] a = {new GroupMember(getAbdrc(pl, "a1", i, j, k)), new GroupMember(getAbdrc(pl, "a2", i, j, k))};
					GroupMember[] b = {new GroupMember(getAbdrc(pl, "b1", i, j, k)), new GroupMember(getAbdrc(pl, "b2", i, j, k))};
					BigInteger[] d = {new BigInteger(getAbdrc(pl, "d1", i, j, k)), new BigInteger(getAbdrc(pl, "d2", i, j, k))};
					BigInteger[] r = {new BigInteger(getAbdrc(pl, "r1", i, j, k)), new BigInteger(getAbdrc(pl, "r2", i, j, k))};
					BigInteger c = new BigInteger(getAbdrc(pl, "c", i, j, k));
					op[k] = new OrProof(a, b, c, d, r);
				}
				int indi = pl.getJSONObject(i).getJSONArray("andProofs").getJSONObject(j).getInt("index1");
				int indj = pl.getJSONObject(i).getJSONArray("andProofs").getJSONObject(j).getInt("index2");
				aProofs[i][j] = new AndProof(op, indi, indj);
			}
		}
		
		return aProofs;
	}
	
	private byte[] getAbdrc (JSONArray pl, String s, int i, int j, int k) throws JSONException {
		String s1 = pl.getJSONObject(i).getJSONArray("andProofs").getJSONObject(j).getJSONObject("orProof" + k).getString(s);
		return Base64.getDecoder().decode(s1.getBytes(ISO_CHARSET));
	}
	
	public String getStringFromPath(String path) {
		byte[] data;
		File file = new File(path);
		try {
			FileInputStream fis = new FileInputStream(file);
			data = new byte[(int) file.length()];
			fis.read(data);
			fis.close();
			return new String(data, "ISO-8859-1");
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return null;
	}
	
}
