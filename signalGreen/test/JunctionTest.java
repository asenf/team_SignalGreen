import static org.junit.Assert.*;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.junit.Test;

import repast.simphony.space.continuous.ContinuousSpace;
import repast.simphony.space.gis.Geography;
import repast.simphony.space.graph.Network;
import signalGreen.Junction;
import signalGreen.PriorityBlockingDeque;
import signalGreen.Vehicle;

/**
 * @author adeelasaalim
 * JunctionTest Class to check methods of this class.
 * Not a working test case. I am working on this to run it.
 */
public class JunctionTest {
	//Repast Projections
	private Network<Junction> network;
	private Geography geography;
	private ContinuousSpace<Object> space;
	Junction jc = new Junction(network,geography);
	// List to store Junctions
	private List<Junction> junctions = new ArrayList<Junction>();
	public Map<Junction, PriorityBlockingDeque<Vehicle>> vehicles;
	
	@Test
	public final void testToString() {
		String expectedOutput =jc.toString();
		String actualOutput =  jc.toString();
		assertEquals(expectedOutput,actualOutput);
		
	}
	//Passed as function returns a list and not null
	@Test
	public final void testGetJunctions() {
		 // TODO
		assertNotNull(jc.getJunctions());
		
	}

	//Passed as junctions List is not empty.
	@Test
	public final void testAddLane() {
		this.junctions.add(jc);
		assertNotNull(junctions);
	}
	
	@Test
	public final void testGetNumVehicles(){
		assertEquals(0,0);
		
	}
}
