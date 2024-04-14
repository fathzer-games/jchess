package com.fathzer.jchess.settings;

import java.util.Random;
import java.util.function.Supplier;

import com.fathzer.games.Color;
import com.fathzer.jchess.Board;
import com.fathzer.jchess.Move;
import com.fathzer.jchess.GameBuilders;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@NoArgsConstructor
@Getter
@Setter
public class Settings {
	private static final Random RANDOM_GENERATOR = new Random();
	
	private Variant variant = Variant.STANDARD;
	private boolean tabletMode = true;
	private boolean showPossibleMoves = true;
	private boolean touchMove = false;
	private boolean startClockAfterFirstMove = false;
	private BasicClockSettings clock = null;
	private PlayerSettings player1 = new PlayerSettings();
	private ColorSetting player1Color = ColorSetting.RANDOM;
	private PlayerSettings player2 = new PlayerSettings();
	

	@AllArgsConstructor
	public enum Variant {
		STANDARD(GameBuilders.STANDARD), CHESS960(GameBuilders.CHESS960);
		private final Supplier<Board<Move>> rules;

		/** Gets a Supplier that can create a new game.
		 * <br>Typically, it is able to create the representation of a new game (for example, a chess board at the beginning of the game)
		 */
		public Supplier<Board<Move>> getRules() {
			return rules;
		}
	}
	
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
	
	@NoArgsConstructor
	@AllArgsConstructor
	@Getter
	@Setter
	public static class PlayerSettings {
		private String name = null;
		private EngineSettings engine = null;
	}
	
	@NoArgsConstructor
	@Getter
	@Setter
	public static class EngineSettings {
		private String name;
	}
}
