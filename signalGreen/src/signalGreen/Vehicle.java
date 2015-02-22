package signalGreen;

import java.util.*;

import repast.simphony.context.Context;
import repast.simphony.engine.schedule.ScheduledMethod;
import repast.simphony.engine.watcher.Watch;
import repast.simphony.engine.watcher.WatcherTriggerSchedule;
import repast.simphony.query.space.grid.GridCell;
import repast.simphony.query.space.grid.GridCellNgh;
import repast.simphony.random.RandomHelper;
import repast.simphony.space.SpatialMath;
import repast.simphony.space.continuous.ContinuousSpace;
import repast.simphony.space.continuous.NdPoint;
import repast.simphony.space.graph.Network;
import repast.simphony.space.graph.RepastEdge;
import repast.simphony.space.graph.ShortestPath;
import repast.simphony.space.grid.Grid;
import repast.simphony.space.grid.GridPoint;
import repast.simphony.util.ContextUtils;
import repast.simphony.util.SimUtilities;

/**
 * Generic class for vehicles of the Traffic Simulator.<br />
 * Cars, ambulances, trucks are subclasses of Vehicle, and have special behaviour such as 
 * cars having reckless or cautious drivers.
 * 
 * @author Yoann
 *
 */
public class Vehicle {
	
	// Repast projections
	private ContinuousSpace<Object> space;
	private Grid<Object> grid;
	private Network<Object> roadNetwork;
	
	private int velocity;
	private int maxVelocity;
	private int acceleration = 3; // m/s
	
	// arbitrary value for time to make simulation faster, it should actually be 1 tick.
	// Used to compute velocity and displacement.
	// TODO adjust it to an optimal value. 
	private final int t = 7;
	// conversion is for now: 1 cell = 50 meters
	private final int convRatioMeters = 50;		
	
	// Simulation is based on Origin Destination pattern.
	// Vehicles have an origin (x, y) starting point
	// and a destination point which is randomly reset to another
	// destination as it reaches it.
	private Junction origin;
	private Junction destination;
	
	// holds the full path from origin to destination
	// each edge of the route is a directed link between Junctions
	private List<RepastEdge<Object>> vehicleRoute;

	/**
	 * Generic Vehicle constructor.
	 * 
	 * @param space
	 * @param grid
	 * @param roadNetwork
	 */
	public Vehicle(ContinuousSpace<Object> space, Grid<Object> grid, Network<Object> roadNetwork,
			int maxVelocity)
	{
		// repast projections
		this.space = space;
		this.grid = grid;
		this.roadNetwork = roadNetwork;
		this.velocity = 0;
		this.maxVelocity = maxVelocity;
	}
	
	/**
	 * Initialises Vehicle: set Origin, find random Destination
	 * and compute best route.
	 */
	public void initVehicle(Junction origin) {
		// set origin and destination of vehicle
		this.origin = origin;
		this.destination = Utils.getRandJunction(roadNetwork); // may return null!
		
		// get best route from origin to destination
		findBestRoute();
	}

