package com.fathzer.jchess.uci;

import com.fathzer.games.Color;
import com.fathzer.games.ai.iterativedeepening.IterativeDeepeningEngine;
import com.fathzer.games.ai.time.BasicTimeManager;
import com.fathzer.games.clock.CountDownState;
import com.fathzer.jchess.Board;
import com.fathzer.jchess.Move;
import com.fathzer.jchess.ai.JChessEngine;
import com.fathzer.jchess.time.VuckovicSolakOracle;
import com.fathzer.jchess.uci.parameters.GoParameters;
import com.fathzer.jchess.uci.parameters.GoParameters.PlayerClockData;
import com.fathzer.jchess.uci.parameters.GoParameters.TimeOptions;

/** A class that configures the engine before executing the go command
 */
public class UCIEngineSearchConfiguration {
	private static final BasicTimeManager<Board<?>> TIME_MANAGER = new BasicTimeManager<>(VuckovicSolakOracle.INSTANCE);

	public static class EngineConfiguration {
		private long maxTime;
		private int depth;
		
		private EngineConfiguration(IterativeDeepeningEngine<?, ?> engine) {
			maxTime = engine.getDeepeningPolicy().getMaxTime();
			depth = engine.getDeepeningPolicy().getDepth();
		}
	}
	
	public EngineConfiguration configure(JChessEngine engine, GoParameters options, Board<Move> board) {
		final EngineConfiguration result = new EngineConfiguration(engine);
		final TimeOptions timeOptions = options.getTimeOptions();
		if (options.isPonder() || !options.getMoveToSearch().isEmpty() || options.getMate()>0 || options.getNodes()>0 || timeOptions.isInfinite()) {
			//TODO some options are not supported
		}
		if (options.getDepth()>0) {
			engine.getDeepeningPolicy().setDepth(options.getDepth());
		}
		if (timeOptions.getMoveTimeMs()>0) {
			engine.getDeepeningPolicy().setMaxTime(timeOptions.getMoveTimeMs());
		} else {
			final Color c = board.getActiveColor();
			final PlayerClockData engineClock = c==Color.WHITE ? timeOptions.getWhiteClock() : timeOptions.getBlackClock();
			if (engineClock.getRemainingMs()>0) {
				engine.getDeepeningPolicy().setMaxTime(getMaxTime(board, engineClock.getRemainingMs(), engineClock.getIncrementMs(), timeOptions.getMovesToGo()));
			}
		}
		return result;
	}

	public void set(JChessEngine engine, EngineConfiguration c) {
		engine.getDeepeningPolicy().setMaxTime(c.maxTime);
		engine.getDeepeningPolicy().setDepth(c.depth);
	}
	
	public long getMaxTime(Board<Move> board, long remainingMs, long incrementMs, int movesToGo) {
		return TIME_MANAGER.getMaxTime(board, new CountDownState(remainingMs, incrementMs, movesToGo));
	}
}
