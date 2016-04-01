import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.PrintWriter;
import java.io.UnsupportedEncodingException;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;


// the class gets the admin's json file and public key from 46.101.148.106 and writes them to 2 files
// ~Note: this class requires internet connection


public class GetJsonAndPublicKey {

	public static void main(String[] args) {
		// before initializing the parameters - the voting booth should double click the server file
		// so it will be able to access it locally
		
		// now we can access the server file locally and ask for the public key and the JSON file -
		// - using HTTP get requests
		
		OkHttpClient client = new OkHttpClient();
		Request request = new Request.Builder().url("http://46.101.148.106:4567/retrieveParametersFile").build();
		client.newCall(request).enqueue(new Callback() {

			@Override
			public void onFailure(Call arg0, IOException arg1) {
				// TODO Auto-generated method stub
				System.out.println("Error connecting the server [json admin]");
			}

			@Override
			public void onResponse(Call arg0, Response arg1) throws IOException {
				// TODO Auto-generated method stub
				String adminString = arg1.body().string();
				System.out.println("admin json is:");
				System.out.println(adminString);
				
				// write admin's json file (adminString)
				PrintWriter writer1 = null;
				try {
					writer1 = new PrintWriter("adminJson", "UTF-8");
				} catch (FileNotFoundException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				} catch (UnsupportedEncodingException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
				writer1.print(adminString);
				writer1.close();
				
				
			}
			
		});
		
		
		OkHttpClient client1 = new OkHttpClient();
		Request request1 = new Request.Builder().url("http://46.101.148.106:4567/retrieveVotingPublicKey").build();
		client1.newCall(request1).enqueue(new Callback() {

			@Override
			public void onFailure(Call arg0, IOException arg1) {
				// TODO Auto-generated method stub
				System.out.println("Error connecting the server [public key]");
			}

			@Override
			public void onResponse(Call arg0, Response arg1) throws IOException {
				// TODO Auto-generated method stub
				String pkeyString = arg1.body().string();
				System.out.println("public key is:");
				System.out.println(pkeyString);
				
				// write public key (pkeyString) to file 
				PrintWriter writer2 = null;
				try {
					writer2 = new PrintWriter("publicKey", "UTF-8");
				} catch (FileNotFoundException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				} catch (UnsupportedEncodingException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
				writer2.print(pkeyString);
				writer2.close();
			}
		});
	}
}
