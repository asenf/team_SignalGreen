package signalGreen;

import repast.simphony.space.gis.Geography;
import repast.simphony.space.graph.Network;

/**
 *
 * Give Way sign junction for traffic management policies.
 * In proximity of give way intersections, 
 * vehicles check for each road segment which 
 * vehicle is closest to that intersection, 
 * to know who has precedence.
 *
 * @author Yoann
 * 
 */
public class GiveWaySign extends Junction {

	/**
	 * Constructs an instance of Give Way junction.
	 * @param network
	 * @param geography
	 */
	public GiveWaySign(Network<Junction> network, Geography geography) {
		super(network, geography);
	}

}
