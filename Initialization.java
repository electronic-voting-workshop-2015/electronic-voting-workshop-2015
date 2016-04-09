import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.io.UnsupportedEncodingException;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLConnection;


// the class gets the admin's json file and public key from 46.101.148.106 and writes them to 2 files
// ~Note: this class requires internet connection
// *******************************************************************************************
// *******************************************************************************************
// ***************************** NOT RELEVENT. TO BE DELETED SOON ****************************
// *******************************************************************************************
// *******************************************************************************************


public class GetJsonAndPublicKey {

	public static void main(String[] args) {
		// before initializing the parameters - the voting booth should double click the server file
		// so it will be able to access it locally
		
		// now we can access the server file locally and ask for the public key and the JSON file -
		// - using HTTP get requests
		
		// create connection
		URL adminParsURL = null, pkeyURL = null;
		URLConnection getAdminPars = null, getPkey = null;
		BufferedReader reader = null;
		String input = null;
		String adminString = null;
		String pkeyString = null;
		
		try {
			adminParsURL = new URL("http://46.101.148.106:4567/retrieveAdminParameters");
		} catch (MalformedURLException e) {
			e.printStackTrace();
		}
		try {
			getAdminPars = adminParsURL.openConnection();
		} catch (IOException e) {
			e.printStackTrace();
		}
		try {
			reader = new BufferedReader(new InputStreamReader(getAdminPars.getInputStream()));
		} catch (IOException e) {
			e.printStackTrace();
		}
		try {
			while ((input = reader.readLine()) != null)
				adminString += input;
		} catch (IOException e) {
			e.printStackTrace();
		}
		
		
		// now get the private key; same process..
		input = null;
		try {
			pkeyURL = new URL("http://46.101.148.106:4567/retrieveVotingPublicKey");
		} catch (MalformedURLException e) {
			e.printStackTrace();
		}
		try {
			getPkey = pkeyURL.openConnection();
		} catch (IOException e) {
			e.printStackTrace();
		}
		try {
			reader = new BufferedReader(new InputStreamReader(getPkey.getInputStream()));
		} catch (IOException e) {
			e.printStackTrace();
		}
		try {
			while ((input = reader.readLine()) != null)
				pkeyString += input;
		} catch (IOException e) {
			e.printStackTrace();
		}
		try {
			reader.close();
		} catch (IOException e) {
			e.printStackTrace();
		}
		
		
		// write the admin's json and public key to files
		// both are in json format
		
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
}
