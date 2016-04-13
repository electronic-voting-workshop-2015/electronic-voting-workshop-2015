package workshop;

import org.json.*;

/**public class Main {

	public static void main(String[] args) throws JSONException {
		Parameters.init();
		JSONArray jsonArray = new JSONArray(args[1]);
		VotingBoothImp obj = new VotingBoothImp();
		if (args[0].equals("vote")) {
			jsonArray.remove(0); // Remove mchine id.
			obj.vote(jsonArray);
		} else
			obj.audit(Boolean.valueOf(((JSONObject) jsonArray.get(0)).getBoolean("audit")));
	}

}OLD MAIN, NO HEBREW**/ 


public class Main {
	
	public static String decodeJson(String s){
		String res="";
		String c= new String(s);
		String[] a=c.split(",");
		for(int i=0; i<a.length; i++){
			res+=(char)(Integer.parseInt(a[i]));
		}
		return res;
	}

	public static void main(String[] args) throws JSONException {
		Parameters.init();				
		VotingBoothImp obj = new VotingBoothImp();
		if (args[0].equals("vote")) {
			String jsonString=decodeJson(args[1]);
			JSONArray jsonArray = new JSONArray(jsonString);
			jsonArray.remove(0); // Remove mchine id.
			obj.vote(jsonArray);
		} else{
			JSONArray jsonArray = new JSONArray(args[1]);
			obj.audit(Boolean.valueOf(((JSONObject) jsonArray.get(0)).getBoolean("audit")));
		}
	}

}