	/**
	 * step() is called at each iteration of the 
	 * simulation, starting from iteration 1.
	 * Vehicles behaviour takes place here. 
	 */
	@ScheduledMethod(start = 1, interval = 1)
	public void step() {
		
		// workaround for vehicles stuck in impasse.
		// from now vehicle won't move anymore.
		// TODO change this once vehicles are able to stop in front of other vehicles.
		if (this.vehicleRoute.size() == 0) {
			System.out.println("Vehicle is stuck in impasse. Cannot move... will wait indefinitely.");
			return;
		}			
		
		// get current position of this Vehicle on the grid
		GridPoint currPosition = grid.getLocation(this);
			
		// get location of next Junction along the route
		Junction nextJunction = (Junction) this.vehicleRoute.iterator().next().getTarget();
		GridPoint nextJunctionPt = grid.getLocation(nextJunction);
				
		// compute how many cells we need to move
		int x = this.computeDisplacement();
		// how far is the next junction?
		int y = (int) grid.getDistance(currPosition, nextJunctionPt);
		
		// TODO find vehicles from current position to x position
		// and check their velocity to know if we need to stop, slowdown or accelerate
		Vehicle vehicleAhead = getVehicleAhead(x);
		if (vehicleAhead != null) {
			System.out.println("Found a vehicle ahead...");
			int dist = (int) grid.getDistance(currPosition, grid.getLocation(vehicleAhead));
			System.out.println("Distance to vehicle ahead: " + (x - dist));
			System.out.println("Velocity vehicle ahead: " + vehicleAhead.getVelocity());
			System.out.println("Velocity current vehicle: " + this.getVelocity());
		}
		
		// following algorithm is for moving vehicles along
		// the road network.
		do {			
			
			if (x < y) {
				// we cannot reach the next junction on the road network
				// because it is too far... just move towards it.
				moveTowards(nextJunctionPt, x);
				x = 0;
			}
			else if (x == y) {
				// we can reach the junction but we won't go further.
				moveTowards(nextJunctionPt, x);
				x = 0;
				this.origin = nextJunction;
				// update best route
				findBestRoute();
			}
			else if (x > y) {
				// we are going to move more than 
				// the next junction, so we first go towards it
				// then we keep moving towards the next one.
				moveTowards(nextJunctionPt, y);
				x -= y;
				this.origin = nextJunction;
				findBestRoute();
			}
			
			// workaround to keep cars moving indefinitely
			// if they reached their destination, pick a new random destination
			ifVehicleAtDestination();
			
		} while (x > 0);
		
		this.accelerate();

		
		// TODO
		// get vehicle ahead, if any - in order to take decisions:
		// 1) avoid collisions
		// 2) accelerate/decelerate
		// 3) change lane
		// use: Vehicle vehicleAhead = this.getVehicleAhead();
		// if vehicle ahead is stopped then stop too. Maybe should use the @Watch annotation to watch the speed instead
		// See http://repast.sourceforge.net/docs/RepastReference.pdf		
	}
	
//	scheduleTriggerPriority = 15
//	@Watch (watcheeClassName = "signalGreen.Vehicle",
//			watcheeFieldNames = "velocity",
//			query = " within_moore 1",
//			whenToTrigger = WatcherTriggerSchedule.IMMEDIATE
//			//, triggerCondition = "$watchee.getVelocity() > 100"
//			)
//	void stopVehicleTraffic(Vehicle watchedVehicle) {
//		System.out.println("Hey! Watchee is going at: " + watchedVehicle.getVelocity());
//	}

	
	private void ifVehicleAtDestination() {
		// DEBUG
		System.out.println("\nOrigin: " + origin.toString());
		System.out.println("Destination: " + destination.toString());
		
		// check if Vehicle has reached destination
		while (this.origin.equals(this.destination)) {
			System.out.println("Origin == destination!");
			// choose new random destination
			this.destination = Utils.getRandJunction(roadNetwork);
			
			// DEBUG
			System.out.println("New origin: " + origin.toString());
			System.out.println("New destination: " + destination.toString());				
		}
		
		// update best route
		findBestRoute();		
	}

	/**
	 * Method uses origin and destination Junctions to find the best
	 * path, using SPF algorithm. 
	 */
	private void findBestRoute() {
		this.vehicleRoute = new ShortestPath<Object>(roadNetwork)
				.getPath(this.origin, this.destination); // shoudnt compute this every time...
		if (vehicleRoute.size() == 0) {
			System.out.println("No route found because vehicle is on an impasse..."
					+ "\nRoad network should be loaded from file and made more realistic, ie."
					+ " no impasses should be present.");
			// TODO
			// for now leave simulation throw null pointer exception.
			// later on need to handle this situation, for ex:
			// road network should not have one way impasses, which is true in real life
			// road networks. Thus, we need to model two-lanes roads, or avoid impasses
			// on initial road map.
		}
		else {
			System.out.println("Route found: " + this.vehicleRoute.toString());	
		}		
	}

