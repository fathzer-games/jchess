package com.fathzer.games.game;

import java.util.Random;

import com.fathzer.games.Color;
import com.fathzer.games.clock.ClockSettings;

public abstract class AbstractGameSettings {
	private static final Random RANDOM_GENERATOR = new Random();
	
	// TODO This should probably be in ClockSettings
	private boolean startClockAfterFirstMove = false;
	private ClockSettings clockSettings = null;
	private ColorSetting player1Color = ColorSetting.RANDOM;
	
	public enum ColorSetting {
		RANDOM, BLACK, WHITE;
		
		public Color getColor() {
			if (BLACK.equals(this)) {
				return Color.BLACK;
			} else if (WHITE.equals(this)) {
				return Color.WHITE;
			} else {
				return RANDOM_GENERATOR.nextBoolean() ? Color.BLACK : Color.WHITE;
			}
		}
	}

	/** Check whether the clock should be started after first move 
	 * @return true if the clock should be started after first move
	 */
	public boolean isStartClockAfterFirstMove() {
		return startClockAfterFirstMove;
	}

	/** Sets the startClockAfterFirstMove attribute
	 * <br>default value is false.
	 * @param startClockAfterFirstMove true if the clock should be started after first move
	 */
	public void setStartClockAfterFirstMove(boolean startClockAfterFirstMove) {
		this.startClockAfterFirstMove = startClockAfterFirstMove;
	}

	/** Gets the clock settings.
	 * @return the clockSettings
	 */
	public ClockSettings getClockSettings() {
		return clockSettings;
	}

	/** Gets the clock settings
	 * <br>default value is null with means there is no time limit to the game.
	 * @param clockSettings the clockSettings to set
	 */
	public void setClockSettings(ClockSettings clockSettings) {
		this.clockSettings = clockSettings;
	}

	/** Gets the color settings of player 1
	 * @return the player 1 ColorSettings
	 */
	public ColorSetting getPlayer1Color() {
		return player1Color;
	}

	/** Sets the color settings of player 1
	 * <br>default value is {@link ColorSetting#RANDOM}
	 * @param player1Color the player 1 color setting
	 */
	public void setPlayer1Color(ColorSetting player1Color) {
		this.player1Color = player1Color;
	}
}
