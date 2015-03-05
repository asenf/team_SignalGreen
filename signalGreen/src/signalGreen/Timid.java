package signalGreen;

import repast.simphony.space.gis.Geography;
import repast.simphony.space.graph.Network;



//maintain current speed
//if traffic jam, wait patiently.
public class Timid extends Vehicle {

	public Timid(Network<Junction> network, Geography geography, int maxVelocity) {
		super(network, geography, 60);
		// TODO Auto-generated constructor stub
	}
	

}