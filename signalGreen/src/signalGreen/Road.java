package signalGreen;

import java.util.ArrayList;

import com.vividsolutions.jts.geom.Coordinate;

import repast.simphony.space.graph.RepastEdge;

/**
 * @author Yoann
 * Road agent is used to display the road on the GIS display.
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

	public Road(String name) {
		//super();
		this.name = name;
		this.ID = UniqueID++;
		this.junctions = new ArrayList<Junction>();
		this.coordinates = new ArrayList<Coordinate>();
	}
	
	public Road(String name, int speedLimit){
		this.name = name;
		this.speedLimit = speedLimit;
	}

	@Override
	public boolean equals(Object obj) {
		if (!(obj instanceof Road))
			return false;
		Road b = (Road) obj;
		return this.ID == b.ID;
	}
	
	public int getID() {
		return ID;
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

	public void setLength(double len) {
		this.length = len;
	}
	
	public double getLength() {
		return this.length;
	}
	
//	public void setCoordinates(ArrayList<Coordinate> c) {
//		this.coordinates = c;
//	}
//	
//	public ArrayList<Coordinate> getCoordinates() {
//		return this.coordinates;
//	}
	
	
	public int getSpeedLimit() {
		return speedLimit;
	}

	public void setSpeedLimit(int speedLimit) {
		this.speedLimit = speedLimit;
	}
	
}
