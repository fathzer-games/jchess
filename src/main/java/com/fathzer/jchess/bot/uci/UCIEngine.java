package com.fathzer.jchess.bot.uci;

import java.io.IOException;
import java.util.Arrays;
import java.util.EnumMap;
import java.util.List;
import java.util.Optional;

import com.fathzer.games.clock.CountDownState;
import com.fathzer.jchess.bot.Engine;
import com.fathzer.jchess.bot.uci.EngineLoader.EngineData;
import com.fathzer.jchess.settings.Settings.Variant;
import com.fathzer.uci.client.GoParameters;
import com.fathzer.uci.client.GoReply.UCIMove;

public class UCIEngine extends com.fathzer.uci.client.UCIEngine implements Engine {
	private static final EnumMap<Variant, com.fathzer.uci.client.Variant> TO_UCI = new EnumMap<>(Variant.class);

	static {
		TO_UCI.put(Variant.STANDARD, com.fathzer.uci.client.Variant.STANDARD);
		TO_UCI.put(Variant.CHESS960, com.fathzer.uci.client.Variant.CHESS960);
	}

	private EngineData data;

	public UCIEngine(EngineData data) throws IOException {
		super(Arrays.asList(data.getCommand()), e -> ((UCIEngine)e).data=data);
	}
	
	@Override
	public boolean isSupported(Variant variant) {
		return super.isSupported(TO_UCI.get(variant));
	}

	@Override
	public boolean newGame(Variant variant) throws IOException {
		return super.newGame(TO_UCI.get(variant));
	}

	@Override
	public void setPosition(String fen, List<String> moves) throws IOException {
		super.setPosition(Optional.ofNullable(fen), moves);
	}

	@Override
	public String getMove(CountDownState params) throws IOException {
		final GoParameters goParams = new GoParameters();
		final GoParameters.TimeControl tc = goParams.getTimeControl();
		tc.setRemainingMs(params.getRemainingMs());
		tc.setIncrementMs(params.getIncrementMs());
		tc.setMovesToGo(params.getMovesToGo());
		final List<UCIMove> moves = super.go(goParams).getMoves();
		return moves.isEmpty() ? null : moves.get(0).getMove();
	}
	
	@Override
	public String getName() {
		return data.getName();
	}
}
