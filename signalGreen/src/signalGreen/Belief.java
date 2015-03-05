package signalGreen;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap; //no need for synchronination, so Map not table
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

//move to front. can access inner members anyway
//when I get back, finish ordering system, find a clever way to loop through prioirties(start with constants)
//getters and setters 

// internal belief - how fast to go driver thinks is morally correct etc
// external belief - how busy a road is

//two inner classes. one is about its own characterstiscs and confidence wont change much(boundit)
//second is about the best roads and junctions. how many cars/speed limit, how long it takes
//mostly for encapsulation and avoiding global variables

//Strings should probably be enums

public class Belief {

	//in ascending order - timid, aggressive, cop
	//cant remember why I didnt use a map here, but there was a reason...
	public  static  ArrayList<String> characteristics = (ArrayList<String>) Arrays.asList(
			"fasterIsBetter", "averageSpeedIsBest",
			"keepASafeDistanceFromCarAhead", "tailgateCarAhead",
			"accelerateSlowly", "accelereatteFast", "alwaysAccelerate",
			"ifAmberSlowDown", "ifAmberSpeedUp", "alwaysObeySpeedLimit",
			"ifFewCarsVisibleSpeed","neverObeySpeedLimits" ,
			"ifRoadToJunctionIsBusyChangeRoute");

	public  static ArrayList<String> observations = (ArrayList<String>) Arrays.asList(
			"howLongShouldIWaitBehindAVehicle", "howBusyIsJunction","which is is the fastest road", //junction sum of roads
			"howBusyIsRoad"); // cops will know exaxtly
	
	//default confidence levels, if not provided in a constructor. out of 100.
	//should have really put this above.
	//use desecending order to change proirtiy
	private static HashMap<String,Integer> characteristicsConfidenceLevels  = new HashMap<String,Integer>(); 
	

	
	
	
	private int confidence;
	private String attribute; // either characteristic or observation
	private String type;

	public Belief(String attribute, String vehicleType) {
		this.attribute = attribute;
		this.type = vehicleType;
	}

	// copy constructor - bit of haskell influence here...
	public Belief(int c, String attribute, String vehicleType) {
		this.confidence = c; // 10 aggresive 30 emergencyS
		this.attribute = attribute;
		this.type = vehicleType;

		// C style error checking....
		if (type == null || !characteristics.contains(attribute)
				|| !observations.contains(attribute))
			return;

		// decide which inner class to use
		if (characteristics.contains(type)) {
			new Internal(type);
		}
		if (observations.contains(type)) {
			new External(type);
		}
	}



	class Internal {
		
		//this is both what values a cop cares about, and the values values.
		private HashMap<String,Integer> copvalues = new HashMap<String,Integer>();
		private HashMap<String,Integer> timidvalues = new HashMap<String,Integer>();
		private HashMap<String,Integer> aggressivevalues = new HashMap<String,Integer>();

	
		// can turn this into a switch statement or a list if amount of
		// subvehicles gets too large

		Internal(String type) { // cop, timid or aggressive for now
		
			//create lookup maps..
			
			//Iterator<String> c = characteristics.iterator();
			//Iterator<String> o = observations.iterator();
			//while(c.hasNext()){
		
			//out of a 100
			//put them in descending order so I can loop through them
			//timid - aggressive - cop
			
			for(int c=0,o=0; c<characteristics.size()-1; o=(c-9), o++,c++){
			
					copvalues.put(characteristics.get(c),90);
					aggressivevalues.put(characteristics.get(c),60);
				    timidvalues.put(characteristics.get(c),30);
		
			}
			
			//then use lookup tables for both constructor, as well as these default values
			if (type.equals("cop"))
				this.addCopBehaviour();

			if (type.equals("aggressive"))
				this.addAggressiveBehaviour();

			if (type.equals("timid"))
				this.addTimidBehaviour();
		}

		private void addTimidBehaviour() {
			// behaviours.add();
		}

		private void addCopBehaviour() { 
		
		}

		private void addAggressiveBehaviour() {

		}
	}

	class External {

		
		 /*
	    "howLongShouldIWaitBehindAVehicle", 
		"howBusyIsRoad"
		 "howBusyIsJunction","which is is the fastest road"
		 switch on type then hashmap of each one
		 
		 hashmap(hashmap string int
		 */
		
		//first level is vehicleType, then attribute, then confidence
		// HashMap<String, HashMap<String,Integer>> observationalConfidenceLevels = new HashMap<String,HashMap<String,Integer>>();
		//observationalConfidenceLevels.put("cop").put("whichIsTheFastestRoad",100);
		
		switch(vehicleType){
		
		if(vehicleType==null)return;
		case 1: "cop": pass; return;
		}
		
		
		
		
		External(String type) {
		};

	}

}
