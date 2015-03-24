package signalGreen;

import java.util.ArrayList;

import com.vividsolutions.jts.geom.Coordinate;

import repast.simphony.space.graph.RepastEdge;

/**
 * Road agent is used to display the road on the GIS display.
 * 
 * @author Yoann
 *
 */
public class Road extends GisAgent {

	public static int UniqueID = 0;
	private int ID;
	private String name;
//	private RepastEdge<Junction> inEdge = null;
//	private RepastEdge<Junction> outEdge = null;
	private ArrayList<Junction> junctions;
	private ArrayList<Coordinate> coordinates; // A list of coordinates between the two junctions
	private double length = 0;
	private int speedLimit = 30; //mph in built up areas in uk

	/**
	 * Constructs a road with no speed limit.
	 * Defaults to 80 Km/h
	 * @param name of street, ex. "Madison Ave"
	 */
	public Road(String name) {
		super();
		this.name = name;
		this.junctions = new ArrayList<Junction>();
		this.coordinates = new ArrayList<Coordinate>();
		this.speedLimit = Constants.DEFAULT_SPEEDLIMIT;
	}
	
	
	/**
	 * preferred contructor for roads.
	 * Speed limit is usually determined from 
	 * the GIS attributes of the shapefile.
	 * 
	 * @param name
	 * @param speedLimit
	 */
	public Road(String name, int speedLimit){
		this.name = name;
		this.junctions = new ArrayList<Junction>();
		this.coordinates = new ArrayList<Coordinate>();
		this.speedLimit = speedLimit;
	}

	@Override
	public boolean equals(Object obj) {
		if (!(obj instanceof Road))
			return false;
		Road b = (Road) obj;
		return this.ID == b.ID;
	}
	
	public String getName() {
		return this.name;
	}

	public void setName(String name) {
		this.name = name;
	}
	
	@Override
	public String toString() {
		return "Road: ID: " + this.getID() + (this.getName() == null ? "" : ", Name: " + this.getName() + 
				", Length: " + this.getLength());
	}

	/**
	 * Roads need to know which junctions they are connected to.
	 * 
	 * @param j
	 */
	public void addJunction(Junction j) {
		if (this.junctions.size() == 2) {
			System.err.println("Road Error: only two Junctions allowed.");
		}
		this.junctions.add(j);
	}
	
	public ArrayList<Junction> getJunctions() {
		if (this.junctions.size() != 2) {
			System.err.println("Road Error: road must have two Junctions.");
		}
		return this.junctions;
	}

	/**
	 * Length is determined while reading 
	 * spatial data in the shapefile.
	 * @param len the length in meters
	 */
	public void setLength(double len) {
		this.length = len;
	}
	
	public double getLength() {
		return this.length;
	}
	
	public int getSpeedLimit() {
		return speedLimit;
	}

	public void setSpeedLimit(int speedLimit) {
		this.speedLimit = speedLimit;
	}
	
}
