package signalGreen;

import com.vividsolutions.jts.geom.Coordinate;

import repast.simphony.space.gis.Geography;
import repast.simphony.space.graph.Network;

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
	// gets the base url to display agent's icons
	private String baseURL;
	
	/**
	 * Default constructor.
	 * This constructor should not be used. 
	 */
	public GisAgent() {  
		this.ID = UniqueID++;
		network = null;
		geography = null;
	}
	
	/**
	 * Constructs a generic GIS agent with its unique ID
	 * and references to the road network and GIS geography.
	 * 
	 * @param network
	 * @param geography
	 */
	public GisAgent(Network<Junction> network, Geography geography) {  
		this.ID = UniqueID++;
		this.network = network;
		this.geography = geography;
		this.baseURL = System.getProperty("user.dir");
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
	 * Returns the url where the raphics are located.
	 * Used by sld stylesheets for display purposes.
	 * 
	 * @return url
	 */
	public String getBaseURL() {
		return baseURL;
	}
	
    /**
     * Get the coordinates on GIS projection.
     * @return coordinate
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
}