package com.fathzer.jchess.swing.settings;

import java.util.Random;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.module.SimpleModule;
import com.fathzer.games.Color;
import com.fathzer.games.GameBuilder;
import com.fathzer.games.clock.ClockSettings;
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
public class GameSettings {
	public static final ObjectMapper MAPPER = new ObjectMapper();
	
	static {
		SimpleModule module = new SimpleModule();
		module.addDeserializer(ClockSettings.class, new ClockSettingsDeserializer());
		MAPPER.registerModule(module);
	}
	
	private static final Random RANDOM_GENERATOR = new Random();
	private Variant variant = Variant.CHESS960;
	private boolean tabletMode = true;
	private boolean showPossibleMoves = true;
	private boolean touchMove = false;
	private boolean startClockAfterFirstMove = false;
	private ClockSettings clock = null;
	private PlayerSettings player1 = new PlayerSettings();
	private ColorSetting player1Color = ColorSetting.RANDOM;
	private PlayerSettings player2 = new PlayerSettings(null, new EngineSettings(), null);
	
	@Getter
	@AllArgsConstructor
	public enum Variant {
		STANDARD(GameBuilders.STANDARD), CHESS960(GameBuilders.CHESS960);
		private GameBuilder<Board<Move>> rules;
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
		private ClockSettings extraClock = null;
	}
	
	@NoArgsConstructor
	@Getter
	@Setter
	public static class EngineSettings {
		private String name = "jchess";
		private int level = 6;
	}
}
