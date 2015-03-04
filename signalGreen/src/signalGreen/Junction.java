package signalGreen;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Queue;

import com.vividsolutions.jts.geom.Coordinate;

import repast.simphony.context.Context;
import repast.simphony.space.continuous.ContinuousSpace;
import repast.simphony.space.continuous.NdPoint;
import repast.simphony.space.gis.Geography;
import repast.simphony.space.graph.Network;
import repast.simphony.space.graph.RepastEdge;
import repast.simphony.space.grid.Grid;

/**
 * Generic class for junctions of the Traffic Simulator.<br />
 * 
 * A Junction object is a node on a Graph. It can have a lane to
 * any number of other Junction objects. Each lane can have 
 * the direction specified and these lanes represent Edges 
 * on the Graph.
 * 
 * TrafficLights is a subclass of Junction, and has the special 
 * behavior of scheduling traffic light management.
 * 
 * @author Waqar
 *
 */
public class Junction extends GisAgent {

	// Repast projections
	private Network<Junction> network;
	private Geography geography;
	
	private Coordinate coordinate;
	public static int UniqueID = 0;
	private int ID;
	private List<Road> roads;
	//List of Junctions it has a lane between
	private List<Junction> junctions;
	// Map holds a queue of Vehicles for each Junction.
	// This way we know for each incoming road segment to the current junction
	// which vehicles are approaching.
	public Map<Junction, Queue<Vehicle>> vehicles;
			
	/**	 * @param network
	 * @param space
	 * @param grid

	 */
	public Junction(Network<Junction> network, Geography geography) {
		this.network = network;
		this.geography = geography;
		this.junctions = new ArrayList<Junction>();
		this.ID = UniqueID++;
		this.roads = new ArrayList<Road>();
		this.vehicles = new HashMap<Junction, Queue<Vehicle>>();
	}
	
	/**
	 * @return List of junctions that this junction has a lane between.
	 */
	public List<Junction> getJunctions() {
		return junctions;
	}
	
	/**
	 * Tells the Junction about its adjacent Junctions. The given Junction 
	 * is added to the List of Junctions that it now has a lane between. 
	 * 
	 * @param junc is the other Junction the lane will be between.
	 */
	public void addJunction(Junction j) {
		this.junctions.add(j);
	}
	
	/**
	 * Remove a lane between another Junction. This given Junction
	 * is removed from the List of Junctions that it has a lane between. 
	 * It represents an Edge being removed on the Graph and is therefore 
	 * updated on the Network Object.
	 * 
	 * @param junc is the other Junction the lane is between.
	 * @param out is a boolean flag for the lane direction being outward.
	 */
	public void removeLane(Junction junc, boolean out) {
		this.junctions.remove(junc);
		
		RepastEdge<Junction> edge;
		
		if (out) {
			edge = network.getEdge(this, junc);
		} else {
			edge = network.getEdge(junc, this);
		}
		
		if (edge != null) 
			network.removeEdge(edge);
	}
	
	/**
	 * Remove all lanes joining toward this Junction, the Graph
	 * will therefore have no Edges to this Node.
	 */
	public void removeAllLanes() {
		
		RepastEdge<Junction> edgeIn;
		RepastEdge<Junction> edgeOut;
		
		for (Junction junc : junctions) {
			edgeIn = network.getEdge(this, junc);
			edgeOut = network.getEdge(junc, this);
			network.removeEdge(edgeIn);
			network.removeEdge(edgeOut);
		}
		
		this.junctions.clear();
	}
	
    @Override
    public boolean equals(Object obj) {
            if (!(obj instanceof Junction)) {
                    return false;
            }
            Junction j = (Junction) obj;
            return this.getCoords().equals(j.getCoords());
    }

    /**
     * Get the coordinates of current Junction
     */
    public Coordinate getCoords() {
            return coordinate;
    }
    
    public void setCoords(Coordinate c) {
            this.coordinate = c;
            
    }

	public int getID() {
		return ID;
	}
	
	public List<Road> getRoads() {
		return this.roads;
	}
	
	
	/**
	 * Returns a list of vehicles that are running on a road segment
	 * from j to this junction. Assumes j is in the this.junctions list.
	 * @param j the junction
	 * @return queue of vehicles
	 */
	public Queue getVehiclesQueue(Junction j) {
		return this.vehicles.get(j);
	}
	
	/**
	 * Used by the context builder class only.
	 * @return all queues for initialisation purposes
	 */
	public Map<Junction, Queue<Vehicle>> getVehiclesMap() {
		return this.vehicles;
	}
	
	/**
	 * Every vehicle entering a new road segment should call this method.
	 * Every junction holds a queue of vehicles running on a particular road
	 * segment going towards this junction from junction j.
	 * @param j junction at the other side of the current road segment
	 * @param v vehicle entering a road segment
	 */
	public void enqueueVehicle(Junction j, Vehicle v) {
		// System.out.println("J: " + j.toString());
		Queue<Vehicle> q = this.vehicles.get(j);
		q.add(v);
	}
	
	/**
	 * Every vehicle leaving a road segment should call this method.
	 * @see signalGreen.enqueueVehicle(Junction j, Vehicle v)
	 * @param j junction at the other side of the current road segment
	 * @param v vehicle leaving a road segment
	 * @return
	 */
	public boolean dequeueVehicle(Junction j, Vehicle v) {
		Queue<Vehicle> q = this.vehicles.get(j);
		return q.remove(v);
	}
	
	/**
	 * Returns the closest vehicle to the current junction
	 * from junction j.
	 * @param j the junction
	 * @return v closest vehicle from j, if any
	 */
	public Vehicle peekVehicle(Junction j) {
		Vehicle v = null;
		Queue<Vehicle> q = this.vehicles.get(j);
		v = q.element();
		return v;
	}
	
	
	/**
	 * Debug the vehicles queue for a particular junction.
	 * @param j the current junction
	 */
	public void printVehiclesQueue(Junction j) {
		Queue<Vehicle> q = this.vehicles.get(j);
		System.out.println(q.toString());
		System.out.println("Peek vehicle: " + this.peekVehicle(j).toString());
	}
	
}
