package workshop;

import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.net.HttpURLConnection;
import java.net.URL;

import org.omg.CORBA.portable.OutputStream;

public class ExecuteShellCommand {

	public static String executeCommand(String command) {

		StringBuffer output = new StringBuffer();

		Process p;
		try {
			p = Runtime.getRuntime().exec(command);
			p.waitFor();
			BufferedReader reader = new BufferedReader(new InputStreamReader(
					p.getInputStream()));

			String line = "";
			while ((line = reader.readLine()) != null) {
				output.append(line + "\n");
			}

		} catch (Exception e) {
			e.printStackTrace();
		}

		return output.toString();

	}

	public static void commandWithFileContent(String filename, String command1, String command2) {
		try {
			byte[] data;
			File file = new File(filename);
			FileInputStream fis = new FileInputStream(file);
			data = new byte[(int) file.length()];
			fis.read(data);
			fis.close();
			String str = new String(data, "ISO-8859-1");
			executeCommand(command1 + str + command2);
		}
		catch (Exception ex) {
			ex.printStackTrace();
		}
	}

	public static void sendWithPOST(String filename, String url) throws Exception {
		URL object=new URL(url);

		HttpURLConnection con = (HttpURLConnection) object.openConnection();
		con.setDoOutput(true);
		con.setRequestProperty("Content-Type", "application/json; charset=ISO-8859-1");
		con.setRequestProperty("Accept", "application/json");
		con.setRequestMethod("POST");

		byte[] data;
		File file = new File(filename);
		FileInputStream fis = new FileInputStream(file);
		data = new byte[(int) file.length()];
		fis.read(data);
		fis.close();


		String proof = new String(data, "ISO-8859-1");
		//here, proof is OK, one can check this with syso.
		//System.out.println(proof + "\n---------\n");

		OutputStreamWriter wr = new OutputStreamWriter(con.getOutputStream());
		wr.write("{\"value\": \"New\"}");
		wr.flush();

		StringBuilder sb = new StringBuilder();  
		int HttpResult = con.getResponseCode(); 
		if (HttpResult == HttpURLConnection.HTTP_OK) {
			BufferedReader br = new BufferedReader(
					new InputStreamReader(con.getInputStream(), "utf-8"));
			String line = null;  
			while ((line = br.readLine()) != null) {  
				sb.append(line + "\n");  
			}
			br.close();
			System.out.println("if: "+ sb.toString());  
		} else {
			System.out.println("else: " + con.getResponseMessage());  
		}  
	}
}