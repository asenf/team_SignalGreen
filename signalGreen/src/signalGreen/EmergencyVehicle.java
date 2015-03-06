package signalGreen;

import java.util.Map;

import repast.simphony.space.gis.Geography;
import repast.simphony.space.graph.Network;
import repast.simphony.space.graph.RepastEdge;

public class EmergencyVehicle extends Vehicle {

	public EmergencyVehicle(Network<Junction> network, Geography geography,
			Map<RepastEdge<Junction>, Road> roads, int maxVelocity) {
		super(network, geography, roads, 140);
		
	}
	
	
	
	
	public void initVehicle(Junction Origin){
	  super.initVehicle(Origin);
	}
	
	
	//need to break yoooans methods down 
	//then use BDI on them	
	public void step(){
		
		
		
	}

}
