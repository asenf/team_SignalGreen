package signalGreen;

import java.util.ArrayList;
import java.util.List;

import repast.simphony.space.continuous.ContinuousSpace;
import repast.simphony.space.continuous.NdPoint;
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
public class Junction {

	/**
	 * Prints out number of Junctions that this Junction has a lane between.
	 */
	@Override
	public String toString() {
		return "Number of lanes: " + junctions.size();
	}

	// Repast projections
	private Network<Object> network;
	private ContinuousSpace<Object> space;
	private Grid<Object> grid;
	
	//List of Junctions it has a lane between
	private List<Junction> junctions;
			
	/**
	 * @param network
	 * @param space
	 * @param grid
	 */
	public Junction(Network<Object> network, ContinuousSpace<Object> space, 
			Grid<Object> grid) {
	
		this.network = network;
		this.space = space;
		this.grid = grid;
		this.junctions = new ArrayList<Junction>();
	}
	
	/**
	 * @return List of junctions that this junction has a lane between.
	 */
	public List<Junction> getJunctions() {
		return junctions;
	}
	
	/**
	 * Create a lane to another Junction. The given Junction 
	 * is added to the List of Junctions that it now has a lane between. 
	 * This new lane represents a new Edge on the Graph and is therefore
	 * updated on the Network object.
	 * 
	 * @param junc is the other Junction the lane will be between.
	 * @param out is a boolean flag for the lane direction being outward.
	 * @param weight for this new edge on the Graph. 
	 */
	public void addLane(Junction junc, boolean out, double weight) {
		this.junctions.add(junc);
		
		if (out) {
			network.addEdge(this, junc, weight);
		} else {
			network.addEdge(junc, this, weight);
		}
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
		
		RepastEdge<Object> edge;
		
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
		
		RepastEdge<Object> edgeIn;
		RepastEdge<Object> edgeOut;
		
		for (Junction junc : junctions) {
			edgeIn = network.getEdge(this, junc);
			edgeOut = network.getEdge(junc, this);
			network.removeEdge(edgeIn);
			network.removeEdge(edgeOut);
		}
		
		this.junctions.clear();
	}
	
	/**
	 * Sets the Junction location in the Continuous Space object.
	 * 
	 * @param x is the x-coordinate.
	 * @param y is the y-coordinate.
	 */
	public void setLocation(int x, int y) {
		space.moveTo(this, x, y);
	}
	
	/**
	 * Returns the Junction location in the Continuous Space object.
	 * 
	 * @return the Junction location as (x,y) in an NdPoint object.
	 */
	public NdPoint getLocation() {
		return space.getLocation(this);
	}
	
}
