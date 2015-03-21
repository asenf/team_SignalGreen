import static org.junit.Assert.*;

import java.util.Map;

import org.junit.Test;

import repast.simphony.space.continuous.ContinuousSpace;
import repast.simphony.space.gis.Geography;
import repast.simphony.space.graph.Network;
import repast.simphony.space.graph.RepastEdge;
import repast.simphony.space.grid.Grid;
import signalGreen.Junction;
import signalGreen.Road;
import signalGreen.Vehicle;

public class VehicleTest {
	//Repast Projections
	private ContinuousSpace<Object> space;
	private Grid<Object> grid;
	private Network<Junction> roadNetwork;
	private int velocity;
	private int maxVelocity;
	private int acceleration = 3; // m/s
	private Map<RepastEdge<Junction>,Road> roads;
	private Geography geography;
	@Test
	public void testInitVehicle() {
		fail("Not yet implemented"); // TODO
	}

	@Test
	public void testMoveTowards(){
		Vehicle vc = new Vehicle(roadNetwork,geography,roads,maxVelocity);
		vc.moveTowards(vc.getCoords(), 4);
		
	}

}
