package signalGreen;



public class Belief {
	
	private int confidence;
	private Attribute attribute;
	
	public Belief(int c, Attribute a){
		this.confidence = c;
		this.attribute = a;
	}
	
	public enum Attribute {
		fasterIsBetter, 
		averageSpeedIsBest,
		ifStoppedCarTurnRound,
		ifStoppedCarWait,
		ifJunctionBusyAvoid,
		ifJunctionBusyWait

	}
	
		
	

}
