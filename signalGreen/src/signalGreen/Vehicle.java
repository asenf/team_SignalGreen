package signalGreen;

import java.util.*;

import com.vividsolutions.jts.geom.Coordinate;
import com.vividsolutions.jts.geom.GeometryFactory;
import com.vividsolutions.jts.geom.Point;

import repast.simphony.engine.schedule.ScheduledMethod;
import repast.simphony.space.gis.Geography;
import repast.simphony.space.graph.Network;
import repast.simphony.space.graph.RepastEdge;
import repast.simphony.space.graph.ShortestPath;
import signalGreen.Constants.*;

/**
 * Generic class for vehicles of the Traffic Simulator.<br />
 * Cars, ambulances, trucks are subclasses of Vehicle, and have special behaviour such as 
 * cars having reckless or cautious drivers.
 * 
 * @author Yoann
 *
 */
public class Vehicle extends GisAgent implements Comparable<Vehicle> {
	
	// position of vehicle in the GIS projection
	private Coordinate realPos; // This is the real position for display purpose only
	private Coordinate networkPos; // logical position used to do all computations
	
	// holds mapping between repast edges and actual GIS roads
	private Map<RepastEdge<Junction>, Road> roads;
	
	private int velocity;
	private int maxVelocity;
	// displacement is used by other vehicles: they can compare it to their displ.
	// and stop, slow down or accelerate accordingly.
	private double displacement;		
		
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
	
	private double angle;
	
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
		super(network, geography);
		this.roads = roads;
		this.velocity = 0;
		this.maxVelocity = maxVelocity;
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
		// System.out.println("*** initVehicle: " + this.toString());
		// set origin and destination of vehicle
		this.origin = origin;
		this.destination = Utils.getRandJunction(getNetwork()); // may return null!
		this.lane = Lane.OUTER; // always start outer

		// check if we have't chosen same origin and destination
		// unlikely to happen but...
		ifVehicleAtDestination();
		
		// get best route from origin to destination
		findBestRoute();
		
		// set the next junction, so vehicle knows what is the next step on the route
		this.next = this.getNextJunctionRoute();
		
		// set positions of vehicle
		this.networkPos = origin.getCoords();
		this.realPos = this.getRealPosFromNetworkPos(this.lane);
		moveTo(realPos);
		