	/**
	 * getVehicleAhead() returns the closest vehicle in vision range 
	 * ahead of the current vehicle, if any.<br />
	 * Vision range distance is measured as the number of grid cells, which 
	 * should vary depending on current speed of vehicle.
	 * @param visionRangeDistance
	 * @return
	 */
	private Vehicle getVehicleAhead(int visionRangeDistance) {
		// note: this method hasnt been tested, dunno if it works correctly
		Vehicle vehicleAhead = null;
		Vehicle tmp = null;
		GridPoint currPosition = grid.getLocation(this);
		double distance = java.lang.Double.MAX_VALUE;
		double distanceTmp = 0.0;

		// ..., visionRangeDistance) means how many cells of distance to look for neighbor agents
		GridCellNgh<Vehicle> nghCreator = new GridCellNgh<Vehicle>(grid, currPosition, 
				Vehicle.class, visionRangeDistance, visionRangeDistance);
		
		List<GridCell<Vehicle>> gridCells = nghCreator.getNeighborhood(false);
		
		for (GridCell<Vehicle> cell : gridCells) {
			if (cell.size() > 0) {
				// found a vehicle
				tmp = cell.items().iterator().next();
				if (this.origin.equals(tmp.origin) 
						&& this.vehicleRoute.iterator().next().getTarget()
							.equals(tmp.vehicleRoute.iterator().next().getTarget())
							) {
					distanceTmp = grid.getDistance(currPosition, grid.getLocation(tmp));
					// trying to find out the closest vehicle to the current vehicle
					// on the same path.
					if (distanceTmp < distance) {
						vehicleAhead = tmp;
						distance = distanceTmp;
					}
				}
			}
		}
		return vehicleAhead;
	}

	
	/**
	 * Moves an agent towards a given point.
	 * Credits: http://repast.sourceforge.net/docs/RepastJavaGettingStarted.pdf
	 * @param pt the point to move towards
	 * @param x is the number of cells of displacement
	 */
	public void moveTowards(GridPoint pt, int x) 
	{
		// only move if we are not already in this grid location
		if (!pt.equals(grid.getLocation(this))) 
		{			
			// now find the right direction to move to
			NdPoint myPoint = space.getLocation(this);
			NdPoint otherPoint = new NdPoint (pt.getX(), pt.getY());
			double angle = SpatialMath.calcAngleFor2DMovement (space, myPoint, otherPoint);
			space.moveByVector(this, x, angle, 0);
			myPoint = space.getLocation(this);
			grid.moveTo(this, (int) myPoint.getX(), (int) myPoint.getY());
		 }
	}
	
	
	/**
	 * Computes the displacement distance and adjusts
	 * the velocity according to:<br />
	 * 1. current velocity<br />
	 * 2. max velocity<br />
	 * Uses standard kinematics equations for this purpose.
	 * 
	 * @return
	 */
	private int computeDisplacement() {		
		//      Equation to find displacement:
		//      x = v0 * t + 1/2 a * t^2
		//		Where:
		//		x = displacement
		//		v0 = initial velocity
		//		a = acceleration <-- add some constant values, the more acceleration, the more powerful. ex. trucks have smaller accel.
		//		t = time
		int x = (int) Math.ceil(velocity + 0.5 * (double) acceleration * t * t);	
		System.out.println("Displacement is: " + x);

		// conversion from meters to cells in order to get a displacement on the map
		x = x / convRatioMeters;
				
		System.out.println("Will move car by: " + x + " cells");
		return x;
	}

	/**
	 * @return the current velocity
	 */
	public int getVelocity() {
		return this.velocity;
	}

	/**
	 * @param currSpeed the currSpeed to set
	 */
	public void setCurrSpeed(int currSpeed) {
		this.velocity = currSpeed;
	}
	
	/**
	 * Method computes new velocity
	 * according to acceleration and max velocity.<br />
	 * Uses standard kinematics equations for this purpose. 
	 */
	private void accelerate() {
		// new velocity is:
		// V = V0 + a * t
		System.out.println("Old velocity is: " + this.velocity);	
		this.velocity += acceleration * t;
		// vehicle cannot go faster than its maxVelocity
		if (this.velocity > this.maxVelocity) {
			this.velocity = this.maxVelocity;
		}
		System.out.println("New velocity is: " + this.velocity);
	}

	/**
	 * Similar algorithm to the acceleration.
	 * Uses standard kinematics equations for this purpose. 
	 */
	public void slowDown() {
		System.out.println("Old velocity is: " + this.velocity);	
		this.velocity -= acceleration * t;
		// velocity cannot be negative
		if (this.velocity < 0) {
			this.velocity = 0;
		}
		System.out.println("New velocity is: " + this.velocity);
	}	
	
}
