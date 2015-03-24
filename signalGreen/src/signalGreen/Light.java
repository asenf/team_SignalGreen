package signalGreen;

import signalGreen.Constants.Signal;

/**
 * Light object for Traffic Light Junctions.
 * 
 * @author Waqar
 *
 */
public class Light extends GisAgent {

	private Signal signal;
	
	/**
	 * Initialize light with user-defined condition.
	 * 
	 * @param signal to start with.
	 */
	public Light(Signal signal) {
		
		this.signal = signal;
	}
	
	/**
	 * Get the current signal.
	 * 
	 * @return the signal
	 */
	public Signal getSignal() {
		return signal;
	}

	/**
	 * Set the current signal.
	 * 
	 * @param signal the signal to set
	 */
	public void setSignal(Signal signal) {
		this.signal = signal;
	}
	
	/**
	 * Switches signal from GREEN to RED or AMBER.
	 */
	public void toggleSignal() {
		if (this.signal == Signal.GREEN) {
			this.signal = Signal.RED;
		} else {
			this.signal = Signal.GREEN;
		}
	}
	
	/**
	 * Used by the GIS display to know which color
	 * has the traffic light at any moment.
	 *  
	 * @return integer representation of the current signal
	 */
	public int getColor() {
		// DO NOT CHANGE VALUES
		if (this.signal == Signal.GREEN) return 0;
		if (this.signal == Signal.AMBER) return 5;
		if (this.signal == Signal.RED) return 10;
		return 15;
	}
	
}
