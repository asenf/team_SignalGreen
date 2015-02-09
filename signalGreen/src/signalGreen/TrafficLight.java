package signalGreen;

import repast.simphony.space.continuous.ContinuousSpace;
import repast.simphony.space.graph.Network;
import repast.simphony.space.grid.Grid;
import repast.simphony.engine.schedule.ScheduledMethod;
import repast_SG_first.Constants.Lights;

public class TrafficLight extends Junction {
	
	private Lights northLight;
	private Lights eastLight;
	private Lights southLight;
	private Lights westLight;
	
	public TrafficLight(Network<Object> network, ContinuousSpace<Object> space,
			Grid<Object> grid) {
		super(network, space, grid);
		
		northLight = Lights.GREEN;
		eastLight = Lights.RED;
		southLight = Lights.GREEN;
		westLight = Lights.RED;
		
	}
	
	@ScheduledMethod(start = 1, interval = 300000)
	public void step() {
		System.out.println("Change light");
	}

}
