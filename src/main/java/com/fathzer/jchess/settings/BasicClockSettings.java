package com.fathzer.jchess.settings;

import com.fathzer.games.clock.ClockSettings;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/** A very simplified version of games-core clock settings. 
 */
@Getter
@Setter
@NoArgsConstructor
public class BasicClockSettings {
	/** Given number of seconds at the beginning of game. */
	private int initialTime;
	/** Time increment in seconds. */
	private int increment;
	/** Number of moves between increments. */
	private int movesNumberBeforeIncrement;
	
	public ClockSettings toClockSettings() {
		final ClockSettings result = new ClockSettings(initialTime);
		if (increment>0) {
			result.withIncrement(increment, movesNumberBeforeIncrement<=0 ? 1 : movesNumberBeforeIncrement, true);
		}
		return result;
	}
}
