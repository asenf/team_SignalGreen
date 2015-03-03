package signalGreen;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Random;

import org.geotools.referencing.GeodeticCalculator;

import repast.simphony.space.gis.Geography;
import repast.simphony.space.graph.Network;
import repast.simphony.space.graph.RepastEdge;

import com.vividsolutions.jts.geom.Coordinate;

public class Utils {

	/**
	 * Method selects a random Junction from the road network.
	 * Vehicles call this method every time they need a new destination.
	 * @param roadNetwork
	 * @return
	 */
	public static Junction getRandJunction(Network<Junction> roadNetwork) {
		// get all edges and put them into list for random access 
		Iterator<RepastEdge<Junction>> it = roadNetwork.getEdges().iterator();
		List<RepastEdge<Junction>> l = new ArrayList<RepastEdge<Junction>>();
		while(it.hasNext()) {
			l.add(it.next());			
		}

		/* TEST BEGIN */
		// for test purpose, returns the last junction.
		// comment out when not needed anymore
//		if (true)
//			return (Junction) l.get(l.size()-1).getTarget();
		/* TEST END */
		
		if (l.size() > 0) {
			Random rand = new Random();
			int index = rand.nextInt(l.size());
			// we know that each edge has a source and target Junction
			Junction j = (Junction) l.get(index).getTarget();
			// System.out.println("Random Junction: " + j.toString());
			return j;
		}

		// maybe use exception instead
		System.out.println("Unable to get a random Junction.");
		return null;
	}

    /* Distance works very well, it is in metres. */
	public static double distance (Coordinate c1, Coordinate c2, Geography g) {	
        GeodeticCalculator calculator = new GeodeticCalculator(g.getCRS()); 
//        System.out.println("*****CRS: " + g.getCRS().toString());
        calculator.setStartingGeographicPoint(c1.x, c1.y); 
        calculator.setDestinationGeographicPoint(c2.x, c2.y); 
        return calculator.getOrthodromicDistance(); 
	} 

	// returns the angle in radians, ie. degrees = angle * 2 * PI 
	public static double getAngle(Coordinate c1, Coordinate c2, Geography g) {	
        GeodeticCalculator calculator = new GeodeticCalculator(g.getCRS()); 
        calculator.setStartingGeographicPoint(c1.x, c1.y); 
        calculator.setDestinationGeographicPoint(c2.x, c2.y); 
        double angle = Math.toRadians(calculator.getAzimuth()); // Angle in range -PI to PI
        // credits: https://code.google.com/p/repastcity/source/browse/branches/sim_comp_sys_model/src/repastcity3/environment/Route.java
        // Need to transform azimuth (in range -180 -> 180 and where 0 points north)
        // to standard mathematical (range 0 -> 360 and 90 points north)
        if (angle > 0 && angle < 0.5 * Math.PI) { // NE Quadrant
                angle = 0.5 * Math.PI - angle;
        } else if (angle >= 0.5 * Math.PI) { // SE Quadrant
                angle = (-angle) + 2.5 * Math.PI;
        } else if (angle < 0 && angle > -0.5 * Math.PI) { // NW Quadrant
                angle = (-1 * angle) + 0.5 * Math.PI;
        } else { // SW Quadrant
                angle = -angle + 0.5 * Math.PI;
        }
        return angle;
	} 

	
}
