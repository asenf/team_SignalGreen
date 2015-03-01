




package signalGreen;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.PriorityQueue;
import java.util.Queue;


//b = brf
//d = options(B,I)
//I = filter(B,D,I)


//when new percept added, object activated, intentions returned

public class BDI {
	
	private ArrayList<Belief> beliefs;
	private PriorityQueue<Intention> intentions;
	//private percept currentPercept;
	
	//just one intent, a junction. beleif depends on cautiious or reckless
	public BDI(ArrayList<Belief> b, ArrayList<Intention> intents){
		this.beliefs = b;
		this.intentions = intents;
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
	private void beliefRevisionFunction(enum percept) {
		switch(percept){
		case(ifJunctionBusyAvoid)
		}
		return;
	}
	
	
	//takes in current set of beliefs, 
	//figures out available options
	//and chooses some
	private void deliberate(){
		LinkedList<String> desires = generateOptions();
		LinkedList<Intention> newIntentions = filterDesires(desires);
	}
	
	//makes use of Beliefs and Intentions to create desires
	private LinkedList<String> generateOptions(){
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