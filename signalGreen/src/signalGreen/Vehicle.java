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
			
		// move forward
		moveTowards(nextJunctionPt);
		
		// TODO accelerate/decelerate using maxspeed and current speed
		// which means just setting the current speed to its new value
		// and move forward the right amount of space, depending on
		// current speed and max speeed
		
		// update vehicle position
		if (currPosition.equals(nextJunctionPt)) {
			this.origin = nextJunction;

			// DEBUG
			System.out.println("\nOrigin: " + origin.toString());
			System.out.println("Destination: " + destination.toString());
			
			// check if Vehicle has reached destination
			if (this.origin.equals(this.destination)) {
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

		// ..., visionRangeDistance) means how many cells of distance to look for neighbor agents
		GridCellNgh<Vehicle> nghCreator = new GridCellNgh<Vehicle>(grid, currPosition, Vehicle.class, visionRangeDistance, visionRangeDistance);
		List<GridCell<Vehicle>> gridCells = nghCreator.getNeighborhood(false);
		for (GridCell<Vehicle> cell : gridCells) {
			if (cell.size() > 0) {
				// found a vehicle
				tmp = cell.items().iterator().next();
				if (this.origin.equals(tmp.origin)) {
					System.out.println("same origin! Possibly a vehicle ahead...");
				}
			}
		}
		return vehicleAhead;
	}

	
	/**
	 * Moves an agent towards a given point.
	 * Credits: http://repast.sourceforge.net/docs/RepastJavaGettingStarted.pdf
	 * @param pt
	 */
	public void moveTowards(GridPoint pt) 
	{
		// only move if we are not already in this grid location
		if (!pt.equals(grid.getLocation(this))) 
		{
			System.out.println("----");
			// find correct displacement
			int tmp = this.computeDisplacement(pt, false);
			int x = this.computeDisplacement(pt, true);
			// adjust velocity
			// TODO make it adjstVel() method which finds if need to brake or accelerate
			System.out.println("hyp. displacement > real displacement ?? " + tmp + " > " + x);
			// comparing hypothetical displacement against real displ.
			// real displacement takes into account traffic lights, other cars etc. 
			if (tmp > x) {
				System.out.println("Slow down!!");
				this.slowDown();
				// TODO now check if slowing down because of junction or car ahead
				// by finding the closest agent on the way 
				// between the current position and x displacement towards destination point
			}
			else {
				System.out.println("Accelerate!!");
				this.accelerate();
			}
			
			// try see if there are other vehicles on the way
			this.getVehicleAhead(x);
			
			System.out.println("----");
			
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
	 * 3. how far is the next junction<br />
	 * Uses standard kinematics equations for this purpose.
	 * 
	 * @param pt
	 * @param isForRealDisplacement
	 * @return
	 */
	private int computeDisplacement(GridPoint pt, boolean isForRealDisplacement) {		
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
		
		if (!isForRealDisplacement) {
			// this means we just want to know the hypothetical displacement
			return x;
		}
		
		// if with current value of x vehicle will pass junction,
		// just go to the junction
		double dist = grid.getDistance(grid.getLocation(this), pt);
		if (x >  dist) {
			System.out.println("Going too far... " + x + " > " + dist);
			// reset distance to Junction
			// this means the car will actually brake going to the junction
			// which is correct.
			// TODO recompute velocity because car is actually braking?? not sure
			x = (int) dist;
		}
		
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
