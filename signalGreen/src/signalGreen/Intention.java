package signalGreen;

/* Intentions are basically goals. things like overtaking.
 * to begin with, just make intentions what the next junction is.
 */

public class Intention {
	
	private Object goal;
	private int priority;
	
	
	
	//have several constructors, for diffrent types of intentions
	public Intention(Junction d, int p){
		this.goal = d;
		this.priority = p;
	}

}