package signalGreen;

import signalGreen.Constants.Signal;

/**
 * Light object for Traffic Light Junctions.
 * 
 * @author Waqar
 *
 */
public class Light {

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
	
	public void toggleSignal() {
		if (this.signal == Signal.GREEN) {
			this.signal = Signal.RED;
		} else {
			this.signal = Signal.GREEN;
		}
	}
	
}
