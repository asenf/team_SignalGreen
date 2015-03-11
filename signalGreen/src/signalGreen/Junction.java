package signalGreen;

import java.util.ArrayList;
import java.util.Deque;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Queue;

import com.vividsolutions.jts.geom.Coordinate;

import repast.simphony.context.Context;
import repast.simphony.engine.schedule.ScheduledMethod;
import repast.simphony.space.continuous.ContinuousSpace;
import repast.simphony.space.continuous.NdPoint;
import repast.simphony.space.gis.Geography;
import repast.simphony.space.graph.Network;
import repast.simphony.space.graph.RepastEdge;
import repast.simphony.space.grid.Grid;
import signalGreen.Constants.Lane;

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
	
	private List<Road> roads;
	//List of Junctions it has a lane between
	private List<Junction> junctions;
	// Map holds a queue of Vehicles for each Junction.
	// This way we know for each incoming road segment to the current junction
	// which vehicles are approaching.
	public Map<Junction, PriorityBlockingDeque<Vehicle>> vehicles;
			
	/**
	 * @param network
	 * @param space
	 * @param grid

	 */
	public Junction(Network<Junction> network, Geography geography) {
		super(network, geography);
		this.junctions = new ArrayList<Junction>();
		this.roads = new ArrayList<Road>();
		this.vehicles = new HashMap<Junction, PriorityBlockingDeque<Vehicle>>();
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
<<<<<<< HEAD
	
=======
	public void removeLane(Junction junc, boolean out) {
		this.junctions.remove(junc);
		
		RepastEdge<Junction> edge;
		
		if (out) {
			edge = getNetwork().getEdge(this, junc);
		} else {
			edge = getNetwork().getEdge(junc, this);
		}
		
		if (edge != null) 
			getNetwork().removeEdge(edge);
	}
>>>>>>> multi_lane_feature
	
	/**
	 * Remove all lanes joining toward this Junction, the Graph
	 * will therefore have no Edges to this Node.
	 */
<<<<<<< HEAD
=======
	public void removeAllLanes() {
		
		RepastEdge<Junction> edgeIn;
		RepastEdge<Junction> edgeOut;
		
		for (Junction junc : junctions) {
			edgeIn = getNetwork().getEdge(this, junc);
			edgeOut = getNetwork().getEdge(junc, this);
			getNetwork().removeEdge(edgeIn);
			getNetwork().removeEdge(edgeOut);
		}
		
		this.junctions.clear();
	}
>>>>>>> multi_lane_feature
	
    @Override
    public boolean equals(Object obj) {
        if (!(obj instanceof Junction)) {
                return false;
        }
        Junction j = (Junction) obj;
        return this.getCoords().equals(j.getCoords());
    }

	public List<Road> getRoads() {
		return this.roads;
	}
	
	
	/**
	 * Returns a list of vehicles that are running on a road segment
	 * from j to this junction. Assumes j is in the this.junctions list.
	 * 
	 * @param j the junction
	 * @return queue of vehicles
	 */
	public Queue getVehiclesQueue(Junction j) {
		return this.vehicles.get(j);
	}
	
	/**
	 * Used by the context builder class only.
	 * 
	 * @return all queues for initialisation purposes
	 */
	public Map<Junction, PriorityBlockingDeque<Vehicle>> getVehiclesMap() {
		return this.vehicles;
	}
	
	/**
	 * Every vehicle entering a new road segment should call this method.
	 * Every junction holds a queue of vehicles running on a particular road
	 * segment going towards this junction from junction j.
	 * 
	 * @param j junction at the other side of the current road segment
	 * @param v vehicle entering a road segment
	 */
	public void enqueueVehicle(Junction j, Vehicle v) {
		// System.out.println("J: " + j.toString());
		PriorityBlockingDeque<Vehicle> q = this.vehicles.get(j);
		q.add(v);
	}
	
	public void reorderVehicle(Junction j, Vehicle v) {
//		System.out.println("\n\nReorder vehicle " + v.toString() + " in q");
		PriorityBlockingDeque<Vehicle> q = this.vehicles.get(j);
//		System.out.println("q bef: " + q.toString());
		q.remove(v);
//		System.out.println("q mid: " + q.toString());
		q.add(v);
//		System.out.println("q aft: " + q.toString());
//		System.out.println("Peek: " + q.peek().toString());
	}
	
	/**
	 * Every vehicle leaving a road segment should call this method.
	 * Vehicles are removed from queue.
	 * 
	 * @see signalGreen.Junction#enqueueVehicle(Junction j, Vehicle v)
	 * @param j junction at the other side of the current road segment
	 * @param v vehicle leaving a road segment
	 * @return true if success
	 */
	public boolean dequeueVehicle(Junction j, Vehicle v) {
		PriorityBlockingDeque<Vehicle> q = this.vehicles.get(j);
		return q.remove(v);
	}
	
	/**
	 * Returns the closest vehicle to the current junction
	 * from junction j. It does not take into account lanes.
	 * 
	 * @param j the junction
	 * @return v closest vehicle from j, if any
	 */
	public Vehicle peekVehicle(Junction j) {
		Vehicle v = null;
		PriorityBlockingDeque<Vehicle> q = this.vehicles.get(j);
		v = q.element();
		return v;
	}
	
	/**
	 * Debug the vehicles queue for a particular junction.
	 * 
	 * @param j the junction
	 */
	public void printVehiclesQueue(Junction j) {
		PriorityBlockingDeque<Vehicle> q = this.vehicles.get(j);
		System.out.println(q.toString());
		System.out.println("Peek vehicle: " + this.peekVehicle(j).toString());
	}
	
	/**
	 * Returns the next vehicle on the Queue (road segment)
	 * in a particular lane.
	 * 
	 * @param j the junction from which the vehicle comes
	 * @param v the current vehicle
	 * @param l which lane to check
	 * @return leader vehicle
	 */
//	public Vehicle getNextVehicle(Junction j, Vehicle v, Constants.Lane l) {
//		Vehicle leader = null;
//		Vehicle curr = null;
//		Vehicle prev = null;
//		boolean isPrev = false;
//		PriorityBlockingDeque<Vehicle> q = this.vehicles.get(j);
//
//		for(Object o : q) {
//			curr = (Vehicle) o;
//		    // find current vehicle's position
//		    if (curr.equals(v)) {
//		    	isPrev = true;		    	
//		    }
//			
//			// check also if next vehicle is on the required lane
//		    //  && (prev.getLane() == l)
//		    if ((isPrev == true)) { 
//		    	leader = prev;
//		    	break;
//		    }
//		    prev = curr;
//		}
//		return leader;
//	}
	
	public Vehicle getNextVehicle(Junction j, Vehicle v, Constants.Lane l) {
		Vehicle leader = null;
		Vehicle tmp = null;
		boolean found = false;
		PriorityBlockingDeque<Vehicle> q = this.vehicles.get(j);

		Iterator<Vehicle> it = q.descendingIterator();
		while (it.hasNext()) {
			tmp = it.next();
			
			if (found == true) {
				leader = tmp;
				break;
			}
			
			if (tmp.equals(v)) {
				found = true;
			}
		}
		return leader;
	}
	/**
	 * Returns an array with size of 2 of vehicles 
	 * that are ahead/behind of a given vehicle as follows:<br />
	 * v[0] => Vehicle on Lane.OUTER<br />
	 * v[1] => Vehicle on Lane.INNER
	 * 
	 * @param j junction
	 * @param v vehicle
	 * @param checkAhead check for vehicle ahead if true
	 * @return array of vehicles
	 */
	public Vehicle[] getNextVehicles(Junction j, Vehicle vehicle, boolean checkAhead) {
		Vehicle[] v = new Vehicle[2];
		v[0] = null; // outer lane
		v[1] = null; // inner lane
		Vehicle tmp = null;
		boolean found = false;
		boolean foundOuter = false;
		boolean foundInner = false;
		PriorityBlockingDeque<Vehicle> q = this.vehicles.get(j);

		Iterator<Vehicle> it = null;
		if (checkAhead == true) {
			it = q.descendingIterator();
		}
		else {
			it = q.iterator();
		}
		
		while (it.hasNext()) {
			tmp = it.next();
			
			if (found == true) {
				if (foundOuter == false && tmp.getLane() == Lane.OUTER) {
					v[0] = tmp;
					foundOuter = true;				
				}
				if (foundInner == false && tmp.getLane() == Lane.INNER) {
					v[1] = tmp;
					foundInner = true;				
				}
			}
			
			if (tmp.equals(vehicle)) {
				found = true;
			}
		}
		return v;
	}
	
	/**
	 * How many vehicles there are on this road segment.
	 *  
	 * @param j the origin junction
	 * @return number of vehicles
	 */
	public int getNumVehicles(Junction j) {
		Vehicle v = null;
		PriorityBlockingDeque<Vehicle> q = this.vehicles.get(j);
		return q.size();
	}
}
