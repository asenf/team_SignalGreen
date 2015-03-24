package signalGreen;

import java.util.Map;

import repast.simphony.space.gis.Geography;
import repast.simphony.space.graph.Network;
import repast.simphony.space.graph.RepastEdge;

/**
 * Trucks are the slowest vehicles in Signal Green.
 * Max speed defaults to 80 Km/h.
 * 
 * @author Yoann
 *
 */
public class TruckVehicle extends Vehicle {

	/**
	 * Constructor for trcuk vehicle.
	 * 
	 * @param network
	 * @param geography
	 * @param roads
	 * @param maxVelocity
	 */
	public TruckVehicle(Network<Junction> network, Geography geography,
			Map<RepastEdge<Junction>, Road> roads, int maxVelocity) {
		super(network, geography, roads, maxVelocity);
		setMaxVelocity(Constants.TRUCK_DEFAULT_MAX_VELOCITY);
	}

}
