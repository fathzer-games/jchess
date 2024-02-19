package com.fathzer.jchess.settings;

import java.util.Random;

import org.json.JSONObject;

import com.fathzer.games.Color;
import com.fathzer.games.GameBuilder;
import com.fathzer.jchess.Board;
import com.fathzer.jchess.Move;
import com.fathzer.util.TinyJackson;
import com.fathzer.jchess.GameBuilders;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@NoArgsConstructor
@Getter
@Setter
public class GameSettings {
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
	}
	
	@NoArgsConstructor
	@Getter
	@Setter
	public static class EngineSettings {
		private String name;
	}
	
	public static void main(String[] args) {
		String json = "{\"variant\":\"STANDARD\",\"tabletMode\":true,\"showPossibleMoves\":true,\"touchMove\":false,\"startClockAfterFirstMove\":false,\"clock\":{\"initialTime\":180,\"increment\":2,\"movesNumberBeforeIncrement\":1,\"canAccumulate\":true,\"movesNumberBeforeNext\":2147483647,\"maxRemainingKept\":0,\"next\":null},\"player1\":{\"name\":null,\"engine\":null,\"extraClock\":null},\"player1Color\":\"RANDOM\",\"player2\":{\"name\":null,\"engine\":{\"name\":\"jchess\",\"level\":16,\"evaluator\":\"simple\"},\"extraClock\":null}}";
		JSONObject jsonO = new JSONObject(json);
		GameSettings settings = TinyJackson.toObject(jsonO, GameSettings.class);
		System.out.println(settings);
	}
}
