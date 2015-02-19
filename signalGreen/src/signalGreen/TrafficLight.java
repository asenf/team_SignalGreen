package signalGreen;

import java.util.ArrayList;
import java.util.List;

import repast.simphony.space.continuous.ContinuousSpace;
import repast.simphony.space.graph.Network;
import repast.simphony.space.grid.Grid;
import repast.simphony.engine.schedule.ScheduledMethod;
import signalGreen.Constants.Signal;

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
	private List<Light> lights;
	
	/**
	 * @param network
	 * @param space
	 * @param grid
	 */
	public TrafficLight(Network<Object> network, ContinuousSpace<Object> space,
			Grid<Object> grid) {
		super(network, space, grid);
		
		this.lights = new ArrayList<Light>();
		
		//Have each light initialized with the number of junctions and set RED.
		for (Junction junc : getJunctions()) {
			Light light = new Light(Signal.RED);
			lights.add(light);
		}
		
		//If even, toggle every other light. If odd, toggle first light.
		if (lights.size() % 2 == 0) {
			for (int i = 0; i < lights.size(); i += 2) {
				lights.get(i).toggleSignal();
			}
		} else {
			lights.get(0).toggleSignal();
		}
		
	}
	
	/**
	 * Add traffic light for new lane to given Junction.
	 * 
	 * @see signalGreen.Junction#addLane(signalGreen.Junction, boolean)
	 */
	@Override
	public void addLane(Junction junc, boolean out, double weight) {
		Light light = new Light(Signal.RED);
		lights.add(light);
		super.addLane(junc, out, weight);
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
		
		if (lights.size() % 2 == 0) {
			toggleAllLights();
		} else {
			toggleNextLight();
		}
	}
	
	/**
	 * Toggle the current state of all traffic lights.
	 */
	public void toggleAllLights() {
		for (Light light : lights) {
			light.toggleSignal();
		}
	}
	
	/**
	 * Toggle to the next traffic light.
	 */
	public void toggleNextLight() {
		int lastGreenLightIndex = 0;
		
		for (Light light : lights) {
			if (light.getSignal() == Signal.GREEN) {
				lastGreenLightIndex = lights.indexOf(light);
			}
		}
		
		lights.get(lastGreenLightIndex).toggleSignal();
		
		if (lights.size() == lastGreenLightIndex) {
			lights.get(0).toggleSignal();
			
		} else {
			lights.get(lastGreenLightIndex + 1).toggleSignal();
		}	
	}

}
