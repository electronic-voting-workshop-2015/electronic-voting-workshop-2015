package workshop;

import java.math.BigInteger;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.Set;
import org.json.*;


import javax.lang.model.element.Parameterizable;

public class Main {
	
	public static void main(String[] args) throws JSONException{
		//Parameters.init();
		String arg = args[1];
		System.out.println(arg);
		//JSONArray jsonArray = new JSONArray(args[0]);
		
        BigInteger a = new BigInteger("-3");
        BigInteger b = new BigInteger("5ac635d8aa3a93e7b3ebbd55769886bc651d06b0cc53b0f63bce3c3e27d2604b", 16);
        BigInteger p = new BigInteger("115792089210356248762697446949407573530086143415290314195533631308867097853951");
        EllipticCurve curve = new EllipticCurve(a, b, p);
        BigInteger gx = new BigInteger("6b17d1f2e12c4247f8bce6e563a440f277037d812deb33a0f4a13945d898c296", 16);
        BigInteger gy = new BigInteger("4fe342e2fe1a7f9b8ee7eb4a7c0f9e162bce33576b315ececbb6406837bf51f5", 16);
        ECPoint g = new ECPoint(curve, gx, gy);
        int integerSize = 32;
        BigInteger order = new BigInteger("115792089210356248762697446949407573529996955224135760342422259061068512044369");
        ECGroup group = new ECGroup(curve.toByteArray(), g.toByteArray(integerSize), integerSize * 2, order.toByteArray());

        byte[] publicKey = group.getElement(new BigInteger("5").toByteArray()); // Just some example public key. Notice you will not know the
            // exponent of the real publicKey.

        ECClientCryptographyModule module = new ECClientCryptographyModule(group, group); // TODO change signGroup!!!!!!!
        
		Parameters.ourGroup = group;
		Parameters.cryptoClient = module;
		Parameters.publicKey = publicKey;
		
		
		
		
		Set<String> s1= new HashSet<String>();
		s1.add("BB");
		s1.add("Bennet");
		s1.add("Zehavush");
		s1.add("Ben-Ari");		
		RaceProperties rc1= new RaceProperties(s1, "prime minister", 1, false);
	/*	Set<String> s2= new HashSet<String>();
		s2.add("Emma");
		s2.add("John");
		s2.add("Michael");
		s2.add("TIMMY");
		RaceProperties rc2=new RaceProperties(s2, "minister", 2, false);
		Set<String> s3= new HashSet<String>();
		s3.add("Jason");
		s3.add("Nathan");
		s3.add("Ella");
		s3.add("Roy");
		RaceProperties rc3=new RaceProperties(s3, "bibi", 3, true);	*/	
		Parameters.racesProperties=new ArrayList<RaceProperties>();
		Parameters.racesProperties.add(rc1);
		//Parameters.racesProperties.add(rc2);
	//	Parameters.racesProperties.add(rc3);			
		Parameters.timeStampLevel=1;
		
		Parameters.candidatesNames = new HashSet<String>();
        for (RaceProperties race : Parameters.racesProperties){
        	for (String name : race.getPossibleCandidates()){
        		Parameters.candidatesNames.add(name);
        	}
        }
        Parameters.candidatesMap = Parameters.mapCandidates(Parameters.candidatesNames);
		
		VotingBoothImp obj = new VotingBoothImp();
		//obj.vote(jsonArray);
		
	}

}