		// register vehicle to next junction
		this.next.enqueueVehicle(this.origin, this);
		this.angle = getAngle();
	}

	/**
	 * step() is called at each iteration of the 
	 * simulation, starting from iteration 1.
	 * Vehicle behavior takes place here. 
	 */
	@ScheduledMethod(start = 1, interval = 1)
	public void step() {		
		Vehicle v; // vehicle ahead
		double tmpDisplacement;
		
		// following happens only when network topology contains more than one graph.
		// It is the case when a vehicle tries to reach a destination on the other graph.
		if (this.vehicleRoute.size() == 0) {
			System.out.println("Vehicle is stuck in impasse. Cannot move...");
		}			
			
		// get location of next Junction along the route
		this.next = this.getNextJunctionRoute();	
						
		// compute how many meters we would like to move
		tmpDisplacement = this.computeDisplacement();
		
		// selects the lane this vehicle wants to go to
		v = laneSelection(tmpDisplacement);

		// find optimal displacement checking the gap between vehicles
		this.displacement = gapAcceptance(v, tmpDisplacement);
		if (this.displacement == 0) {
			return; // no need to perform displacement
		}
		
		// move the vehicle on the GIS geography on the selected
		// lane using optimal displacement
		executeDisplacement(this.displacement);
	}
	
	private Vehicle laneSelection(double tmpDisplacement) {
		Vehicle v = null;
		Lane targetLane = this.lane;
		// see if there is a vehicle ahead within vision range
		Vehicle[] veh = getVehiclesAhead(tmpDisplacement + Constants.DIST_VEHICLES);
		v = null; // reset vehicle ahead
		
		// Vehicle now decides if he wants to change lane or not.
		// case: we are on the outer lane
		if (this.lane == Lane.OUTER) {
			// if there is a vehicle ahead on the OUTER but none on the INNER
			// we can overtake safely, but only if outer's lane vehicle's speed < ours
			// and their speed != 0 --> causes horrible overtaking at traffic lights...
			if ((veh[0] != null)  
					&& (this.getVelocity() > veh[0].getVelocity())
					&& (veh[0].getVelocity() != 0)) {
				if (veh[1] == null) {
					// move to inner lane
					targetLane = Lane.INNER;					
					v = veh[1]; // set next vehicle to the one on the INNER lane
				}				
			}
			else {
				// we stay on the OUTER lane
				targetLane = Lane.OUTER;
				v = veh[0];
			}

		}
		// case: we are on the INNER so if there are no cars on the 
		// OUTER lane we might want to go back to the OUTER lane
		// if it is clear
		else {
			// check if the outer lane is clear, meaning
			// no vehicles behind are approaching
			Vehicle vBehind = getVehiclesBehind(tmpDisplacement + Constants.DIST_VEHICLES)[0];
			if ((veh[0] == null) && (vBehind == null)) {
				targetLane = Lane.OUTER;
				v = veh[0];
			}
			else {
				targetLane = Lane.INNER;
				v = veh[1];
			}
		}		
		this.lane = targetLane;
		return v;
	}

	private double gapAcceptance(Vehicle v, double tmpDisplacement) {
		// check vehicle ahead's displacement to know how to 
		// adjust velocity and displacement
		if (v != null) {
			// distance between current vehicle and leader
			// double vDistance = Utils.distance(getNetworkPos(), v.getNetworkPos(), getGeography());
			
			// check if we need to stop: vehicle ahead is close enough and stopped
			if ((v.getDisplacement() == 0) || v.getVelocity() == 0) {
				this.velocity = 0;
				return 0; // no need to perform the displacement
			}
			
			// adjust to optimal velocity/displacement			
			while ((tmpDisplacement + Constants.DIST_VEHICLES) >= (v.getDisplacement() /* + vDistance */)) {
				
				this.slowDown();
				tmpDisplacement = this.computeDisplacement();
				
				// manage limit cases
				if (this.velocity == 0) {
					return 0;
				}
			}
		}
		else {
			// no vehicles, accelerate if we are allowed to
			this.accelerate();
		}
		return tmpDisplacement;
	}

	private void executeDisplacement(double tmpDisplacement) {
		boolean mustStopVehicle = false;
		// how far is the next junction?
		double juncDist = Utils.distance(getNetworkPos(), next.getCoords(), getGeography());
				
		// if there is a red traffic light we adjust the distance to it
		// so that we stop before the jam
		if (next instanceof TrafficLight) {
			juncDist = juncDist - Constants.DIST_LIGHTS;
		}		
		
		// now update position of vehicle on GIS display:
		// might have changed because of change lane algorithm
		this.realPos = this.getRealPosFromNetworkPos(lane);
		this.moveTo(realPos);

		
		// following algorithm is for moving vehicles along
		// the road network towards the next Junction.
		do {	
			// we cannot reach the next junction on the road network
			// because it is too far... just move towards it.
			if (tmpDisplacement < juncDist) {
				moveTowards(next.getCoords(), tmpDisplacement);
				tmpDisplacement = 0;
			}
			// we are going to move more than
			// the next junction
			else if (tmpDisplacement >= juncDist) {
				// check traffic policies
				mustStopVehicle = evaluateTrafficManagementPolicies();
				if (mustStopVehicle == true) {
					return;
				}
				
				// road is clear: move to next junction
				// then we keep moving towards the next one.
				moveTowards(next.getCoords(), juncDist);
				tmpDisplacement = tmpDisplacement - juncDist;
				displacement = tmpDisplacement;
				removeCurrentRoadSegmentFromRoute();
				// recompute distance towards updated next junction		
				juncDist = Utils.distance(getNetworkPos(), next.getCoords(), getGeography());
			}
			
			// DEBUG
			// debugRoute();
			
			// This makes vehicles moving indefinitely:
			// if they reached their destination, pick a new random destination
			ifVehicleAtDestination();
			
		} while (tmpDisplacement > 0); // keep iterating until the whole displacement has been covered
		
		// update queue held by junction to know order of vehicles on current road segment
		next.reorderVehicle(origin, this);
		// update graphic's angle to match with road's horizontal axis
		this.angle = Utils.getAngleDeg(origin.getCoords(), next.getCoords(), getGeography());		
	}

	private boolean evaluateTrafficManagementPolicies() {
		// 1. Stop according to traffic policies
		if (next instanceof TrafficLight) {
			Light light = ((TrafficLight) next).getLights().get(origin);
			if ((light.getSignal() == Constants.Signal.RED)) {
				// easy case :)
				this.setVelocity(0);
				this.displacement = 0;
				return true; // red t. light, must stop
			}
		}				

		// *** stop sign management here.
		
		return false; // vehicle can keep moving
	}

	/**
	 * Removes the last road segment that a Vehicle has just traveled
	 * and updates the current route.
	 */
	private void removeCurrentRoadSegmentFromRoute() {
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
			this.realPos = this.getRealPosFromNetworkPos(this.lane);
			moveTo(realPos);
		}
	}

	/**
	 * Picks a new random destination if the vehicle has reach his current dest.
	 */
	private void ifVehicleAtDestination() {
		boolean isAtDestination = false;
		// check if Vehicle has reached destination
		while (this.origin.equals(this.destination)) {
			isAtDestination = true;
			// choose new random destination
			this.destination = Utils.getRandJunction(getNetwork());
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
		ShortestPath<Junction> p = new ShortestPath<Junction>(getNetwork());
		p.finalize();
		this.vehicleRoute = p.getPath(this.origin, this.destination);
		
		if (vehicleRoute.size() == 0) {
			System.out.println("No route found because vehicle is on an impasse..."
					+ "\nMake sure Road network has no impasses, ie. have always "
					+ "two-way roads.");
		}
		else {
			this.next = this.getNextJunctionRoute();
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
	 * Method finds the closest vehicle in vision range 
	 * ahead of the current vehicle, if any.<br />
	 * Vision range distance is measured in meters, which 
	 * should vary depending on current speed of vehicle.
	 * Usually this is the Vehicle displacement.<br />
	 * Returns an array with size of 2 of vehicles ahead as follows:<br />
	 * <code>v[0]</code> => Vehicle on <code>Lane.OUTER</code><br />
	 * <code>v[1]</code> => Vehicle on <code>Lane.INNER</code>
	 * 
	 * @see signalGreen.Vehicle#computeDisplacement()
	 * @param x the vision range distance in meters
	 * @return array of vehicles
	 */
	private Vehicle[] getVehiclesAhead(double x) {
		
		Vehicle v[] = next.getNextVehicles(origin, this, true);
		
		// check if next vehicles are in vision range		
		// Outer lane
		v[0] = validateVehicleWithinVisionRange(v[0], x);
		// Inner lane
		v[1] = validateVehicleWithinVisionRange(v[1], x);

		return v;
	}
	
	/**
	 * Methods returns closest vehicles behind current vehicle.
	 * @param x distance
	 * @return array of vehicles
	 * @see signalGreen.Vehicle#getVehiclesAhead(double)
	 */
	private Vehicle[] getVehiclesBehind(double x) {

		Vehicle v[] = next.getNextVehicles(origin, this, false);
		
		// check if prev vehicles are in vision range		
		// Outer lane
		v[0] = validateVehicleWithinVisionRange(v[0], x);				
		// Inner lane
		v[1] = validateVehicleWithinVisionRange(v[1], x);

		return v;
	}
	
	/**
	 * If vehicle is within vision range retuns a
	 * reference to it, otherwise it is too far from
	 * the current vehicle so we return null.
	 * 
	 * @param v the vehicle 
	 * @param x the vision range
	 * @return vehicle or null
	 */
	private Vehicle validateVehicleWithinVisionRange(Vehicle v, double x) {
		if (v != null) {
			// check if next vehicles are in vision range		
			Coordinate c = this.getNetworkPos(); // current position
			Coordinate c1 = v.getNetworkPos();
			double dist = Utils.distance(c, c1, getGeography());
			if (dist >= x) {
				v = null;
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

	/**
	 * Real position refers to the position
	 * of a vehicle on a particular lane.
	 * 
	 * @param real position of vehicle
	 */
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
	 * Moves a vehicle towards a given Coordinate.
	 * Uses network position and then moves on the 
	 * real GIS projection.
	 * 
	 * @param c Coordinate to move towards
	 * @param x displacement in meters
	 */
	@SuppressWarnings("unchecked")
	public void moveTowards(Coordinate c, double x) 
	{
		
		double angle = Utils.getAngle(this.networkPos, c, getGeography());	
		try {
			getGeography().moveByVector(this, x, angle); // move agent	
		}
		catch (IllegalArgumentException iae) {
			System.out.println("Could not move vehicle for some reason.");
			iae.printStackTrace();
		}
		
		
		// update positions
		realPos = getGeography().getGeometry(this).getCoordinate();
		this.networkPos = this.getNetworkPosFromRealPos(this.lane);
	}
	
	/**
	 * Moves a vehicle to a given Coordinate.
	 * 
	 * @param coordinate
	 */
	@SuppressWarnings("unchecked")
	private void moveTo(Coordinate c) {
		GeometryFactory geomFac = new GeometryFactory();
		Point p = geomFac.createPoint(realPos);
		getGeography().move(this, p);		
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
		double accFactor = getAccelerationFactor();
		double x = Math.ceil(velocity + 0.5 * accFactor 
				* Constants.ACCELERATION * Math.pow(Constants.t, 2));	

		// adjust displacement for more realistic simulation
		x = x / Constants.CONV_RATIO_METERS;
				
		return x;
	}
	
	private double getAccelerationFactor() {
		if ((this instanceof CarVehicle) && (this.velocity <= Constants.SLOW)) {
			return Constants.CAR_SLOW_ACC;
		}
		else if (this instanceof CarVehicle) {
			return Constants.CAR_FAST_ACC;
		}
		else if (this instanceof TruckVehicle) {
			return Constants.TRUCK_ACC;
		}
		return Constants.CAR_SLOW_ACC;
	}

	/**
	 * Returns four coordinates that are perpendicular (+-90 degrees)
	 * to a logical or real position of the current vehicle.
	 * 
	 * @see signalGreen.Utils#createCoordsFromCoordAndAngle(Coordinate, double, double, Geography)
	 * @param coordinate either real or network position of vehicle
	 * @return array of coordinates
	 */
	private Coordinate[] getPosition(Coordinate c) {
		double azimuth = Utils.getAzimuth(origin.getCoords(), next.getCoords(), getGeography());
		Coordinate position[] = Utils.createCoordsFromCoordAndAngle(c, azimuth, Constants.DIST_LANE, getGeography());
		return position;
	}
	
	public Coordinate getRealPosFromNetworkPos(Constants.Lane lane) {
		Coordinate position[] = this.getPosition(this.networkPos);
		if (lane == Lane.OUTER) {
			return position[0];			
		}
		if (lane == Lane.INNER) {
			return position[1];
		}
		return null;
	}
	
	public Coordinate getNetworkPosFromRealPos(Constants.Lane lane) {
		Coordinate position[] = this.getPosition(this.realPos);
		if (lane == Lane.INNER) {
			return position[2];
		}
		if (lane == Lane.OUTER) {
			return position[3];			
		}
		return null;
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
	
	protected void setMaxVelocity(int maxVelocity) {
		this.maxVelocity = maxVelocity;
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
		// new velocity algorithm is:
		// V = V0 + a * t	
		this.velocity += Math.ceil(Constants.ACCELERATION * Constants.t);
		// vehicle cannot go faster than its maxVelocity
		if (this.velocity > this.maxVelocity) {
			this.velocity = this.maxVelocity;
		}
	}

	/**
	 * Similar algorithm to the acceleration.
	 * Uses standard kinematics equations for this purpose. 
	 */
	public void slowDown() {
		this.velocity -= Constants.ACCELERATION * Constants.t * 2;
		// velocity cannot be negative
		if (this.velocity < 0) {
			this.velocity = 0;
		}
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
		if (e != null) {
			r = this.roads.get(e);
		}
		return r;
	}

	/**
	 * Simulates a vehicle using the blinker.
	 * Ie. the vehicle tells which lane is going to 
	 * move to/stay on.
	 * 
	 * @return OUTER or INNER lane
	 */
	public Lane getLane() {
		return lane;
	}

	public void setLane(Lane lane) {
		this.lane = lane;
	}
	
	public String getDebug() {
		return debug;
	}

	/**
	 * Method compares two vehicles based on their distance to the next junction.
	 * Used to keep vehicles in a priority queue, in order
	 * to perform overtaking logic.
	 */
	@Override
	public int compareTo(Vehicle v) {
	    double thisDist = this.getDistanceToNextJunction();
	    double otherDist = v.getDistanceToNextJunction();
	    if (thisDist < otherDist) return -1;
	    if (thisDist > otherDist) return 1;
	    return 0;
	}

	private double getDistanceToNextJunction() {
		return Utils.distance(this.getNetworkPos(), next.getCoords(), getGeography());
	}
	
	/**
	 * Used to display the vehicle's icon
	 * using the correct angle. Uses convergence angle
	 * from grid north + azimuth.
	 * Called by the GIS display during simulation.
	 * 
	 * @return angle in degrees
	 */
	public double getAngle() {
		return Utils.getAngleForIcons(origin.getCoords(), next.getCoords(), getGeography());
	}
}
