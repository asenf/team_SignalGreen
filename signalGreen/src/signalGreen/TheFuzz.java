package signalGreen;

import repast.simphony.space.gis.Geography;
import repast.simphony.space.graph.Network;


//accerelate until maxspeed acheived. Maybe have to states,
//one flashing blues and twos, other obeying speed limits?

//ignore traffic lights, tailgate vehicle ahead if there is one at there speed
public class TheFuzz extends Vehicle {

	public TheFuzz(Network<Junction> network, Geography geography,
			int maxVelocity) {
		super(network, geography, 140);
		// TODO Auto-generated constructor stub
	}

}
