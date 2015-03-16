package signalGreen;

import java.util.Map;

import repast.simphony.space.gis.Geography;
import repast.simphony.space.graph.Network;
import repast.simphony.space.graph.RepastEdge;

public class TruckVehicle extends Vehicle {

	public TruckVehicle(Network<Junction> network, Geography geography,
			Map<RepastEdge<Junction>, Road> roads, int maxVelocity) {
		super(network, geography, roads, maxVelocity);
		setMaxVelocity(80);
	}

}
