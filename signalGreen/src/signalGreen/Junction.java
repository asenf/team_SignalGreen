package signalGreen;

import java.util.ArrayList;
import java.util.List;

import com.vividsolutions.jts.geom.Coordinate;

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
public class Junction extends GisAgent {

	/**
	 * Prints out number of Junctions that this Junction has a lane between.
	 */
	@Override
	public String toString() {
		return "Number of lanes: " + junctions.size();
	}

	// Repast projections
	private Network<Junction> network;
	
	private Coordinate coordinate;
	public static int UniqueID = 0;
	private int ID;
	private List<Road> roads;
	
	//List of Junctions it has a lane between
	private List<Junction> junctions;
			
	/**	 * @param network
	 * @param space
	 * @param grid

	 */
	public Junction(Network<Junction> network) {
	
		this.network = network;
		this.junctions = new ArrayList<Junction>();
		this.ID = UniqueID++;
		this.roads = new ArrayList<Road>();
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
	
}
