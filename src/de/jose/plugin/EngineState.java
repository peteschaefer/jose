package de.jose.plugin;

import de.jose.Application;

public enum EngineState
{
	/**
	 * mode: paused (engine not in use)
	 * engine position not in synch with application
	 */
	PAUSED(0),
	/**
	 * mode: thinking (calculating the next computer move)
	 */
	THINKING(1),
	/**
	 * mode: waiting for user move, pondering (permanent brain)
	 */
	PONDERING(2),
	/**
	 * mode: anylizing (i.e. thinking but not moving automatically)
	 */
	ANALYZING(3);

	public final int numval;
	EngineState(int numval) { this.numval=numval; }

	public static EngineState valueOf(Object val) {
		if (val instanceof Number) {
			int idx = ((Number) val).intValue();
			if (idx >=0 && idx < EngineState.values().length)
				return values()[idx];
			else
				return null;
		}
		else {
			return EngineState.valueOf(val.toString());
		}
	}
}
