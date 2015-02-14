package signalGreen;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Random;

import repast.simphony.space.graph.Network;
import repast.simphony.space.graph.RepastEdge;

/**
 * @author Yoann
 *
 */
public class Utils {

	
	/**
	 * Method selects a random Junction from the road network.
	 * Vehicles call this method every time they need a new destination.
	 * @param roadNetwork
	 * @return
	 */
	public static Junction getRandJunction(Network<Object> roadNetwork) {
		// get all edges and put them into list for random access 
		Iterator<RepastEdge<Object>> it = roadNetwork.getEdges().iterator();
		List<RepastEdge<Object>> l = new ArrayList<RepastEdge<Object>>();
		while(it.hasNext()) {
			l.add(it.next());			
		}

		/* TEST BEGIN */
		// for test purpose, returns the last junction.
		// comment out when not needed anymore
		if (true)
			return (Junction) l.get(l.size()-1).getTarget();
		/* TEST END */
		
		if (l.size() > 0) {
			Random rand = new Random();
			int index = rand.nextInt(l.size());
			// we know that each edge has a source and target Junction
			Junction j = (Junction) l.get(index).getTarget();
			System.out.println("Random Junction: " + j.toString());
			return j;
		}

		// maybe use exception instead
		System.out.println("Unable to get a random Junction.");
		return null;
	}
		
}
