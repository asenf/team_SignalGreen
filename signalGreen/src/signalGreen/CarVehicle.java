package signalGreen;

import java.util.Map;
import repast.simphony.space.gis.Geography;
import repast.simphony.space.graph.Network;
import repast.simphony.space.graph.RepastEdge;

public class CarVehicle extends Vehicle {
	private String carIcon;

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
	
	public String getCarIcon() {
		return this.carIcon;
	}

}
