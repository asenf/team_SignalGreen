/**
 * 
 */
package signalGreen;

import repast.simphony.space.gis.Geography;
import repast.simphony.space.graph.Network;

/**
 * @author Yoann
 *
 */
public class GiveWaySign extends Junction {

	/**
	 * @param network
	 * @param geography
	 */
	public GiveWaySign(Network<Junction> network, Geography geography) {
		super(network, geography);
	}

}
