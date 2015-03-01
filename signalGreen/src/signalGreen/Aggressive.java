package signalGreen;


//if it detects a car in front, it tail gates
// accelerates until it speed is mathed

//if there is a traffic jam, wait 5 seconds. (or 5 steps)
//then say f**k it, and do a U turn. or just beep horn? could also do this at red lights

//always try and attain maxspeed, within speedlimit

import repast.simphony.space.gis.Geography;
import repast.simphony.space.graph.Network;

public class Aggressive extends Vehicle {

	public Aggressive(Network<Junction> network, Geography geography,
			int maxVelocity) {
		super(network, geography, maxVelocity);
		BDI brain = new()
		
	}
	

}
