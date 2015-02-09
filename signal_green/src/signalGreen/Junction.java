package signalGreen;

import repast.simphony.space.continuous.ContinuousSpace;
import repast.simphony.space.continuous.NdPoint;
import repast.simphony.space.graph.Network;
import repast.simphony.space.grid.Grid;

public class Junction {

	private Network<Object> network;
	private ContinuousSpace<Object> space;
	private Grid<Object> grid;
	
	private Junction northJunc;
	private Junction eastJunc;
	private Junction southJunc;
	private Junction westJunc;
			
	public Junction(Network<Object> network, ContinuousSpace<Object> space, 
			Grid<Object> grid) {
	
		this.network = network;
		this.space = space;
		this.grid = grid;
	}
	
	public Junction getNorthJunc() {
		return northJunc;
	}

	public void setNorthJunc(Junction northJunc) {
		this.northJunc = northJunc;
		network.addEdge(this, northJunc);
	}

	public Junction getEastJunc() {
		return eastJunc;
	}

	public void setEastJunc(Junction eastJunc) {
		this.eastJunc = eastJunc;
		network.addEdge(this, eastJunc);
	}

	public Junction getSouthJunc() {
		return southJunc;
	}

	public void setSouthJunc(Junction southJunc) {
		this.southJunc = southJunc;
		network.addEdge(southJunc, this);
	}

	public Junction getWestJunc() {
		return westJunc;
	}

	public void setWestJunc(Junction westJunc) {
		this.westJunc = westJunc;
		network.addEdge(westJunc, this);
	}
	
	public void setLocation(int x, int y) {
		space.moveTo(this, x, y);
	}
	
	public NdPoint getLocation() {
		return space.getLocation(this);
	}
	
}
