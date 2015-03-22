package signalGreen;

import java.awt.geom.Point2D;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Random;

import org.geotools.referencing.GeodeticCalculator;
import repast.simphony.space.gis.Geography;
import repast.simphony.space.graph.Network;
import repast.simphony.space.graph.RepastEdge;

import com.vividsolutions.jts.geom.Coordinate;

/**
 * Most utility functions are used for geographic
 * calculations, such as distance between coordinates
 * or azimuth of a road segment. 
 * 
 * @author signalGreen team
 *
 */
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
		
		if (l.size() > 0) {
			Random rand = new Random();
			int index = rand.nextInt(l.size());
			// we know that each edge has a source and target Junction
			Junction j = (Junction) l.get(index).getTarget();
			// System.out.println("Random Junction: " + j.toString());
			return j;
		}
		return null;
	}
	
	/**
	 * Method iterates though a List of Junctions to find most popular one.
	 * This is the Junction with the most number of cars.
	 * @param List of junctions
	 * @return Popular junction
	 */
	public Junction getPopularJunction(List<Junction> junctions)
	{
		if (junctions == null) return null;
		
		Junction popularJunc = junctions.get(0);
		
		for (int i = 0; i < junctions.size(); i++) {
			if(junctions.get(i).vehicles.size() > popularJunc.vehicles.size()) {
				popularJunc = junctions.get(i);
			}
		}
	  return popularJunc;
	}
	
	public static void debugCoordinate(Coordinate c) {
		System.out.println("Coordinate: " + c.toString() + ", x: " 
				+ c.x + ", y: " + c.y);
	}

    /**
     * Distance between two coordinates in metres
     * 
     * @param coordinate1
     * @param coordinate2
     * @param geography 
     */
	public static double distance(Coordinate c1, Coordinate c2, Geography g) {
        GeodeticCalculator calculator = new GeodeticCalculator(g.getCRS()); 
        calculator.setStartingGeographicPoint(c1.x, c1.y); 
        calculator.setDestinationGeographicPoint(c2.x, c2.y); 
        return calculator.getOrthodromicDistance(); 
	} 

	/**
	 * Returns the angle in radians given two coordinates.
	 * Radians to degrees conversion = angle * 2 * PI
	 * 
	 * @param c1
	 * @param c2
	 * @param g
	 * @return
	 */
	public static double getAngle(Coordinate c1, Coordinate c2, Geography g) {	
        double angle = Math.toRadians(Utils.getAzimuth(c1, c2, g)); // Angle in range -PI to PI
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
	
	/**
	 * Given two coordinates it returns the azimuth.
	 * Azimuth = angle in range of (+-) 180 degrees
	 * 
	 * @param c1 coordinate
	 * @param c2 coordinate
	 * @param g the geography
	 * @return azimuth in double precision
	 */
	public static double getAzimuth(Coordinate c1, Coordinate c2, Geography g) {
        GeodeticCalculator calculator = new GeodeticCalculator(g.getCRS()); 
        calculator.setStartingGeographicPoint(c1.x, c1.y); 
        calculator.setDestinationGeographicPoint(c2.x, c2.y); 
        return calculator.getAzimuth();		
	}

	/**
	 * Returns two coordinates that are perpendicular (+-90 degrees) to a given angle
	 * from a starting point on Earth, at a given distance from c.
	 * Used for creating Lanes on the left and right of a Road.
	 * 
	 * @param c the coordinate
	 * @param azimuth
	 * @param distance
	 * @param g the geography
	 * @return
	 */
	public static Coordinate[] createCoordsFromCoordAndAngle(Coordinate c, double azimuth, double distance, Geography g) {	
        // on the GIS display the do not look 90 degrees because
		// GIS is actually a sphere (the Earth..)
        double angle = Math.toRadians(azimuth);
        double a1, a2;
        
        // LEFT LANES in respect to road
        // -90 degrees angle
        if (angle > 0 && angle < 0.5 * Math.PI) { // NE Quadrant
	            a1 = angle - 0.5 * Math.PI;
	    } else if (angle >= 0.5 * Math.PI) { // SE Quadrant
	            a1 = angle - 0.5 * Math.PI;
	    } else if (angle < 0 && angle > -0.5 * Math.PI) { // NW Quadrant
	            a1 = angle - 0.5 * Math.PI;
	    } else { // SW Quadrant
	            a1 = angle + 1.5 * Math.PI;
	    }
        
        // RIGHT LANES
        // +90 degrees angle
        if (angle > 0 && angle < 0.5 * Math.PI) { // NE Quadrant
	            a2 = angle + 0.5 * Math.PI;
	    } else if (angle >= 0.5 * Math.PI) { // SE Quadrant
	            a2 = angle - 1.5 * Math.PI;
	    } else if (angle < 0 && angle > -0.5 * Math.PI) { // NW Quadrant
	            a2 = angle + 0.5 * Math.PI;
	    } else { // SW Quadrant
	            a2 = angle + 0.5 * Math.PI;
	    }

        // convert back to azimuth
        a1 = Math.toDegrees(a1);
        a2 = Math.toDegrees(a2);
        
        GeodeticCalculator calculator = new GeodeticCalculator(g.getCRS());
        calculator.setStartingGeographicPoint(c.x, c.y);
        
        /*        
         * Generate set of coordinates as follows using either j1 or j2 as
         * starting coordinate c:
         * 
         * 					j1					j2
	     *  lft outer		o------------------->o 
	     *  lft inner		o------------------->o 
	     *  road topology	o<==================>o
	     *  rgt inner		o<-------------------o
	     *  rgt outer		o<-------------------o
	     *  
	     */
        
        // Left lanes
        calculator.setDirection(a1, (distance * 1.5));
	    Point2D dest1 = calculator.getDestinationGeographicPoint();
        calculator.setDirection(a1, distance * 0.5);
	    Point2D dest2 = calculator.getDestinationGeographicPoint();
	    
	    // Right lanes
	    calculator.setDirection(a2, distance * 0.5);
	    Point2D dest3 = calculator.getDestinationGeographicPoint();
	    calculator.setDirection(a2, (distance * 1.5));
	    Point2D dest4 = calculator.getDestinationGeographicPoint();
	   
	    // return coords
	    Coordinate[] coords = {
	    		new Coordinate(dest1.getX(), dest1.getY()),	// lft outer
	    		new Coordinate(dest2.getX(), dest2.getY()),	// lft inner
	    		new Coordinate(dest3.getX(), dest3.getY()),	// rgt inner
	    		new Coordinate(dest4.getX(), dest4.getY())	// rgt outer
	    };
		return coords;
	}

	public static double getAngleDeg(Coordinate c1, Coordinate c2,
			Geography g) {
		return Math.toDegrees(Utils.getAngle(c1, c2, g));
	}
	
	/**
	 * Returns a correct angle for displaying graphics
	 * correctly on the GIS projection. Angle of orientation
	 * of graphics must be computed from grid north as 
	 * opposed to true north, which may not point to the
	 * top of the display.
	 * 
	 * @param c1
	 * @param c2
	 * @param geography
	 * @return
	 */
	public static double getAngleForIcons(Coordinate c1, Coordinate c2, Geography g) {
		// angle must be computed from grid north (NOT true north of projection)
		double atan = Math.atan2(c1.y - c2.y, c1.x - c2.x) * 180.0 / Math.PI;
        double azimuth = (450.0 - atan) % 360;
		return azimuth;		
	}


}
