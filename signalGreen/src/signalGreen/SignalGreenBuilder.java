
package signalGreen;

import java.io.File;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Queue;
import java.util.Random;

import org.geotools.data.shapefile.ShapefileDataStore;
import org.geotools.data.simple.SimpleFeatureIterator;
import org.opengis.feature.simple.SimpleFeature;
import org.opengis.feature.type.AttributeType;

import com.vividsolutions.jts.geom.Coordinate;
import com.vividsolutions.jts.geom.Geometry;
import com.vividsolutions.jts.geom.GeometryFactory;
import com.vividsolutions.jts.geom.LineString;
import com.vividsolutions.jts.geom.MultiLineString;
import com.vividsolutions.jts.geom.Point;

import repast.simphony.context.Context;
import repast.simphony.context.space.continuous.ContinuousSpaceFactory;
import repast.simphony.context.space.continuous.ContinuousSpaceFactoryFinder;
import repast.simphony.context.space.gis.GeographyFactoryFinder;
import repast.simphony.context.space.graph.NetworkBuilder;
import repast.simphony.context.space.grid.GridFactory;
import repast.simphony.context.space.grid.GridFactoryFinder;
import repast.simphony.dataLoader.ContextBuilder;
import repast.simphony.engine.environment.RunEnvironment;
import repast.simphony.parameter.Parameters;
import repast.simphony.space.continuous.ContinuousSpace;
import repast.simphony.space.continuous.NdPoint;
import repast.simphony.space.continuous.RandomCartesianAdder;
import repast.simphony.space.gis.Geography;
import repast.simphony.space.gis.GeographyParameters;
import repast.simphony.space.graph.Network;
import repast.simphony.space.graph.RepastEdge;
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
	
	// List to store Junctions
	private List<Junction> junctions;
	private Network<Junction> network;
	private Geography geography;
	
	// user-defined parameters
	private int vehCount;
	private boolean usesTrafficLights;
	private String inputShapefile;
	
	// holds mapping between repast edges and roads, used to get the individual coordinates
	// alond the road segment.
	private Map<RepastEdge<Junction>, Road> roads = new HashMap<RepastEdge<Junction>, Road>();
	
	@Override
	public Context build(Context context) {

		junctions = new ArrayList<Junction>();

		// To store GIS roads
		GeographyParameters geoParams = new GeographyParameters();
		geography = GeographyFactoryFinder.createGeographyFactory(null)
				.createGeography("Geography", context, geoParams);
		
		// Road network
		// network.addEdge(this, junc, weight);
		NetworkBuilder<Object> roadBuilder = new NetworkBuilder<Object>("road network", context, true);
		roadBuilder.buildNetwork();
		network = (Network<Junction>) context.getProjection("road network");

		// User decides the number of vehicles placed on the map at runtime (aks)
		final Parameters params = RunEnvironment.getInstance().getParameters();
		vehCount = ((Integer) params.getValue(Constants.NUM_VEHICLES)).intValue();
		usesTrafficLights = ((boolean) params.getValue("usesTrafficLights"));
		inputShapefile = "data/" + ((String) params.getValue("inputShapefile"));
		
		// load user defined GIS shapefile
		File f = new File(inputShapefile);
		if (!f.exists() && f.isDirectory()) { 
			System.out.println("File Not Found!");
			return null;
		}
		loadShapefile(inputShapefile, context, geography, network); 
		
		// sets some default data for each junction in the topology
		// and sets appropriate position of traffic lights if needed.
		initJunctions(context);
		
		// create a few vehicles at random Junctions
		Random rand = new Random();
		for (int i = 0; i < vehCount; i++) {          // user defined at runtime (aks)
			int[] speed = {100, 120, 140, 80};
			int maxSpeed = (speed[rand.nextInt(speed.length)]); // assign random speed to vehicles
			Vehicle vehicle = new Vehicle(network, geography, roads, maxSpeed);
			context.add(vehicle);
			Junction origin = junctions.get(rand.nextInt(junctions.size()));	
			GeometryFactory geomFac = new GeometryFactory();
			Point p = geomFac.createPoint(origin.getCoords());
            geography.move(vehicle, p);
            vehicle.initVehicle(origin);
		}	
		
		return context;
	}

	/**
	 * Loads features from the specified shapefile.
	 * 
	 * @param filename relative path of the ESRI shapefile
	 * @param context the context
	 * @param geography the road geography
	 */
	@SuppressWarnings({ "unchecked", "unused" })
	private void loadShapefile(String filename, Context context, Geography geography, Network<Junction> network) {
		// used to create junctions on the gis projection
		GeometryFactory geomFac = new GeometryFactory();
		
		// read in shapefile
		URL url = null;
		try {
			url = new File(filename).toURL();
		} catch (MalformedURLException e1) {
			e1.printStackTrace();
		}

		List<SimpleFeature> features = new ArrayList<SimpleFeature>();
		
		// Try to load the shapefile
		SimpleFeatureIterator fiter = null;
		ShapefileDataStore store = null;
		store = new ShapefileDataStore(url);

		try {
			fiter = store.getFeatureSource().getFeatures().features();
			while(fiter.hasNext()){
				features.add(fiter.next());
			}
		} catch (IOException e) {
			e.printStackTrace();
		}
		finally{
			fiter.close();
			store.dispose();
		}
	
		// tmp map to understand which junctions will be actually traffic lights
		// or stop signs. Integer will be > 2, ie. junction has at least 3 roads.
		// We will also skip duplicate coordinates as we do not wand duplicate junctions.
		Map<Coordinate, Integer> map = new HashMap<Coordinate, Integer>();
		
		// For each feature in the shapefile
		for (SimpleFeature feature : features) {
			Geometry geom = (Geometry) feature.getDefaultGeometry();
			Object agent = null;
			
			// take into account MultiLineString shapes for Road objects
			if (geom instanceof MultiLineString){
				MultiLineString line = (MultiLineString)feature.getDefaultGeometry();
				geom = (LineString) line.getGeometryN(0);
				// get first and last, which are the junctions to create
				Coordinate[] c = geom.getCoordinates();
				Coordinate c1 = c[0]; // First coordinate
                Coordinate c2 = c[geom.getNumPoints() - 1]; // Last coordinate                                    
                
                // 1. initial/end Coordinate already found
                // 2. initial/end Coordinate not found yet
                if (map.containsKey(c1)) {
                	map.put(c1, map.get(c1) + 1);
                }
                else {
                    map.put(c1, 0);
                }
                
                if (map.containsKey(c2)) {
                	map.put(c2, map.get(c2) + 1);
                }
                else {
	                map.put(c2, 0);
                } 
			}
		}
		
		// contains a list of Junctions, so that we do not create duplicate Junctions.
		// This happens whenever two roads meet in a Junction.
		Map<Coordinate, Junction> cache = new HashMap<Coordinate, Junction>();
		
		// now create the junctions
		for (Map.Entry<Coordinate, Integer> entry : map.entrySet()) {
			
			Junction j;
		    Coordinate c = entry.getKey();
		    Integer nRoads = entry.getValue();
		    
		    if (nRoads > 1) {
		    	// this junction needs special traffic management policy
		    	// such as traffic lights or give way signs because it
		    	// has more than two roads.
		    	if (this.usesTrafficLights == true) {
		    		j = new TrafficLight(this.network, this.geography);
		    	}
		    	else { // implement here all policies
		    		j = new Junction(this.network, this.geography);
		    	}
		    }
		    else {
		    	// generic junction
		    	j = new Junction(this.network, this.geography);
		    }
		    // put Junction in the GIS projection
		    j.setCoords(c);
		    cache.put(c, j);
            context.add(j);
            Point p = geomFac.createPoint(c);
            geography.move(j, p);
            junctions.add(j);
		}
		
		// Now we have all Junctions created, but we need to 
		// create the network topology. We do this by iterating
		// through each road in the shapefile, and create a network
		// edge for each road.
		for (SimpleFeature feature : features) {
			Geometry geom = (Geometry) feature.getDefaultGeometry();
			Object agent = null;
			
			// if shape is MultiLineString, create a Road object
			if (geom instanceof MultiLineString){
				MultiLineString line = (MultiLineString)feature.getDefaultGeometry();
				geom = (LineString) line.getGeometryN(0);

				// Get attributes and assign them to the agent
				// attributes depend on the shapefile attributes.
				String name = (String)feature.getAttribute("LNAME");
//				String name = "test";
				agent = new Road(name);
//				System.out.println("Name: " + name + " --> " + feature.getAttribute("THRULANES")
//						+ "\nTHRULANES " + feature.getAttribute("THRULANES")
//						+ "\nFCLASS " + feature.getAttribute("FCLASS")
//						+ "\nSTATUS " + feature.getAttribute("STATUS")
//						+ "\nNHS " + feature.getAttribute("NHS")
//						+ "\nRECTYPE " + feature.getAttribute("RECTYPE")
//						+ "\n\n");
                
				// road segment start and end coordinate
				Coordinate[] c = geom.getCoordinates();
				Coordinate c1 = c[0]; // First coordinate
                Coordinate c2 = c[geom.getNumPoints() - 1]; // Last coordinate 
                
        		
        		// TEST LANES ******
                
                addLanes(c1, c2, geography, context);
        						
        		//************
				
                Junction j1 = cache.get(c1);
                Junction j2 = cache.get(c2);                            
                
                // set road data
                double weight = Utils.distance(c1, c2, geography);

    			RepastEdge<Junction> re1 = network.addEdge(j1, j2, weight);
    			RepastEdge<Junction> re2 = network.addEdge(j2, j1, weight);
                
                j1.addJunction(j2);
                j2.addJunction(j1);
                
                ((Road) agent).setLength(weight);
                ((Road) agent).setCoordinates(new ArrayList<Coordinate>(Arrays.asList(c)));
                // System.out.println(((Road) agent).toString()); // DEBUG

                // Road-RepastEdge mapping for lane management use
                this.roads.put(re1, (Road) agent);
                this.roads.put(re2, (Road) agent);
                
				// put road in the GIS projection
				// 1. show the road as it is in the GIS shapefile <-- many details shown
				// context.add(agent);
				// geography.move(agent, geom);
				
				// 2. or display a simplified version of the map
				// in this case need to uncomment previous block of code
				Coordinate[] coords = new Coordinate[] { c1, c2 };
				LineString ls = geomFac.createLineString(coords);
				geom = (LineString)ls.getGeometryN(0);
				context.add(ls);
				geography.move(agent, geom);
			}

		}				
	}
	
	
	@SuppressWarnings("unchecked")
	private void addLanes(Coordinate c1, Coordinate c2, Geography geography, Context context) {

		GeometryFactory geomFac = new GeometryFactory();
		
        double azimuth = Utils.getAzimuth(c1, c2, geography);
		Coordinate dest1[] = Utils.createCoordsFromCoordAndAngle(c1, azimuth, Constants.DIST_LANE, geography);

		// 1
		Junction j1Left = new Junction(network, geography);
		context.add(j1Left);
		Point p = geomFac.createPoint(dest1[0]);
		geography.move(j1Left, p);
		// 2
		Junction j1Right = new Junction(network, geography);
		context.add(j1Left);
		p = geomFac.createPoint(dest1[1]);
		geography.move(j1Right, p);
		
		Coordinate dest2[] = Utils.createCoordsFromCoordAndAngle(c2, azimuth, Constants.DIST_LANE, geography);
		// 3
		Junction j2Left = new Junction(network, geography);
		context.add(j2Left);
		p = geomFac.createPoint(dest2[0]);
		geography.move(j2Left, p);
		// 4
		Junction j2Right = new Junction(network, geography);
		context.add(j2Right);
		p = geomFac.createPoint(dest2[1]);
		geography.move(j2Right, p);
		
		// add lanes
		
		// 1
		Coordinate[] coords = new Coordinate[] { dest1[0], dest2[0] };
		LineString ls = geomFac.createLineString(coords);
		Geometry geom = (LineString) ls.getGeometryN(0);
		context.add(ls);
		geography.move(new Road("test"), geom);
		
		// 2
		coords = new Coordinate[] { dest1[1], dest2[1] };
		ls = geomFac.createLineString(coords);
		geom = (LineString) ls.getGeometryN(0);
		context.add(ls);
		geography.move(new Road("test"), geom);
		
//		geography.moveByVector(j1Left, Constants.DIST_LIGHTS, angle);
		
	}

	/**
	 * 1. Initialises queues for every junction. Each junction holds
	 * a list of incoming vehicles for each in-edge road segment.
	 * 2. Puts Lights of TrafficLights on GIS projection if needed.
	 * To be called only after all junctions have been loaded from
	 * the GIS shapefile.
	 */
	@SuppressWarnings("unchecked")
	private void initJunctions(Context context) {
		Iterator<Junction> it = this.junctions.iterator();
		while (it.hasNext()) {
			Junction j = it.next();
			List<Junction> l = j.getJunctions();

			// initialise vehicle queues
			Iterator<Junction> itmap = l.iterator();
			while (itmap.hasNext()) {
				j.vehicles.put(itmap.next(), new LinkedList<Vehicle>());
			}			
			
			// position Lights of TrafficLights if needed		
			if (j instanceof TrafficLight) {
				Map<Junction, Light> lights = ((TrafficLight) j).getLights();
				for (Map.Entry<Junction, Light> e : lights.entrySet()) {
					Light light = e.getValue();
					Coordinate coords = e.getKey().getCoords();
					Coordinate currPos = geography.getGeometry(j).getCoordinate();
					double angle = Utils.getAngle(currPos, coords, geography);
					context.add(light);
					GeometryFactory geomFac = new GeometryFactory();
					Point p = geomFac.createPoint(currPos);
					geography.move(light, p);
					geography.moveByVector(light, Constants.DIST_LIGHTS, angle);
				}
				
			}
		}
	}
}