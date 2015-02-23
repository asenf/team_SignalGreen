package signalGreen;

public class Intention {
	
	private Object goal;
	private int priority;
	
	
	public Intention(Junction d, int p){
		this.goal = d;
		this.priority = p;
	}

}
