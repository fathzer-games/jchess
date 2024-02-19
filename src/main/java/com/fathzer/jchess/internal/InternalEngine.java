package com.fathzer.jchess.internal;

import java.util.Arrays;
import java.util.List;
import java.util.Set;

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
import com.fathzer.jchess.bot.options.SpinOption;
import com.fathzer.jchess.fen.FENUtils;
import com.fathzer.jchess.lichess.DefaultOpenings;
import com.fathzer.jchess.settings.GameSettings.Variant;
import com.fathzer.jchess.time.VuckovicSolakOracle;
import com.fathzer.jchess.uci.JChessUCIEngine;
import com.fathzer.jchess.uci.UCIMove;

public class InternalEngine implements Engine {
	private static final BasicTimeManager<Board<Move>> TIME_MANAGER = new BasicTimeManager<>(VuckovicSolakOracle.INSTANCE);
	
	private static final String SIMPLIFIED_EVAL_VALUE = "simplified";

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
	}
	
	private List<Option<?>> buildOptions() {
		final int threads = PhysicalCores.count()>=2 ? 2 : 1;
		this.engine.setParallelism(threads);
		final ComboOption evalOption = new ComboOption("Evaluation", SIMPLIFIED_EVAL_VALUE, Set.of(SIMPLIFIED_EVAL_VALUE,"naive"));
		evalOption.addListener((o,n) -> this.engine.setEvaluatorSupplier(SIMPLIFIED_EVAL_VALUE.equals(n)?SimplifiedEvaluator::new:NaiveEvaluator::new));
		final SpinOption threadsOption = new SpinOption("Threads", threads, 1, Runtime.getRuntime().availableProcessors());
		threadsOption.addListener((o,n)->this.engine.setParallelism(n.intValue()));
		final SpinOption maxTimeOption = new SpinOption("maxtime", 30000, 100, 1800000);
		maxTimeOption.addListener((o,n) -> this.engine.getDeepeningPolicy().setMaxTime(n));
		final SpinOption depthOption = new SpinOption("depth", 6, 1, 128);
		depthOption.addListener((o,n) -> this.engine.getDeepeningPolicy().setDepth(n.intValue()));
		return Arrays.asList(
				evalOption,
				threadsOption,
				maxTimeOption,
				depthOption
		);
	}

	@Override
	public String getName() {
		return "Internal";
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
		engine.getDeepeningPolicy().setMaxTime(Math.min(maxTime, TIME_MANAGER.getMaxTime(board, countDownState)));
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
