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
import com.fathzer.jchess.ai.evaluator.SimplifiedEvaluator;
import com.fathzer.jchess.bot.Engine;
import com.fathzer.jchess.bot.Option;
import com.fathzer.jchess.bot.Variant;
import com.fathzer.jchess.bot.options.ComboOption;
import com.fathzer.jchess.bot.options.SpinOption;
import com.fathzer.jchess.fen.FENUtils;
import com.fathzer.jchess.lichess.DefaultOpenings;
import com.fathzer.jchess.time.VuckovicSolakOracle;
import com.fathzer.jchess.uci.JChessUCIEngine;
import com.fathzer.jchess.uci.UCIMove;

public class InternalEngine implements Engine {
	private static final BasicTimeManager<Board<Move>> TIME_MANAGER = new BasicTimeManager<>(VuckovicSolakOracle.INSTANCE);

	private final JChessEngine engine;
	private final List<Option<?>> options;
	private Board<Move> board;
	
	public InternalEngine() {
		this.engine = new JChessEngine(SimplifiedEvaluator::new, 6);
		this.engine.setOpenings(DefaultOpenings.INSTANCE);
		this.engine.getDeepeningPolicy().setDeepenOnForced(false);
		this.engine.setTranspositionTable(new TT(16, SizeUnit.MB));
		this.board = null;
		final int threads = PhysicalCores.count()>=2 ? 2 : 1;
		this.engine.setParallelism(threads);
		this.options = Arrays.asList(
				new ComboOption("evaluation", "simplified", Set.of("simplified","naive")),
				new SpinOption<Long>("threads", (long)threads, 1L, (long)Runtime.getRuntime().availableProcessors()),
				new SpinOption<Long>("maxtime", Long.MAX_VALUE, 1L, Long.MAX_VALUE),
				new SpinOption<Long>("depth", 6L, 1L, 128L)
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
		engine.getDeepeningPolicy().setMaxTime(TIME_MANAGER.getMaxTime(board, countDownState));
		return JChessUCIEngine.toUCIMove(board.getCoordinatesSystem(), engine.apply(board)).toString();
	}
}
