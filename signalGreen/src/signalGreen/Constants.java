package signalGreen;

public final class Constants {
	
	public static final String NETWORK = "road network";
	public static final String ID = "signalGreen";
//	public static final String SPACE = "space";
//	public static final String GRID = "grid";
	
	// user parameter for creating number at runtime (aks)
	public static final String NUM_VEHICLES = "numVehicle";
	
//	public static final int SCALE = 10;
	
	// distance between lanes in meters
	public static final double DIST_LANE = 1.0;
	// distance of Lights in meters from Junction for display purposes
	public static final double DIST_LIGHTS = 1.0; // should be eq. DIST_LANE
	// minimum distance between vehicles driving in meters
	public static final double DIST_VEHICLES = 2.2;
	// minimum distance between vehicles stopped in meters
	public static final double DIST_VEHICLES_STOPPED = 1.0;
	
	public static enum Signal {
		GREEN, AMBER, RED
	}

	public static enum Lane {
		LEFT, RIGHT
	}
	
}

