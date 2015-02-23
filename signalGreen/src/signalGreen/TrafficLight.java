package signalGreen;

import java.util.ArrayList;
import java.util.List;
import java.awt.*;

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
public class TrafficLight extends Junction{
	
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
		
	}
	
	/**
	 * Add traffic light for new lane to given Junction. If it is the
	 * first light, set state to GREEN, else RED.
	 * 
	 * @see signalGreen.Junction#addLane(signalGreen.Junction, boolean)
	 */
	@Override
	public void addLane(Junction junc, boolean out, double weight) {
		Light light = new Light(Signal.RED);
		
		if(lights.size() == 0)  {
			light.setSignal(Signal.GREEN);
		}
		
		lights.add(light);
		super.addLane(junc, out, weight);
		
		System.out.println("Added Light");
		
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
	@ScheduledMethod(start = 1, interval = 3)
	public void step() {
		
		System.out.println("Change " + lights.size() + " light(s)!");
		
		if (lights.size() != 0) {
			toggleNextLight();
		}
			
		for (int i = 0; i < lights.size(); i++) {
			if (lights.get(i).getSignal() == Signal.GREEN)
				System.out.println("Light " + i + " : GREEN");
		}
	}
	
	/**
	 * Get a List of all Lights, their indexes match junctions indexes.
	 * @return
	 */
	public List<Light> getLights() {
		return lights;
	}
	
	/**
	 * Get a List of all Lights currently in GREEN state.
	 * 
	 * @return List of GREEN lights.
	 */
	public List<Light> getLightsOn() {
		List<Light> lightsOn = new ArrayList<Light>();
		for (Light light : lights) {
			if (light.getSignal() == Signal.GREEN) {
				lightsOn.add(light);
			}
		}
		
		return lightsOn;
	}
	
	/**
	 * Toggle the current state of all traffic lights to GREEN.
	 */
	public void toggleAllLightsOn() {
		for (Light light : lights) {
			light.setSignal(Signal.GREEN);
		}
	}
	
	/**
	 * Toggle the current state of all traffic lights to RED.
	 */
	public void toggleAllLightsOff() {
		for (Light light : lights) {
			light.setSignal(Signal.RED);
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
	/**
	 * Not working, just a try to draw a traffic light
	 * @param gc
	 */
	public void drawTrafficLight(Graphics gc){
		gc.setColor(Color.GREEN);
		gc.fillOval(65,65,85,85);
		
	}

}
