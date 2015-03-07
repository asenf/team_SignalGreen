package signalGreen;

import java.awt.geom.Point2D;
import java.util.*;

import org.geotools.referencing.GeodeticCalculator;

import com.vividsolutions.jts.geom.Coordinate;
import com.vividsolutions.jts.geom.Envelope;
import com.vividsolutions.jts.geom.Geometry;
import com.vividsolutions.jts.geom.GeometryFactory;
import com.vividsolutions.jts.geom.Point;

import repast.simphony.context.Context;
import repast.simphony.engine.environment.RunEnvironment;
import repast.simphony.engine.schedule.ScheduledMethod;
import repast.simphony.engine.watcher.Watch;
import repast.simphony.engine.watcher.WatcherTriggerSchedule;
import repast.simphony.parameter.Parameters;
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
import signalGreen.Constants.Lane;

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
	private Geography geography; // GIS
	private Network<Junction> roadNetwork; // Road network topology
	
	// position of vehicle in the GIS projection
	private Coordinate realPos; // This is the real position for display purpose only
	private Coordinate networkPos; // logical position used to do all computations
	
	// holds mapping between repast edges and actual GIS roads
	private Map<RepastEdge<Junction>, Road> roads;
	
	private int velocity;
	private int maxVelocity;
	private double acceleration = 3; // m/s
	// displacement is used by other vehicles: they can compare it to their displ.
	// and stop, slow down or accelerate accordingly.
	private double displacement;

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
	private Lane lane;
	
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
			Geography geography, Map<RepastEdge<Junction>, Road> roads, int maxVelocity)
	{
		this.ID = UniqueID++;
		// repast projections
		this.roadNetwork = network;
		this.geography = geography;
		this.roads = roads;
		this.velocity = 0;
		this.maxVelocity = maxVelocity;
	}
	
	/**
	 * @return ID the unique ID of this Vehicle
	 */
	public int getID() {
		return ID;
	}

	@Override
	public boolean equals(Object obj) {
        if (!(obj instanceof Vehicle)) {
            return false;
        }
        Vehicle v = (Vehicle) obj;
        return  (v.getID() == this.getID());
	}

	/**
	 * Initialises Vehicle: set Origin, find random Destination
	 * and compute best route.
	 */
	public void initVehicle(Junction origin) {
		// System.out.println("*** initVehicle");
		// set origin and destination of vehicle
		this.origin = origin;
		this.destination = Utils.getRandJunction(roadNetwork); // may return null!
		this.lane = Lane.LEFT; // always start left

		// check if we have't chosen same origin and destination
		// unlikely to happen but...
		ifVehicleAtDestination();
		
		// get best route from origin to destination
		findBestRoute();
		
		// set the next junction, so vehicle knows what is the next step on the route
		this.next = this.getNextJunctionRoute();
		
		// register vehicle to next junction
		this.next.enqueueVehicle(this.origin, this);
		
		// set positions of vehicle
		this.networkPos = origin.getCoords();
		this.realPos = this.getRealPosFromNetworkPos();
		moveTo(realPos);
		// this.moveTowards(realPos, Constants.DIST_LANE);
//		System.out.println("\n\nMyPos: " + geography.getGeometry(this).getCoordinate());
//		Utils.debugCoordinate(networkPos);
//		Utils.debugCoordinate(this.realPos);
	}

	/**
	 * step() is called at each iteration of the 
	 * simulation, starting from iteration 1.
	 * Vehicle behavior takes place here. 
	 */
	@ScheduledMethod(start = 1, interval = 1)
	public void step() {
		// System.out.println("---------\n*** Step");
		
		Vehicle v; // vehicle ahead
		
		// following happens only when network topology contains more than one graph.
		// It is the case when a vehicle tries to reach a destination on the other graph.
		if (this.vehicleRoute.size() == 0) {
			System.out.println("Vehicle is stuck in impasse. Cannot move...");
		}			

		// System.out.println("Traveling on: " + getNextRoadSegmentRoute().getName());
		
		// get current position of this Vehicle on the geography
//		Coordinate currPositionCoord = geography.getGeometry(this).getCoordinate();
//		Coordinate currPositionCoord = getNetworkPos();
			
		// get location of next Junction along the route
		this.next = this.getNextJunctionRoute();
//		Coordinate nextJunctionCoord = nextJunction.getCoords();	
						
		// compute how many meters we would like to move
		double tmpDisplacement = this.computeDisplacement(); // just need to adjust optimal acceleration parameters
		
		// now check if we can actually make it all the way, ie. we need to 
		// adjust velocity and displacement
		
		// see if there is a vehicle ahead within vision range
		v = getVehicleAhead(tmpDisplacement);		
		
		// check vehicle ahead's displacement to know what to do
		if (v != null) {
			// distance between current vehicle and leader
			double vDistance = Utils.distance(getNetworkPos(), v.getNetworkPos(), geography);
			
			// check if we need to stop: vehicle ahead is stopped
			if (v.getDisplacement() == 0) {
				this.setVelocity(0);
				this.displacement = 0;
				return; // no need to perform the displacement
			}
			
			// adjust to optimal velocity/displacement			
			while ((v.getDisplacement() + vDistance) < (tmpDisplacement + Constants.DIST_VEHICLES)) {
				this.slowDown();	
				tmpDisplacement = this.computeDisplacement();
				
				// manage limit cases
				if (this.velocity == 0) {
					this.displacement = 0;
					return;
				}
			}
		}
		else {
			// no vehicles, accelerate if we are allowed to
			this.accelerate();
		}
		
		// optimal displacement found!
		this.displacement = tmpDisplacement;
		
		// how far is the next junction?
		double juncDist = Utils.distance(getNetworkPos(), next.getCoords(), geography);
		
		// if there is a red traffic light we adjust the distance to it
		// so that we stop before the jam
		if (next instanceof TrafficLight) {
			juncDist = juncDist - Constants.DIST_LANE;
		}
		
		// following algorithm is for moving vehicles along
		// the road network towards the next Junction.
		do {	
			
			if (tmpDisplacement < juncDist) {
				// we cannot reach the next junction on the road network
				// because it is too far... just move towards it.
				moveTowards(next.getCoords(), tmpDisplacement);
				tmpDisplacement = 0;
			}
			else if (tmpDisplacement == juncDist) {
				// we can reach the junction but we won't go further.
				moveTowards(next.getCoords(), tmpDisplacement);
				tmpDisplacement = 0;
				// update current position of vehicle along the route
				removeCurrentRoadSegmentFromRoute();
			}
			else if (tmpDisplacement > juncDist) {
				// we are going to move more than
				// the next junction				
				
				// 1. Stop according to traffic policies
				if (next instanceof TrafficLight) {
					Light light = ((TrafficLight) next).getLights().get(origin);
					if ((light.getSignal() == Constants.Signal.RED)) {
						// easy case :)
						this.setVelocity(0);
						return;
					}
				}				

				// *** stop sign management here.
				
				// 2. road is clear: go towards nex junction
				// then we keep moving towards the next one.
				moveTowards(next.getCoords(), juncDist);
				tmpDisplacement = tmpDisplacement - juncDist;
				// this.next.printVehiclesQueue(this.origin);
				removeCurrentRoadSegmentFromRoute();
				// this.next.printVehiclesQueue(this.origin);
				// recompute distance towards updated next junction		
				juncDist = Utils.distance(getNetworkPos(), next.getCoords(), geography);
			}
			
			// DEBUG
			// debugRoute();
			
			// This makes vehicles moving indefinitely:
			// if they reached their destination, pick a new random destination
			ifVehicleAtDestination();
			
		} while (tmpDisplacement > 0); // keep iterating until the whole x distance has been covered
		
		// check vehicle ahead's velocity to slowDown() or accelerate()
//		this.accelerate();
		
		// TODO
		// get vehicle ahead, if any - in order to take decisions:
		// 1) avoid collisions
		// 2) accelerate/decelerate
		// 3) change lane
		// use: Vehicle vehicleAhead = this.getVehicleAhead();
		// if vehicle ahead is stopped then stop too. Maybe should use the @Watch annotation to watch the speed instead
		// See http://repast.sourceforge.net/docs/RepastReference.pdf		
	}

	/**
	 * Removes the last road segment that a Vehicle has just traveled
	 * and updates the current route.
	 */
	private void removeCurrentRoadSegmentFromRoute() {
		// System.out.println("*** removeCurrentRoadSegmentFromRoute");
		// current next junction (soon the origin) thinks we are 
		// on his road segment. Need to dequeue vehicle from vehicle list.
		this.next.dequeueVehicle(origin, this);
		
		if (this.vehicleRoute.size() <= 1) { // origin == destination?
			// reset vehicle route
			initVehicle(this.next);
		}
		else {			
			// Move to next road segment
			this.origin = this.next;
			// remove current road segment from current route
			this.vehicleRoute.remove(0);
			this.next = this.getNextJunctionRoute();
			// tell next junction this vehicle is on his way
			this.next.enqueueVehicle(this.origin, this);
			// update position
			this.networkPos = origin.getCoords();
			this.realPos = this.getRealPosFromNetworkPos();
			// this.moveTowards(realPos, Constants.DIST_LANE);
			moveTo(realPos);
		}
		// DEBUG
		// next.printVehiclesQueue(origin);
	}

	/**
	 * Picks a new random destination if the vehicle has reach his current dest.
	 */
	private void ifVehicleAtDestination() {
		boolean isAtDestination = false;
		// check if Vehicle has reached destination
		while (this.origin.equals(this.destination)) {
			isAtDestination = true;
			// System.out.println("Origin == destination!");
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
			// System.out.println("New Route Computed:");
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
	 * 
	 * @see signalGreen.Vehicle#computeDisplacement()
	 * @param x the vision range distance in meters
	 * @return next vehicle in the same road segment
	 * 
	 */
	private Vehicle getVehicleAhead(double x) {
		Vehicle v = this.getNextVehicle(); // the vehicle ahead
		if (v == null) {
			// no vehicles ahead
			return null;
		}
		// check if next vehicle is in vision range		
		Coordinate c1 = this.getNetworkPos(); // current position
		Coordinate c2 = v.getNetworkPos(); // current position
		
		double dist = Utils.distance(c1, c2, geography);
		if (dist < x) {
			return v;
		}
		// vehicle is too far, assume there are no vehicles ahead
		return null;
	}
	
	/**
	 * @return vehicle next in the same road segment
	 */
	private Vehicle getNextVehicle() {
		boolean isNext = false;
		Vehicle v = null;
		Vehicle tmp = null;
		Queue q = this.next.getVehiclesQueue(this.origin);
		for(Object o : q) {
			tmp = (Vehicle) o;
		    if (isNext == true) {
		    	v = tmp;
		    	break;
		    }
		    if (tmp.equals(this)) {
		    	isNext = true;		    	
		    }
		}
		return v;
	}
	
	/**
	 * Returns the real position on a vehicle on the GIS projection
	 * 
	 * @return coordinate 
	 */
	public Coordinate getRealPos() {
		return realPos;
	}

	public void setRealPos(Coordinate realPos) {
		this.realPos = realPos;
	}

	/**
	 * Returns the logical position of a vehicle on the
	 * road network topology, using real geodetic distances
	 *  
	 * @return coordinate
	 */
	public Coordinate getNetworkPos() {
		return networkPos;
	}

	public void setNetworkPos(Coordinate networkPos) {
		this.networkPos = networkPos;
	}

	/**
	 * Moves a vehicle towards a give Coordinate.
	 * Uses network position and then moves on the 
	 * real GIS projection.
	 * 
	 * @param c the Coordinate to move towards
	 * @param x the displacement in meters
	 */
	public void moveTowards(Coordinate c, double x) 
	{
		
		double angle = Utils.getAngle(this.networkPos, c, geography);	
		geography.moveByVector(this, x, angle); // move agent
		
		// update positions
		realPos = geography.getGeometry(this).getCoordinate();
		this.networkPos = this.getNetworkPosFromRealPos(); // must be here BUG **
	}
	
	
	/**
	 * Moves a vehicle to a give Coordinate.
	 * 
	 * @param coordinate
	 */
	private void moveTo(Coordinate c) {
		GeometryFactory geomFac = new GeometryFactory();
		Point p = geomFac.createPoint(realPos);
		geography.move(this, p);		
	}
	
	
	/**
	 * Computes the displacement distance and adjusts
	 * the velocity according to:<br />
	 * 1. current velocity<br />
	 * 2. max velocity<br />
	 * Uses standard kinematics equations for this purpose.
	 * 
	 * @return x the displacement in meters
	 */
	private double computeDisplacement() {		
		//      Equation to find displacement:
		//      x = v0 * t + 1/2 a * t^2
		//		Where:
		//		x = displacement
		//		v0 = initial velocity
		//		a = acceleration <-- add some constant values, the more acceleration, the more powerful. ex. trucks have smaller accel.
		//		t = time
		double x = Math.ceil(velocity + 0.5 * acceleration * t * t);	
		// System.out.println("Displacement is: " + x);

		// conversion from meters to cells in order to get a displacement on the map
		x = x / convRatioMeters;
				
		// System.out.println("Will move car by: " + x + " meters.");
		return x;
	}

	/**
	 * Returns two coordinates that are perpendicular (+-90 degrees)
	 * to a logical or real position of the current vehicle.
	 * 
	 * @param coordinate either real or network position of vehicle
	 * @return array of coordinates
	 */
	private Coordinate[] getPosition(Coordinate c) {
		double azimuth = Utils.getAzimuth(origin.getCoords(), next.getCoords(), geography);
		Coordinate position[] = Utils.createCoordsFromCoordAndAngle(c, azimuth, Constants.DIST_LANE, geography);
		return position;
	}
	
	public Coordinate getRealPosFromNetworkPos() {
		Coordinate position[] = this.getPosition(this.networkPos);
		return position[0];
	}
	
	public Coordinate getNetworkPosFromRealPos() {
		Coordinate position[] = this.getPosition(this.realPos);
		return position[1];
	}

	/**
	 * @return current velocity
	 */
	public int getVelocity() {
		return this.velocity;
	}

	/**
	 * @param currSpeed the currSpeed to set
	 */
	public void setVelocity(int currSpeed) {
		this.velocity = currSpeed;
	}
	
	
	public double getDisplacement() {
		return displacement;
	}
	
	/**
	 * Method computes new velocity
	 * according to acceleration and max velocity.<br />
	 * Uses standard kinematics equations for this purpose. 
	 */
	private void accelerate() {
		// new velocity is:
		// V = V0 + a * t
		// System.out.println("Accelerate: Old velocity is: " + this.velocity);	
		this.velocity += Math.ceil(acceleration * t);
		// vehicle cannot go faster than its maxVelocity
		if (this.velocity > this.maxVelocity) {
			this.velocity = this.maxVelocity;
		}
		// System.out.println("Accelerate: New velocity is: " + this.velocity);
	}

	/**
	 * Similar algorithm to the acceleration.
	 * Uses standard kinematics equations for this purpose. 
	 */
	public void slowDown() {
		// System.out.println("Slow down: Old velocity is: " + this.velocity);	
		this.velocity -= acceleration * t * 2;
		// velocity cannot be negative
		if (this.velocity < 0) {
			this.velocity = 0;
		}
		// System.out.println("Slow down: New velocity is: " + this.velocity);
	}	
	
	/**
	 * 
	 * @return the next junction on the route we are heading to
	 */
	private Junction getNextJunctionRoute() {
		Junction jNext = null;
		Iterator<RepastEdge<Junction>> it = this.vehicleRoute.iterator();
		if (it.hasNext()) {
			jNext = it.next().getTarget();
		}
		return jNext;
	}	
	
	/**
	 * 
	 * @return RepastEdge ie. the current road segment of the vehicle
	 */
	public RepastEdge<Junction> getNextRepastEdgeRoute() {
		RepastEdge<Junction> e = null;
		Iterator<RepastEdge<Junction>> it = this.vehicleRoute.iterator();
		if (it.hasNext()) {
			e = it.next();
		}
		return e;
	}
	
	/**
	 * @return Road the current road segment
	 */
	public Road getNextRoadSegmentRoute() {
		Road r = null;
		RepastEdge<Junction> e = getNextRepastEdgeRoute();
//		System.out.println("Edge: " + e.toString() + " roads size: " + this.roads.size());
		if (e != null) {
			r = this.roads.get(e);
		}
		return r;
	}

	public Lane getLane() {
		return lane;
	}

	public void setLane(Lane lane) {
		this.lane = lane;
	}
	
}
