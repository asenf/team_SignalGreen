package signalGreen;

import java.util.ArrayList;

import com.vividsolutions.jts.geom.Coordinate;

import repast.simphony.space.graph.RepastEdge;

public class Road extends GisAgent {

	public static int UniqueID = 0;
	private int ID;
	private String name;
	private RepastEdge<Junction> inEdge = null;
	private RepastEdge<Junction> outEdge = null;
	private ArrayList<Junction> junctions;
	private ArrayList<Coordinate> coordinates; // A list of coordinates between the two junctions
	private double length = 0;
	private int speedLimit;
	
	public Road(String name, int ) {
		super();
		this.name = name;
		this.ID = UniqueID++;
		this.junctions = new ArrayList<Junction>();
		this.coordinates = new ArrayList<Coordinate>();
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

	public RepastEdge<Junction> getInEdge() {
		return inEdge;
	}

	public void setInEdge(RepastEdge<Junction> edge) {
		this.inEdge = edge;
	}

	public RepastEdge<Junction> getOutEdge() {
		return outEdge;
	}

	public void setOutEdge(RepastEdge<Junction> edge) {
		this.outEdge = edge;
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
	
	public void setCoordinates(ArrayList<Coordinate> c) {
		this.coordinates = c;
	}
	
	public ArrayList<Coordinate> getCoordinates() {
		return this.coordinates;
	}
	
}
