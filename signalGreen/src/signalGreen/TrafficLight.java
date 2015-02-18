package signalGreen;

import java.util.ArrayList;
import java.util.List;

import repast.simphony.space.continuous.ContinuousSpace;
import repast.simphony.space.graph.Network;
import repast.simphony.space.grid.Grid;
import repast.simphony.engine.schedule.ScheduledMethod;
import signalGreen.Constants.Lights;

/**
 * TrafficLight object is a subclass of the Junction object. It has
 * a traffic light dedicated to each lane linked to the Junction.
 * 
 * It contains the Step() method that performs the light changing
 * algorithm.
 * 
 * @author Waqar, Adeela
 * 
 */
public class TrafficLight extends Junction {
	
	//List of lights for each lane linking to the Junction
	private List<Lights> lights;
	
	/**
	 * @param network
	 * @param space
	 * @param grid
	 */
	public TrafficLight(Network<Object> network, ContinuousSpace<Object> space,
			Grid<Object> grid) {
		super(network, space, grid);
		
		this.lights = new ArrayList<Lights>();
		
		for (Junction junc : getJunctions()) {
			Lights light = Lights.RED;
			lights.add(light);
		}
	}
	
	/**
	 * Add traffic light for new lane to given Junction.
	 * 
	 * @see signalGreen.Junction#addLane(signalGreen.Junction, boolean)
	 */
	@Override
	public void addLane(Junction junc, boolean out) {
		Lights light = Lights.RED;
		lights.add(light);
		super.addLane(junc, out);
	}
	
	/**
	 * Remove traffic light for lane to given Junction.
	 * 
	 * @see signalGreen.Junction#removeLane(signalGreen.Junction, boolean)
	 */
	@Override
	public void removeLane(Junction junc, boolean out) {
		lights.remove(getJunctions().indexOf(junc));
		super.removeLane(junc, out);
	}
	
	/**
	 * Remove all traffic lights.
	 * 
	 * @see signalGreen.Junction#removeAllLanes()
	 */
	@Override
	public void removeAllLanes() {
		lights.clear();
		super.removeAllLanes();
	}
	
	/**
	 * Step() method to perform light changing algorithm with the
	 * scheduled method annotation by Repast.
	 */
	@ScheduledMethod(start = 1, interval = 300000)
	public void step() {
		System.out.println("Change light");
		
		// TODO:Implement traffic light changing algorithm.
	}

}
