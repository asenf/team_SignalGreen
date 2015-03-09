package signalGreen;


//if it detects a car in front, it tail gates
// accelerates until it speed is mathed

//if there is a traffic jam, wait 5 seconds. (or 5 steps)
//then say f**k it, and do a U turn. or just beep horn? could also do this at red lights

//always try and attain maxspeed, within speedlimit

import java.util.ArrayList;

import repast.simphony.space.gis.Geography;
import repast.simphony.space.graph.Network;
import signalGreen.Belief.Attribute;

public class Aggressive extends Vehicle {

	public Aggressive(Network<Junction> network, Geography geography,
			int maxVelocity) {
		
		super(network, geography, 80);
		
		BDI brain = new BDI(new ArrayList<Belief>(){
			{
			add(new Belief(10,Attribute.fasterIsBetter));
			add(new Belief(5,Attribute.ifStoppedCarTurnRound));
			}
		});
		
	}
	

}
