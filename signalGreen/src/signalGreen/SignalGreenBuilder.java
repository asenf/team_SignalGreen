package signalGreen;

import java.util.ArrayList;
import java.util.List;

import repast.simphony.context.Context;
import repast.simphony.context.space.continuous.ContinuousSpaceFactory;
import repast.simphony.context.space.continuous.ContinuousSpaceFactoryFinder;
import repast.simphony.context.space.graph.NetworkBuilder;
import repast.simphony.context.space.grid.GridFactory;
import repast.simphony.context.space.grid.GridFactoryFinder;
import repast.simphony.dataLoader.ContextBuilder;
import repast.simphony.space.continuous.ContinuousSpace;
import repast.simphony.space.continuous.NdPoint;
import repast.simphony.space.continuous.RandomCartesianAdder;
import repast.simphony.space.graph.Network;
import repast.simphony.space.grid.Grid;
import repast.simphony.space.grid.GridBuilderParameters;
import repast.simphony.space.grid.SimpleGridAdder;
import repast.simphony.space.grid.WrapAroundBorders;

/**
 * This is custom context builder implementation which is responsible 
 * to perform the initialization of the Traffic Simulator.
 * 
 * @see repast.simphony.dataLoader.ContextBuilder
 * @author Waqar, Yoann
 *
 */
public class SignalGreenBuilder implements ContextBuilder<Object> {

	// Repast projections
	private Context<Object> context;
	private Network<Object> network;
	private ContinuousSpace<Object> space;
	private Grid<Object> grid;
	
	// List to store Junctions
	private List<Junction> junctions;
	
	/**
	 * Builds and returns a context of the traffic simulation. Building a 
	 * this consists of filling it with the agents such as Junction and
	 * Vehicles, and adding projections such as the Graph.
	 * 
	 *  @see repast.simphony.dataLoader.ContextBuilder#build(repast.simphony.context.Context)
	 */
	@Override
	public Context build(Context<Object> context) {
		
		this.context = context;
		this.context.setId(Constants.ID);
		
		NetworkBuilder<Object> roadBuilder = new NetworkBuilder<Object>(Constants.NETWORK, context, true);
		roadBuilder.buildNetwork();
		
		this.network = (Network<Object>)context.getProjection(Constants.NETWORK);
		this.junctions = new ArrayList<Junction>();
		
		ContinuousSpaceFactory spaceFactory = ContinuousSpaceFactoryFinder.createContinuousSpaceFactory(null);
		this.space = spaceFactory.createContinuousSpace(Constants.SPACE, context, 
				new RandomCartesianAdder<Object>(),
				new repast.simphony.space.continuous.WrapAroundBorders(),
				50, 50);
		
		GridFactory gridFactory = GridFactoryFinder.createGridFactory(null);
		this.grid = gridFactory.createGrid(Constants.GRID, context, 
				new GridBuilderParameters<Object>(new WrapAroundBorders(), 
				new SimpleGridAdder<Object>(),
				true, 50, 50));
	
		buildJunctionsAndLanes(junctions, Constants.SCALE);
		addVehicleAtJunction(junctions.get(0), 280);
		addVehicleAtJunction(junctions.get(1), 40);
		addVehicleAtJunction(junctions.get(4), 200);
		
		for (Object obj : context) {
			NdPoint pt = space.getLocation(obj);
			grid.moveTo(obj, (int)pt.getX(), (int)pt.getY());
		}
			
		return context;
	}
	
	/**
	 * Initialize and draw out a grid of Junction objects, with the
	 * default lanes specified. 
	 * 
	 * @param junctions is the List to store all Junction objects.
	 * @param scale is the scale for the grid to be drawn.
	 */
	public void buildJunctionsAndLanes(List<Junction> junctions, int scale) {
		
		for (int i = 1; i < 5; i++) {
		    for (int j = 1; j < 5; j++) {
		    	Junction junc = new Junction(network, space, grid);
		    	junctions.add(junc);
		    	context.add(junc);
		    	junc.setLocation(scale*i, scale*j);
		    }
		}
		
		junctions.get(0).addLane(junctions.get(1), true);
		junctions.get(1).addLane(junctions.get(2), true);
		junctions.get(2).addLane(junctions.get(3), true);
		junctions.get(4).addLane(junctions.get(5), true);
		junctions.get(5).addLane(junctions.get(6), true);
		junctions.get(6).addLane(junctions.get(7), true);
		junctions.get(8).addLane(junctions.get(9), true);
		junctions.get(9).addLane(junctions.get(10), true);
		junctions.get(10).addLane(junctions.get(11), true);
		junctions.get(12).addLane(junctions.get(13), true);
		junctions.get(13).addLane(junctions.get(14), true);
		junctions.get(14).addLane(junctions.get(15), true);
		junctions.get(0).addLane(junctions.get(4), true);
		junctions.get(1).addLane(junctions.get(5), true);
		junctions.get(2).addLane(junctions.get(6), true);
		junctions.get(3).addLane(junctions.get(7), true);
		junctions.get(4).addLane(junctions.get(8), true);
		junctions.get(5).addLane(junctions.get(9), true);
		junctions.get(6).addLane(junctions.get(10), true);
		junctions.get(7).addLane(junctions.get(11), true);
		junctions.get(8).addLane(junctions.get(12), true);
		junctions.get(9).addLane(junctions.get(13), true);
		junctions.get(10).addLane(junctions.get(14), true);
		junctions.get(11).addLane(junctions.get(15), true);
		
	}
	
	/**
	 * Add Vehicle object to a Junction object.
	 * 
	 * Here the vehicle is initialized with the projections and a
	 * maximum speed for it. It is then added to the Context and 
	 * moved to the given Junction coordinates in the Continuous Space. 
	 * 
	 * @param junc is the given Junction object.
	 * @param maxSpeed is the given Maximum Speed for the vehicle.
	 */
	public void addVehicleAtJunction(Junction junc, int maxSpeed) {
		
		Vehicle vehicle = new Vehicle(space, grid, network, maxSpeed);
		context.add(vehicle);
		NdPoint location = space.getLocation(junc);
		space.moveTo(vehicle, location.getX(), location.getY());
		vehicle.initVehicle(junc);
	}

}
