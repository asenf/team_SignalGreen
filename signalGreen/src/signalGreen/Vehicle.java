package signalGreen;

import java.util.*;

import com.vividsolutions.jts.geom.Coordinate;
import com.vividsolutions.jts.geom.Envelope;
import com.vividsolutions.jts.geom.Geometry;

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
import repast.simphony.space.gis.Geography;
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
	
	public static int UniqueID = 0;
	private int ID;
	
	// Repast projections
	private Geography geography;
	private Network<Junction> roadNetwork;
	
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
	private Junction next;
	private Junction destination;
	
	// holds the full path from origin to destination
	// each edge of the route is a directed link between Junctions
	private List<RepastEdge<Junction>> vehicleRoute;

	/**
	 * Generic Vehicle constructor.
	 * 
	 * @param space
	 * @param grid
	 * @param roadNetwork
	 */
	public Vehicle(Network<Junction> network, 
			Geography geography, int maxVelocity)
	{
		this.ID = UniqueID++;
		// repast projections
		this.roadNetwork = network;
		this.geography = geography;
		this.velocity = 0;
		this.maxVelocity = maxVelocity;
	}
	
	/**
	 * @return ID the unique ID of this Vehicle
	 */
	public int getID() {
		return ID;
	}

	/**
	 * Initialises Vehicle: set Origin, find random Destination
	 * and compute best route.
	 */
	public void initVehicle(Junction origin) {
		// set origin and destination of vehicle
		this.origin = origin;
		this.destination = Utils.getRandJunction(roadNetwork); // may return null!

		// check if we have't chosen same origin and destination
		// unlikely to happen but...
		ifVehicleAtDestination();
		
		// get best route from origin to destination
		findBestRoute();
		
		// set the next junction, so vehicle knows what is the next step on the route
		this.next = this.getNextJunctionRoute();
	}

	/**
	 * step() is called at each iteration of the 
	 * simulation, starting from iteration 1.
	 * Vehicles behaviour takes place here. 
	 */
	@ScheduledMethod(start = 1, interval = 1)
	public void step() {
		
		// workaround for vehicles stuck in impasse.
		if (this.vehicleRoute.size() == 0) {
			System.out.println("Vehicle is stuck in impasse. Cannot move...");
			initVehicle(origin);
		}			
		
		// get current position of this Vehicle on the geography
		Coordinate currPositionCoord = geography.getGeometry(this).getCoordinate();
			
		// get location of next Junction along the route
		Junction nextJunction = this.next = this.getNextJunctionRoute();
		Coordinate nextJunctionCoord = nextJunction.getCoords();
		

		// DEBUG
//		Iterator<RepastEdge<Junction>> it = this.roadNetwork.getOutEdges(origin).iterator();
//		while (it.hasNext()) {
//			RepastEdge<Junction> e = it.next();
//			System.out.println("Out Edge: " + e.getSource().toString() + " -> " + e.getTarget().toString());
//		}		
						
		// compute how many meters we would like to move
		double x = this.computeDisplacement(); // just need to adjust optimal acceleration parameters
		
		// how far is the next junction?
		double y = Utils.distance(currPositionCoord, nextJunctionCoord, geography);
		
		// TODO
//		if (x > y) {
//			// junction is very close: we need to check the following:
//			if (next instanceof TrafficLight) {
//				// **note: TrafficLights should have only one inEdge at
//				// a time with the green light. All others should be red.
//				// Possible implementation: Junctions already have a list of roads, which
//				// in turns have inEdges. it would be sufficient to iterate through them every 
//				// N ticks and perform the switchLight() algorithm.
//				if (next.getLight() == Constants.RED) {
//					// easy case :)
//					this.stopAndWait();
//				}
//			}
//			
//			// this can be implemented if we have time
//			else if (next instanceof StopSign) {
//				// ask the StopSign if there are other Vehicles approaching the stop:
//				// it needs to tell us who is the closest.
//				// Possible implementation: Vehicles tell the this.next junctions
//				// they are approaching by having a reference in a queue.
//				// ex.
//				if (next.approachingVehicles.getNext().getID() != this.getID) {
//					this.stopAndWait();
//				}
//				else {
//					// remove current vehicle from queue because 
//					// we are going through the junction
//					next.ApproachingVehicles.dequeue();
//				}
//			}
//			// if neither case is true then Junction is just a connection
//			// between two consecutive roads and we do nothing.
//		}
		
		// TODO find vehicles from current position to x position
		// and check their velocity to know if we need to stop, slowdown or accelerate
		Vehicle vehicleAhead = getVehicleAhead(x);
		if (vehicleAhead != null) {
			Coordinate vac = geography.getGeometry(vehicleAhead).getCoordinate();
			System.out.println("Found a vehicle ahead...");
			double dist = Utils.distance(currPositionCoord, vac, geography);
			System.out.println("Distance to vehicle ahead: " + (x - dist));
			System.out.println("Velocity vehicle ahead: " + vehicleAhead.getVelocity());
			System.out.println("Velocity current vehicle: " + this.getVelocity());
		}
		
		// following algorithm is for moving vehicles along
		// the road network.
		do {	
			// DEBUG
//			System.out.println("Displacement x: " + x + "Displacement to junction y: " + y);
			
			if (x < y) {
				// we cannot reach the next junction on the road network
				// because it is too far... just move towards it.
				moveTowards(nextJunctionCoord, x);
				x = 0;
			}
			else if (x == y) {
				// we can reach the junction but we won't go further.
				moveTowards(nextJunctionCoord, x);
				x = 0;
				// update current position of vehicle along the route
				removeCurrentRoadSegmentFromRoute();
			}
			else if (x > y) {
				// we are going to move more than 
				// the next junction, so we first go towards it
				// then we keep moving towards the next one.
				moveTowards(nextJunctionCoord, y);
				x = x - y;
				removeCurrentRoadSegmentFromRoute();
				nextJunctionCoord = next.getCoords();
				// recompute distances
				currPositionCoord = geography.getGeometry(this).getCoordinate();
				y = Utils.distance(currPositionCoord, nextJunctionCoord, geography);
			}
			
			// DEBUG
			// debugRoute();
			
			// This makes vehicles moving indefinitely:
			// if they reached their destination, pick a new random destination
			ifVehicleAtDestination();
			
		} while (x > 0); // keep iterating until the whole x distance has been covered
		
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
	
	private void removeCurrentRoadSegmentFromRoute() {
		if (this.vehicleRoute.size() <= 1) { // origin == destination?
			// System.out.println("Reinitialise route.");
			initVehicle(this.next);
		}
		else {
			this.origin = this.next;
			// remove current road segment from current route
			this.vehicleRoute.remove(0);
			this.next = this.getNextJunctionRoute();
		}
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
		boolean isAtDestination = false;
		// check if Vehicle has reached destination
		while (this.origin.equals(this.destination)) {
			isAtDestination = true;
			System.out.println("Origin == destination!");
			// choose new random destination
			this.destination = Utils.getRandJunction(roadNetwork);
		}
		// update best route
		if (isAtDestination == true) {
			findBestRoute();
		}
	}

	/**
	 * Method uses origin and destination Junctions to find the best
	 * path, using SPF algorithm. 
	 */
	private synchronized void findBestRoute() {
		ShortestPath<Junction> p = new ShortestPath<Junction>(roadNetwork);
		p.finalize();
		this.vehicleRoute = p.getPath(this.origin, this.destination);
		
		if (vehicleRoute.size() == 0) {
			System.out.println("No route found because vehicle is on an impasse..."
					+ "\nMake sure Road network has no impasses, ie. have always "
					+ "two-way roads.");
		}
		else {
			this.next = this.getNextJunctionRoute();
			System.out.println("New Route Computed:");
			// debugRoute();
		}		
	}
	
	/**
	 * Prints out Vehicle route data.
	 */
	private void debugRoute() {
		System.out.println("\n***"
				+ "\nVehicle ID: " + this.getID()
				+ "\nOrigin: " + this.origin
				+ "\nNext Junction: " + this.next
				+ "\nDestination" + this.destination
				+ "\nCurrent route:\n");	
		Iterator<RepastEdge<Junction>> it = vehicleRoute.iterator();
		while (it.hasNext()) {
			RepastEdge<Junction> e = it.next();
			System.out.println("\tOut Edge: " + e.getSource().toString() 
					+ " -> " + e.getTarget().toString());
		}
		System.out.println("\n***\n");	
	}

	/**
	 * getVehicleAhead() returns the closest vehicle in vision range 
	 * ahead of the current vehicle, if any.<br />
	 * Vision range distance is measured in meters, which 
	 * should vary depending on current speed of vehicle.
	 * Usually this is the Vehicle displacement.
	 * @see this.computeDosplacement()
	 * @param x the vision range distance in meters
	 * @return
	 */
	private Vehicle getVehicleAhead(double x) {
		// note: this method hasnt been tested, dunno if it works correctly
		Vehicle v = null;
		Object obj = null;
		Vehicle tmp = null;
		Coordinate c = geography.getGeometry(this).getCoordinate(); // current position
		double d = java.lang.Double.MAX_VALUE;
		double dTmp = 0.0;

		// ..., visionRangeDistance) means how many cells of distance to look for neighbor agents
//		GridCellNgh<Vehicle> nghCreator = new GridCellNgh<Vehicle>(grid, currPosition, 
//				Vehicle.class, visionRangeDistance, visionRangeDistance);
		
		// search in a square with: edge = 2x, centroid = c 
		Envelope e = new Envelope(c.x + x, c.x - x, c.y + x, c.y - x);
		Iterator<Object> it = geography.getObjectsWithin(e).iterator();
		
		while (it.hasNext()) {
			obj = it.next();
			if (obj instanceof Vehicle) {
				tmp = (Vehicle) obj;
				// try to find out the closest vehicle to the current vehicle
				// on the current Vehicle path, ie. two Vehicles are on the same Road segment
				// which means the have same origin and next junction.
				if ((origin.equals(tmp.origin)) 
						&& (this.getNextJunctionRoute().equals(tmp.getNextJunctionRoute()))) 
				{
					dTmp =Utils.distance(c, geography.getGeometry(tmp).getCoordinate(), geography);
					if (dTmp < d) {
						// this vehicle is closest so far
						v = tmp;
						d = dTmp;
					}
				}	
				tmp = null;
				obj = null;
			}			
		}
		return v;
		
//		List<GridCell<Vehicle>> gridCells = nghCreator.getNeighborhood(false);
		
//		for (GridCell<Vehicle> cell : gridCells) {
//			if (cell.size() > 0) {
//				// found a vehicle
//				tmp = cell.items().iterator().next();
//				if (this.origin.equals(tmp.origin) 
//						&& this.vehicleRoute.iterator().next().getTarget()
//							.equals(tmp.vehicleRoute.iterator().next().getTarget())
//							) {
//					distanceTmp = grid.getDistance(currPosition, grid.getLocation(tmp));
//					// trying to find out the closest vehicle to the current vehicle
//					// on the same path.
//					if (distanceTmp < distance) {
//						vehicleAhead = tmp;
//						distance = distanceTmp;
//					}
//				}
//			}
//		}
//		return vehicleAhead;
	}

	
	/**
	 * Moves an agent towards a given Coordinate.
	 * @param c the Coordinate to move towards
	 * @param x the displacement in meters
	 */
	public void moveTowards(Coordinate c, double x) 
	{
		Coordinate currPos = geography.getGeometry(this).getCoordinate();
		// only move if we are not already there
		if (!currPos.equals(c)) 
		{			
			// now find the right direction to move to
			double angle = Utils.getAngle(currPos, c, geography);
			geography.moveByVector(this, x, angle);
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
	private double computeDisplacement() {		
		//      Equation to find displacement:
		//      x = v0 * t + 1/2 a * t^2
		//		Where:
		//		x = displacement
		//		v0 = initial velocity
		//		a = acceleration <-- add some constant values, the more acceleration, the more powerful. ex. trucks have smaller accel.
		//		t = time
		double x = Math.ceil(velocity + 0.5 * (double) acceleration * t * t);	
		System.out.println("Displacement is: " + x);

		// conversion from meters to cells in order to get a displacement on the map
		x = x / convRatioMeters;
				
		// System.out.println("Will move car by: " + x + " meters.");
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
	
	
	/**
	 * 
	 * @return the next junction on the route we are heading to.
	 */
	private Junction getNextJunctionRoute() {
		Junction jNext = null;
		Iterator<RepastEdge<Junction>> it = this.vehicleRoute.iterator();
		if (it.hasNext()) {
			jNext = it.next().getTarget();
		}
		return jNext;
	}
	
}
