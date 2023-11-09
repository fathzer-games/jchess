package com.fathzer.jchess.uci;

import java.util.Arrays;
import java.util.LinkedHashSet;
import java.util.NoSuchElementException;
import java.util.Optional;

import com.fathzer.games.Color;
import com.fathzer.games.MoveGenerator;
import com.fathzer.games.MoveGenerator.MoveConfidence;
import com.fathzer.games.ai.evaluation.Evaluator;
import com.fathzer.games.perft.TestableMoveGeneratorSupplier;
import com.fathzer.games.util.PhysicalCores;
import com.fathzer.jchess.Board;
import com.fathzer.jchess.CoordinatesSystem;
import com.fathzer.jchess.Move;
import com.fathzer.jchess.Piece;
import com.fathzer.jchess.ai.JChessEngine;
import com.fathzer.jchess.ai.evaluator.BasicEvaluator;
import com.fathzer.jchess.ai.evaluator.simple.SimpleEvaluator;
import com.fathzer.jchess.fen.FENUtils;
import com.fathzer.jchess.generic.BasicMove;
import com.fathzer.jchess.lichess.DefaultOpenings;
import com.fathzer.jchess.uci.option.ComboOption;
import com.fathzer.jchess.uci.option.Option;
import com.fathzer.jchess.uci.option.SpinOption;

public class JChessUCIEngine implements Engine, TestableMoveGeneratorSupplier<Move>, MoveGeneratorSupplier<Move>, MoveToUCIConverter<Move> {
	private static final String BEST_LEVEL = "best";
	private static final String AVERAGE_LEVEL = "average";
	private static final String SILLY_LEVEL = "silly";
	private static final String NAIVE_EVALUATOR = "naive";
	private static final String SIMPLIFIED_EVALUATOR = "simplified";
	
	private Board<Move> board;
	private final JChessEngine engine;

	public JChessUCIEngine() {
		engine = new JChessEngine(new SimpleEvaluator(), 10);
		engine.setOpenings(DefaultOpenings.INSTANCE);
		engine.getDeepeningPolicy().setDeepenOnForced(false);
	}
	
	@Override
	public String getId() {
		return "JChess";
	}
	
	@Override
	public String getAuthor() {
		return "Jean-Marc Astesana (Fathzer)";
	}
	
	@Override
	public Option<?>[] getOptions() {
		return new Option[] {
			new ComboOption("level", this::setLevel, AVERAGE_LEVEL, new LinkedHashSet<>(Arrays.asList(SILLY_LEVEL, AVERAGE_LEVEL, BEST_LEVEL))),
			new SpinOption("thread", this::setParallelism, PhysicalCores.count(), 1, Runtime.getRuntime().availableProcessors()),
			new ComboOption("eval", this::setEvaluator, NAIVE_EVALUATOR, new LinkedHashSet<>(Arrays.asList(NAIVE_EVALUATOR, SIMPLIFIED_EVALUATOR)))
		};
	}
	
	private void setLevel(String level) {
		final int depth;
		if (SILLY_LEVEL.equals(level)) {
			depth = 4;
		} else if (AVERAGE_LEVEL.equals(level)) {
			depth = 6;
		} else if (BEST_LEVEL.equals(level)) {
			depth = 12;
			engine.getDeepeningPolicy().setMaxTime(30000);
		} else {
			throw new IllegalArgumentException();
		}
		engine.getDeepeningPolicy().setDepth(depth);
	}
	
	private void setParallelism(int parallelism) {
		engine.setParallelism(parallelism);
	}
	
	private void setEvaluator(String eval) {
		final Evaluator<Board<Move>> evaluator;
		if (NAIVE_EVALUATOR.equals(eval)) {
			evaluator = new BasicEvaluator();
		} else if (SIMPLIFIED_EVALUATOR.equals(eval)) {
			evaluator = new SimpleEvaluator();
		} else {
			throw new IllegalArgumentException();
		}
		engine.setEvaluator(evaluator);
	}

	@Override
	public boolean isChess960Supported() {
		return true;
	}
	
	@Override
	public void move(UCIMove move) {
		board.makeMove(toMove(board.getCoordinatesSystem(), move, board.getActiveColor()), MoveConfidence.UNSAFE);
	}
	
	public static Move toMove(CoordinatesSystem cs, UCIMove move, Color color) {
		final int from = cs.getIndex(move.getFrom());
		final int to = cs.getIndex(move.getTo());
		String promotion = move.getPromotion();
		if (promotion!=null && Color.WHITE.equals(color)) {
			// Warning the promotion code is always in lowercase
			promotion = promotion.toUpperCase();
		}
		return toMove(from, to, promotion);
	}
	
	private static Move toMove(int from, int to, String promotionAsFen) {
		if (promotionAsFen==null) {
			return new BasicMove(from, to);
		} else {
			final Optional<Piece> o = Arrays.stream(com.fathzer.jchess.Piece.values()).filter(x->promotionAsFen.equals(x.getNotation())).findAny();
			if (o.isEmpty()) {
				throw new NoSuchElementException(promotionAsFen+" is not a valid piece notation");
			} else {
				return new BasicMove(from, to, o.get());				
			}
		}
	}

	@Override
	public void setStartPosition(String fen) {
		board = FENUtils.from(fen);
	}

	@Override
	public LongRunningTask<BestMoveReply> go(GoOptions options) {
		return new LongRunningTask<>() {
			@Override
			public BestMoveReply get() {
				final UCIEngineSearchConfiguration c = new UCIEngineSearchConfiguration();
				final UCIEngineSearchConfiguration.EngineConfiguration previous = c.configure(engine, options, board);
				final Move move = engine.apply(board);
				c.set(engine, previous);
				return new BestMoveReply(toMove(board.getCoordinatesSystem(), move));
			}

			@Override
			public void stop() {
				super.stop();
				engine.interrupt();
			}
		};
	}
	
	private static UCIMove toMove(CoordinatesSystem cs, Move move) {
		final String promotion = move.getPromotion()==null ? null : move.getPromotion().getNotation().toLowerCase();
		return new UCIMove(cs.getAlgebraicNotation(move.getFrom()), cs.getAlgebraicNotation(move.getTo()), promotion);
	}

	@Override
	public MoveGenerator<Move> get() {
		Board<Move> copy = board.create();
		copy.copy(board);
		return copy;
	}
	
	@Override
	public String toUCI(Move move) {
		return toMove(board.getCoordinatesSystem(), move).toString();
	}

	@Override
	public String getBoardAsString() {
		return board==null ? "no position defined" : board.toString();
	}

	@Override
	public String getFEN() {
		return board==null ? null : FENUtils.to(board);
	}
}
