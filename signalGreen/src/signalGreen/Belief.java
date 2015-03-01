package signalGreen;



public class Belief {
	
	private int confidence;
	private Attribute attribute;
	
	public Belief(int c, Attribute a){
		this.confidence = c; //10 aggresive 30 emergencyS
		this.attribute = a;
	}
	
	public enum Attribute {
		fasterIsBetter, 
		averageSpeedIsBest,
		ifStoppedCarTurnRound,
		ifStoppedCarWait,
		ifJunctionBusyAvoid,
		ifJunctionBusyWait


		//can imagine others like aggression, 
		//lonquitroad vs shortbusy one	

	}
	
		
	

}
