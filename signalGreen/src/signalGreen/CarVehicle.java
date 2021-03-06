package signalGreen;

import java.util.Map;
import repast.simphony.space.gis.Geography;
import repast.simphony.space.graph.Network;
import repast.simphony.space.graph.RepastEdge;

/**
 * CarVehicle simulates vehicles of type Car.
 * Cars are initialised with different graphics, higher speed
 * than other vehicles. Cars can be either slow or fast.
 * 
 * @author Yoann
 */
public class CarVehicle extends Vehicle {
	private String carIcon;

	/**
	 * Cars have different types of graphics
	 * depending on their maxVelocity.
	 * Faster cars look like sport cars.
	 * 
	 * @param network
	 * @param geography
	 * @param roads
	 * @param maxVelocity
	 */
	public CarVehicle(Network<Junction> network, Geography geography,
			Map<RepastEdge<Junction>, Road> roads, int maxVelocity) {
		super(network, geography, roads, maxVelocity);
		// set icon
		if (maxVelocity >= Constants.FAST) {
			this.carIcon = Constants.ICON_FAST_CAR;	
		}
		else {
			this.carIcon = Constants.ICON_SLOW_CAR;
		}
	}
	
	/**
	 * @return string the graphics relative name
	 */
	public String getCarIcon() {
		return this.carIcon;
	}

}
