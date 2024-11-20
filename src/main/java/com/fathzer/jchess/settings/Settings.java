package com.fathzer.jchess.settings;

import java.util.function.Function;
import java.util.function.Supplier;

import com.fathzer.games.clock.ClockSettings;
import com.fathzer.games.game.AbstractGameSettings;
import com.fathzer.jchess.Board;
import com.fathzer.jchess.Move;
import com.fathzer.jchess.fen.FENUtils;
import com.fathzer.jchess.GameBuilders;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@NoArgsConstructor
@Getter
@Setter
public class Settings extends AbstractGameSettings {
	private Variant variant = Variant.STANDARD;
	//TODO fen should be obtained by Settings panel 
	private String fen = buildFEN();
	private boolean tabletMode = true;
	private boolean showPossibleMoves = true;
	private boolean touchMove = false;
	private boolean startClockAfterFirstMove = false;
	private BasicClockSettings clock = null;
	private PlayerSettings player1 = new PlayerSettings();
	private PlayerSettings player2 = new PlayerSettings();
	
	private static final String buildFEN() {
		var fen = System.getProperty("fen");
		final String result = fen==null ? fen : fen.replace('_', ' ');
		if (result!=null) {
			System.out.println("Fen of start position was retrieved in 'fen' system property: "+result);
		}
		return result;
	}

	@AllArgsConstructor
	public enum Variant {
		STANDARD(toFunction(GameBuilders.STANDARD)), CHESS960(toFunction(GameBuilders.CHESS960));
		private final Function<String, Board<Move>> rules;

		/** Gets a Supplier that can create a new game.
		 * <br>Typically, it is able to create the representation of a new game (for example, a chess board at the beginning of the game)
		 */
		public Function<String, Board<Move>> getRules() {
			return rules;
		}
		
		private static Function<String, Board<Move>> toFunction(Supplier<Board<Move>> s) {
			return fen -> fen==null ? s.get() : FENUtils.from(fen);
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

	@Override
	public ClockSettings getClockSettings() {
		return clock.toClockSettings();
	}

	@Override
	public void setClockSettings(ClockSettings clockSettings) {
		throw new UnsupportedOperationException();
	}
}
