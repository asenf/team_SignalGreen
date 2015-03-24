package signalGreen;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Queue;
import java.awt.*;

import com.vividsolutions.jts.geom.Coordinate;
import com.vividsolutions.jts.geom.GeometryFactory;
import com.vividsolutions.jts.geom.Point;

import repast.simphony.space.continuous.ContinuousSpace;
import repast.simphony.space.gis.Geography;
import repast.simphony.space.graph.Network;
import repast.simphony.space.grid.Grid;
import repast.simphony.context.Context;
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
	
	// List of lights for each lane linking from a Junction
	// to this Junction
	private Map<Junction, Light> lights;
	
	/**
	 * @param network
	 */
	public TrafficLight(Network<Junction> network, Geography geography) {
		super(network, geography);
		this.lights = new HashMap<Junction, Light>();
	}
	
	/**
	 * Add traffic light for new lane to given Junction. If it is the
	 * first light, set state to GREEN, else RED.
	 * 
	 * @see signalGreen.Junction#addJunction(signalGreen.Junction)
	 */
	@Override
	public void addJunction(Junction junc) {
		super.addJunction(junc);	
		
		Light light = new Light(Signal.RED);
		
		if(lights.size() == 0)  {
			light.setSignal(Signal.GREEN);
		}
		
		lights.put(junc, light);		
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
	@ScheduledMethod(start = 1, interval = 25)
	public void step() {
		// TODO find optimal interval	
		if (lights.size() != 0) {
			toggleNextLight();
		}
		// DEBUG
		// debugLights();
	}
	
	public void debugLights() {
		for (Map.Entry<Junction, Light> entry : lights.entrySet()) {
		    // System.out.println("key=" + entry.getKey() + ", value=" + entry.getValue());
		    Light l = entry.getValue();
		    Junction j = entry.getKey();
		    if (l.getSignal() == Signal.GREEN)
		    	System.out.println(l.toString() + " : GREEN");
		    if (l.getSignal() == Signal.AMBER)
		    	System.out.println(l.toString() + " : AMBER");
		    if (l.getSignal() == Signal.RED)
		    	System.out.println(l.toString() + " : RED");
		}		
	}

	/**
	 * Get a List of all Lights, their indexes match junctions indexes.
	 * @return lights
	 */
	public Map<Junction, Light> getLights() {
		return lights;
	}
		
	/**
	 * Toggle to the next traffic light.
	 */
	public void toggleNextLight() {
		int lastGreenLightIndex = 0;
				
		List<Light> l = new ArrayList<Light>(lights.values());
		
		for (Light light : l) {
			if (light.getSignal() == Signal.GREEN) {
				lastGreenLightIndex = l.indexOf(light);
			}
		}
		
		l.get(lastGreenLightIndex).toggleSignal();
		
		if (l.size() - 1 == lastGreenLightIndex) {
			l.get(0).toggleSignal();
			
		} else {
			l.get(lastGreenLightIndex + 1).toggleSignal();
		}
		
	}

}
