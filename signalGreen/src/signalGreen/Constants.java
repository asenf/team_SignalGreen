package signalGreen;

public final class Constants {
	
	public static final String NETWORK = "road network";
	public static final String ID = "signalGreen";
	public static final String SPACE = "space";
	public static final String GRID = "grid";
	
	// user parameter for creating number at runtime (aks)
	public static final String NUM_VEHICLES = "numVehicle";
	
	public static final int SCALE = 10;
	
	// distance of Lights in meters from Junction for display purposes
	public static final double DIST_LIGHTS = 18.0;
	// minimum distance between vehicles driving in meters
	public static final double DIST_VEHICLES = 20.0;
	// minimum distance between vehicles stopped in meters
	public static final double DIST_VEHICLES_STOPPED = 5.0;
	
	public static enum Signal {
		GREEN, AMBER, RED
	}
	
}

