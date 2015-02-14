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

public class SignalGreenBuilder implements ContextBuilder<Object> {

	private Context<Object> context;
	private Network<Object> network;
	private ContinuousSpace<Object> space;
	private Grid<Object> grid;
	
	private List<Junction> junctions;
	
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
	
		buildLayout(junctions, Constants.SCALE);
		addVehicleAtJunction(junctions.get(0), 280);
		addVehicleAtJunction(junctions.get(1), 40);
		addVehicleAtJunction(junctions.get(4), 200);
		
		for (Object obj : context) {
			NdPoint pt = space.getLocation(obj);
			grid.moveTo(obj, (int)pt.getX(), (int)pt.getY());
		}
			
		return context;
	}
	
	public void buildLayout(List<Junction> junctions, int scale) {
		
		for (int i = 1; i < 5; i++) {
		    for (int j = 1; j < 5; j++) {
		    	Junction junc = new Junction(network, space, grid);
		    	junctions.add(junc);
		    	context.add(junc);
		    	junc.setLocation(scale*i, scale*j);
		    }
		}
		
		junctions.get(0).setEastJunc(junctions.get(1));
		junctions.get(1).setEastJunc(junctions.get(2));
		junctions.get(2).setEastJunc(junctions.get(3));
		junctions.get(4).setEastJunc(junctions.get(5));
		junctions.get(5).setEastJunc(junctions.get(6));
		junctions.get(6).setEastJunc(junctions.get(7));
		junctions.get(8).setEastJunc(junctions.get(9));
		junctions.get(9).setEastJunc(junctions.get(10));
		junctions.get(10).setEastJunc(junctions.get(11));
		junctions.get(12).setEastJunc(junctions.get(13));
		junctions.get(13).setEastJunc(junctions.get(14));
		junctions.get(14).setEastJunc(junctions.get(15));
		
		junctions.get(0).setNorthJunc(junctions.get(4));
		junctions.get(1).setNorthJunc(junctions.get(5));
		junctions.get(2).setNorthJunc(junctions.get(6));
		junctions.get(3).setNorthJunc(junctions.get(7));
		junctions.get(4).setNorthJunc(junctions.get(8));
		junctions.get(5).setNorthJunc(junctions.get(9));
		junctions.get(6).setNorthJunc(junctions.get(10));
		junctions.get(7).setNorthJunc(junctions.get(11));
		junctions.get(8).setNorthJunc(junctions.get(12));
		junctions.get(9).setNorthJunc(junctions.get(13));
		junctions.get(10).setNorthJunc(junctions.get(14));
		junctions.get(11).setNorthJunc(junctions.get(15));
		
		
		//TrafficLight trafficJunc = new TrafficLight(network, space, grid);
		//context.add(trafficJunc);
		//trafficJunc.setLocation(0, 0);
		
	}
	
	public void addVehicleAtJunction(Junction junc, int maxSpeed) {
		
		Vehicle vehicle = new Vehicle(space, grid, network, maxSpeed);
		context.add(vehicle);
		NdPoint location = space.getLocation(junc);
		space.moveTo(vehicle, location.getX(), location.getY());
		vehicle.initVehicle(junc);
	}

}
