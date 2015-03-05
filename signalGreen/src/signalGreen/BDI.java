




package signalGreen;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.PriorityQueue;
import java.util.Queue;


//b = brf takes in set of beliefs and new percept
//d = options(B,I) //could stay on current path, could change
//I = filter(B,D,I) //finalise choice, choose road to take (find best route)
//return change in plan to vehicle class


//when new percept added, object activated, intentions returned

public class BDI {
	
	private List<Belief> beliefs;
	private PriorityQueue<Intention> intentions; //starts with one, a junction
	private percept currentPercept;
	
	
	//just one intent, a junction. beleif depends on cautiious or reckless
	//because the only intent is a junction...someone else can choose that
	
	public BDI(ArrayList<Belief> b){
		this.beliefs = b;
		}
	
	
	
	public addNewPercept(enum p){
		String pass = "pass";
	}
	
	
	
	//main method
	private void think(){
		beliefs.add(beliefRevisionFunction(p));
		
	}
	
	//takes in current beliefs, and new percept
	//updates current beliefs
	//check to see if list whatever contains the belief first
	private void beliefRevisionFunction(String percept) {
		//loop over dictionary, update on spot
		switch(percept){
		case(ifJunctionBusyAvoid)
		}
		return;
	}
	
	//work out which road is fastest based on speedlimit times business. not all vehicles do this,
	
	
	
	
	//takes in current set of beliefs, 
	//figures out available options
	//and chooses some
	private void deliberate(){
		LinkedList<String> desires = generateOptions();
		LinkedList<Intention> newIntentions = filterDesires(desires);
	}
	
	//has to go in step. Like, if new car is seen, then stop speeding
	//makes use of Beliefs and Intentions to create desires
	private LinkedList<String> generateOptions(){
		/*
		public enum Attribute {
			fasterIsBetter, 
			averageSpeedIsBest,
			ifStoppedCarTurnRound,
			ifStoppedCarWait,
			ifJunctionBusyAvoid,
			ifJunctionBusyWait
			
			*/
	}
	
	private LinkedList<Intention> filterDesires(LinkedList<String> desires){
		LinkedList<Intention> newIntents = new LinkedList<Intention>();
		return newIntents;
	}
	
	
	public PriorityQueue<Intention> getIntentions(){
		return this.intentions;
	}
}
	
}