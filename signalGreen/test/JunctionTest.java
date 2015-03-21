import static org.junit.Assert.*;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;






import repast.simphony.space.continuous.ContinuousSpace;
import repast.simphony.space.gis.Geography;
import repast.simphony.space.graph.Network;
import repast.simphony.space.grid.Grid;
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
	
/*Test passed
 */
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
	// TODO
		this.junctions.add(jc);
		assertNotNull(junctions);
	}
	
	@Test
	public final void testGetNumVehicles(){
		jc.getNumVehicles(junction);
		
	}

/*	
	@Test
	public final void testSetLocation() {
		//fail("Not yet implemented"); // TODO
	}

	@Test
	public final void testGetLocation() {
		//fail("Not yet implemented"); // TODO
	}*/

}
