
package signalGreen;

import java.io.File;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;

import org.geotools.data.shapefile.ShapefileDataStore;
import org.geotools.data.simple.SimpleFeatureIterator;
import org.opengis.feature.simple.SimpleFeature;

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

	// holds mapping between repast edges and roads, used to get the individual coordinates
	// alond the road segment.
	private Map<RepastEdge<Junction>, Road> roads = new HashMap<RepastEdge<Junction>, Road>();
	
	public Context build(Context context) {

		System.out.println("Geography Demo ContextBuilder.build()");
		junctions = new ArrayList<Junction>();
		
//		Parameters parm = RunEnvironment.getInstance().getParameters();
//		numAgents = (Integer)parm.getValue("numAgents");

		// To store GIS roads
		GeographyParameters geoParams = new GeographyParameters();
		Geography geography = GeographyFactoryFinder.createGeographyFactory(null)
				.createGeography("Geography", context, geoParams);

		// To store Junctions taken from the GIS shapefile
//		Geography junctionGeography = GeographyFactoryFinder.createGeographyFactory(null)
//				.createGeography("JunctionGeography", context, params);		
		
		// Road network
		// network.addEdge(this, junc, weight);
		NetworkBuilder<Object> roadBuilder = new NetworkBuilder<Object>("road network", context, true);
		roadBuilder.buildNetwork();
		network = (Network<Junction>) context.getProjection("road network");
		
		// Load Features from shapefiles
		// SWITCH MAPS HERE FOR DIFFERENT SCALES
		loadShapefile("data/NEW_YORK_MAPS/map1.shp", context, geography, network); // custom
//		loadShapefile("data/NEW_YORK_MAPS/map2.shp", context, geography, network); // custom small
//		loadShapefile("data/NEW_YORK_MAPS/map3.shp", context, geography, network); // custom minimal
		
		// create a few vehicles at random Junctions
		Random rand = new Random();
		for (int i = 0; i < 5; i++) {
			int[] speed = {100, 160, 240, 280};
			int maxSpeed = (speed[rand.nextInt(speed.length)]); // assign random speed to vehicles
			Vehicle vehicle = new Vehicle(network, geography, maxSpeed);
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
	@SuppressWarnings("unchecked")
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

		// contains a list of Junctions, so that we do not create duplicate Junctions.
		// This happens whenever two roads meet in a Junction.
		Map<Coordinate, Junction> cache = new HashMap<Coordinate, Junction>();
		
		// For each feature in the shapefile
		for (SimpleFeature feature : features){
			Geometry geom = (Geometry) feature.getDefaultGeometry();
			Object agent = null;
			
			// if shape is MultiLineString, create a Road object
			if (geom instanceof MultiLineString){
				MultiLineString line = (MultiLineString)feature.getDefaultGeometry();
				geom = (LineString) line.getGeometryN(0);

				// Get attributes and assign them to the agent
//				String name = (String)feature.getAttribute("ROUTE"); // attribute depends on the shapefile attributes.
//				String name = (String)feature.getAttribute("LNAME");
				String name = "test";
				agent = new Road(name);
				
				// gets all coordinates of this road segment into an array
				Coordinate[] c = geom.getCoordinates();
				// get first and last, which are the junctions to create
				Coordinate c1 = c[0]; // First coordinate
                Coordinate c2 = c[geom.getNumPoints() - 1]; // Last coordinate                                    
                
                // four cases can occur:
                // 1. initial/end Junction already exists
                // 2. initial/end Junction does not exist
                Junction j1, j2;
                if (cache.containsKey(c1)) {
                	// start junction already exists
                	j1 = cache.get(c1);
                }
                else {
                    // create start junction
                    j1 = new Junction(this.network);
                    j1.setCoords(c1);
                    context.add(j1);
                    Point p1 = geomFac.createPoint(c1);
                    geography.move(j1, p1);
                    cache.put(c1, j1);
                    junctions.add(j1);
                }
                
                if (cache.containsKey(c2)) {
                	// end junction already exists
                	j2 = cache.get(c2);
                }
                else {
	                // create end junction
	                j2 = new Junction(this.network);
	                j2.setCoords(c2);
	                context.add(j2);
	                Point p2 = geomFac.createPoint(c2);
	                geography.move(j2, p2);
	                cache.put(c2, j2);
	                junctions.add(j2);
                }                               
                
                // road needs to know its in and out edges
                double weight = Utils.distance(c1, c2, geography);
//              System.out.println("---\nKM: " + feature.getAttribute("KM"));
                RepastEdge<Junction> inEdge = network.addEdge(j1, j2, weight);
                RepastEdge<Junction> outEdge = network.addEdge(j2, j1, weight);
                this.roads.put(inEdge, (Road) agent); // tell the road which RepastEdge it has
                this.roads.put(outEdge, (Road) agent);
                ((Road) agent).setInEdge(inEdge);
                ((Road) agent).setOutEdge(outEdge);
                ((Road) agent).setLength(weight);
                ((Road) agent).setCoordinates(new ArrayList<Coordinate>(Arrays.asList(c)));
//                System.out.println(((Road) agent).toString()); // DEBUG
				
				// put road in the GIS projection
				if (agent != null){
					// show the road as it is in the GIS shapefile <-- many details shown
					// context.add(agent);
					// geography.move(agent, geom);
					
					// or display a simplified version of the map
					// in this case need to uncomment previous block of code
					Coordinate[] coords = new Coordinate[] { c1, c2 };
					LineString ls = geomFac.createLineString(coords);
					geom = (LineString)ls.getGeometryN(0);
					context.add(ls);
					geography.move(agent, geom);
				}
				else{
					System.out.println("Error creating agent.");
				}
			}

		}				
	}
}