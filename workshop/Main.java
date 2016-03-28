package workshop;

import org.json.*;

public class Main {

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

}
