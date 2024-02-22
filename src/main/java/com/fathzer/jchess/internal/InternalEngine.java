package com.fathzer.jchess.internal;

import java.util.Arrays;
import java.util.Collections;
import java.util.LinkedHashSet;
import java.util.List;

import com.fathzer.games.MoveGenerator.MoveConfidence;
import com.fathzer.games.ai.time.BasicTimeManager;
import com.fathzer.games.ai.transposition.SizeUnit;
import com.fathzer.games.clock.CountDownState;
import com.fathzer.games.util.PhysicalCores;
import com.fathzer.jchess.Board;
import com.fathzer.jchess.Move;
import com.fathzer.jchess.ai.JChessEngine;
import com.fathzer.jchess.ai.TT;
import com.fathzer.jchess.ai.evaluator.NaiveEvaluator;
import com.fathzer.jchess.ai.evaluator.SimplifiedEvaluator;
import com.fathzer.jchess.bot.Engine;
import com.fathzer.jchess.bot.Option;
import com.fathzer.jchess.bot.options.ComboOption;
import com.fathzer.jchess.fen.FENUtils;
import com.fathzer.jchess.lichess.DefaultOpenings;
import com.fathzer.jchess.settings.GameSettings.Variant;
import com.fathzer.jchess.time.VuckovicSolakOracle;
import com.fathzer.jchess.uci.JChessUCIEngine;
import com.fathzer.jchess.uci.UCIMove;

public class InternalEngine implements Engine {
	private static final long MILLIS_IN_SECONDS = 1000L;
	private static final BasicTimeManager<Board<Move>> TIME_MANAGER = new BasicTimeManager<>(VuckovicSolakOracle.INSTANCE);
	
	private static final String LEVEL_BABY_VALUE = "Baby";
	private static final String LEVEL_KID_VALUE = "Kid";
	private static final String LEVEL_TEEN_VALUE = "Teenager";
	private static final String LEVEL_ADULT_VALUE = "Adult";
	private static final String LEVEL_BEST_VALUE = "Best";

	private final JChessEngine engine;
	private final List<Option<?>> options;
	private Board<Move> board;
	
	public InternalEngine() {
		this.engine = new JChessEngine(SimplifiedEvaluator::new, 6);
		this.engine.setOpenings(DefaultOpenings.INSTANCE);
		this.engine.getDeepeningPolicy().setDeepenOnForced(false);
		this.engine.setTranspositionTable(new TT(16, SizeUnit.MB));
		this.board = null;
		this.options = buildOptions();
		setLevel(LEVEL_BEST_VALUE);
	}
	
	private List<Option<?>> buildOptions() {
		final ComboOption level = new ComboOption("Level",LEVEL_BEST_VALUE, new LinkedHashSet<>(Arrays.asList(LEVEL_BABY_VALUE,LEVEL_KID_VALUE,LEVEL_TEEN_VALUE,LEVEL_ADULT_VALUE,LEVEL_BEST_VALUE)));
		level.addListener((o,n) -> setLevel(n));
		return Collections.singletonList(level);
	}
	
	private void setLevel(String level) {
		final boolean naive = LEVEL_BABY_VALUE.equals(level) || LEVEL_KID_VALUE.equals(level);
		engine.setParallelism(PhysicalCores.count()<2 || naive ? 1 : 2);
		engine.setEvaluatorSupplier(naive ? NaiveEvaluator::new : SimplifiedEvaluator::new);
		long maxTime = Long.MAX_VALUE;
		int depth;
		if (LEVEL_BABY_VALUE.equals(level)) {
			depth = 2;
		} else if (LEVEL_KID_VALUE.equals(level)) {
			depth = 4;
		} else if (LEVEL_TEEN_VALUE.equals(level)) {
			depth = 6;
		} else if (LEVEL_ADULT_VALUE.equals(level)) {
			depth = 8;
			maxTime = 10*MILLIS_IN_SECONDS;
		} else if (LEVEL_BEST_VALUE.equals(level)) {
			depth = 14;
			maxTime = 30*MILLIS_IN_SECONDS;
		} else {
			throw new IllegalArgumentException();
		}
		engine.getDeepeningPolicy().setDepth(depth);
		engine.getDeepeningPolicy().setMaxTime(maxTime);
	}

	@Override
	public List<Option<?>> getOptions() {
		return options;
	}

	@Override
	public boolean isSupported(Variant variant) {
		return true;
	}

	@Override
	public boolean newGame(Variant variant) {
		board = null;
		return true;
	}

	@Override
	public void setPosition(String startpos, List<String> moves) {
		board = FENUtils.from(startpos); 
		board.setMoveComparatorBuilder(engine.getMoveComparatorSupplier());
		for (String m : moves) {
			final Move move = JChessUCIEngine.toMove(board, UCIMove.from(m));
			if (!board.makeMove(move, MoveConfidence.UNSAFE)) {
				throw new IllegalArgumentException("The move "+m+" is illegal");
			}
		}
	}

	@Override
	public String play(CountDownState countDownState) {
		if (board==null) {
			throw new IllegalStateException("No position set");
		}
		final long maxTime = engine.getDeepeningPolicy().getMaxTime();
		if (countDownState!=null) {
			engine.getDeepeningPolicy().setMaxTime(Math.min(maxTime, TIME_MANAGER.getMaxTime(board, countDownState)));
		}
		try {
			return JChessUCIEngine.toUCIMove(board.getCoordinatesSystem(), engine.apply(board)).toString();
		} finally {
			engine.getDeepeningPolicy().setMaxTime(maxTime);
		}
	}

	@Override
	public void close() {
		// Nothing to do
	}
}
