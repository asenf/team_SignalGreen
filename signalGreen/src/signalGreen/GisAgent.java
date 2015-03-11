package signalGreen;

import com.vividsolutions.jts.geom.Coordinate;

import repast.simphony.context.Context;
import repast.simphony.engine.schedule.ScheduleParameters;
import repast.simphony.engine.schedule.ScheduledMethod;
import repast.simphony.random.RandomHelper;
import repast.simphony.space.gis.Geography;
import repast.simphony.space.graph.Network;
import repast.simphony.util.ContextUtils;

/**
 * A generic GIS agent.
 * Every GIS agent should extend this class.
 */
public abstract class GisAgent {

	public static int UniqueID = 0;
	private int ID;
	
	// Repast projections
	private Network<Junction> network;
	private Geography geography;
	
	// position on GIS projection
	private Coordinate coordinate;
	// field used in GIS display for debug purposes
	protected String debug;
	
	/**
	 * Default constructor. 
	 */
	public GisAgent() {  
		this.ID = UniqueID++;
		network = null;
		geography = null;
	}
	
	public GisAgent(Network<Junction> network, Geography geography) {  
		this.ID = UniqueID++;
		this.network = network;
		this.geography = geography;
	}
	
	/**
	 * Get the GIS geography
	 * @return geograpy
	 */
	public Geography getGeography() {
		return geography;
	}
	
	/**
	 * Get the road network
	 * @return network
	 */
	public Network<Junction> getNetwork() {
		return network;
	}
	
	/**
	 * Every GIS agent has a unique ID.
	 * @return unique ID
	 */
	public int getID() {
		return ID;
	}
	
    /**
     * Get the coordinates on GIS projection
     */
    public Coordinate getCoords() {
            return coordinate;
    }
    
    public void setCoords(Coordinate c) {
            this.coordinate = c;
    }
	
	public String getDebug() {
		return debug;
	}
	
	public void setDebug(String debug) {
		this.debug = debug;
	}
}