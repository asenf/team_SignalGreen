package signalGreen;

/**
 * Constant class holds all arbitrary calibration parameters
 * derived from manual testing of Signal Green.
 * 
 * @author Signal Green Team*
 */
public final class Constants {
	
	public static final String NETWORK = "road network";
	public static final String ID = "signalGreen";
	
	// put all GIS maps in the following folder
	public static final String MAPS_FOLDER = "data/maps/";
	
	// user-defined parameter
	public static final String NUM_VEHICLES = "numVehicle";
	
	// distance between lanes in meters
	public static final double DIST_LANE = 1.0;
	// distance of Lights in meters from Junction for display purposes
	public static final double DIST_LIGHTS = (DIST_LANE * 2); // should be eq. 2 * DIST_LANE
	// minimum distance between vehicles driving in meters
	public static final double DIST_VEHICLES = 1.8;
	// minimum distance between vehicles stopped in meters
	public static final double DIST_VEHICLES_STOPPED = 1.0;
	
	// arbitrary value for time to make simulation faster, in reality t = 1 tick.
	// Used to compute velocity and displacement.
	public static final int t = 4;
	// arbitary value used to adjust GIS projection meters
	// because vehicle graphics are bigger than real scale
	public final static int CONV_RATIO_METERS = 70;
	
	// default speed limit for roads
	public static final int DEFAULT_SPEEDLIMIT = 80;
	// maximum velocity of cars when initialised
	public final static int[] speed = {100, 120, 140, 80};
	// boundaries what is fast and slow, in km/h
	public static final int VERY_SLOW = 80;
	public static final int SLOW = 100;
	public static final int FAST = 140;
	public static final int VERY_FAST = 160;

	// arbitrary value for vehicle acceleration
	public static final double ACCELERATION = 1.6; // m/s
	// acceleration factor: trucks have smaller accel. than cars
	public static final double CAR_SLOW_ACC = 1.0;
	public static final double CAR_FAST_ACC = 1.2;
	public static final double TRUCK_ACC = 0.8;
	public static final int TRUCK_DEFAULT_MAX_VELOCITY = 80;
	
	// car graphics
	public static String ICON_SLOW_CAR = "car_simple.png";
	public static String ICON_FAST_CAR = "car_fast.png";
			
	// traffic light signals
	public static enum Signal {
		GREEN, AMBER, RED
	}
		
	// 
	public static enum RoadType {
		SINGLE_LANE, TWO_LANES
	}
	
	// roads have two lanes per side
	public static enum Lane {
		INNER, OUTER
	}
	
}

