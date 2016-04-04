import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.PrintWriter;
import java.io.UnsupportedEncodingException;

import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;
import java.util.Base64;

import org.json.*;

// the class gets the admin's json file and public key from 46.101.148.106 and writes them to 2 files
// ~Note: this class requires internet connection
// ~Note: this class required okhttp jar file

public class GetJsonAndPublicKey {
	public static void main(String[] args) throws JSONException, IOException {
		// before initializing the parameters - the voting booth should double
		// click the server file
		// so it will be able to access it locally

		// now we can access the server file locally and ask for the public key
		// and the JSON file -
		// - using HTTP get requests

		OkHttpClient client = new OkHttpClient();
		Request request = new Request.Builder().url("http://46.101.148.106:4567/retrieveParametersFile").build();
		Response response = null;
		try {
			response = client.newCall(request).execute();
		} catch (IOException e) {
			e.printStackTrace();
		}
		if (response.isSuccessful()) {
			String adminString = null;
			try {
				adminString = response.body().string();
			} catch (IOException e1) {
				// TODO Auto-generated catch block
				e1.printStackTrace();
			}
			System.out.println("admin json is:");
			System.out.println(adminString);

			// write admin's json file (adminString)
			PrintWriter writer1 = null;
			try {
				writer1 = new PrintWriter("adminJson", "ISO-8859-1");
			} catch (FileNotFoundException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} catch (UnsupportedEncodingException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			writer1.print(adminString);
			writer1.close();
		} else {
			System.out.println("Error connecting the server [json admin]");
		}

		OkHttpClient client2 = new OkHttpClient();
		Request request2 = new Request.Builder().url("http://46.101.148.106:4567/retrieveVotingPublicKey").build();
		Response response2 = null;

		try {
			response2 = client2.newCall(request2).execute();
		} catch (IOException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		}
		if (response2.isSuccessful()) {
			JSONArray pkeyOriginArray = null;
			pkeyOriginArray = new JSONArray(response2.body().string());
			JSONObject obj = (JSONObject)pkeyOriginArray.get(0);
			JSONObject newObj = new JSONObject();
			newObj.put("content", convertStringToISO(obj.getString("content")));
			JSONArray pkeyResultArray = new JSONArray();
			pkeyResultArray.put(newObj);
			String pkeyString = pkeyResultArray.toString();
			System.out.println("public key is:");
			System.out.println(pkeyString);

			// write public key (pkeyString) to file
			PrintWriter writer2 = null;
			try {
				writer2 = new PrintWriter("publicKey", "ISO-8859-1");
			} catch (FileNotFoundException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} catch (UnsupportedEncodingException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			writer2.print(pkeyString);
			writer2.close();
		} else {
			System.out.println("Error connecting the server [public key]");
		}
	}

	private static String convertStringToISO(String origin) throws UnsupportedEncodingException {
		byte[] base64 = origin.getBytes("UTF-8");
		byte[] arr = Base64.getDecoder().decode(base64);
		String result = new String(arr, "ISO-8859-1");
		return result;
	}
}
