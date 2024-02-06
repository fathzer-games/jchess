package com.fathzer.jchess.uci;

import java.util.Arrays;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.Optional;

import com.fathzer.games.ai.time.BasicTimeManager;
import com.fathzer.games.ai.transposition.SizeUnit;
import com.fathzer.games.ai.transposition.TranspositionTable;
import com.fathzer.games.perft.TestableMoveGeneratorBuilder;
import com.fathzer.games.util.PhysicalCores;
import com.fathzer.jchess.Board;
import com.fathzer.jchess.CoordinatesSystem;
import com.fathzer.jchess.Move;
import com.fathzer.jchess.Piece;
import com.fathzer.jchess.ai.JChessEngine;
import com.fathzer.jchess.ai.TT;
import com.fathzer.jchess.ai.evaluator.NaiveEvaluator;
import com.fathzer.jchess.ai.evaluator.SimplifiedEvaluator;
import com.fathzer.jchess.fen.FENUtils;
import com.fathzer.jchess.generic.BasicMove;
import com.fathzer.jchess.lichess.DefaultOpenings;
import com.fathzer.jchess.time.VuckovicSolakOracle;
import com.fathzer.jchess.uci.extended.Displayable;
import com.fathzer.jchess.uci.helper.AbstractEngine;
import com.fathzer.jchess.uci.helper.EvaluatorConfiguration;
import com.fathzer.jchess.uci.option.ComboOption;
import com.fathzer.jchess.uci.option.Option;

public class JChessUCIEngine extends AbstractEngine<Move, Board<Move>> implements TestableMoveGeneratorBuilder<Move, Board<Move>>, Displayable {
	private static final EvaluatorConfiguration<Move, Board<Move>> SIMPLIFIED_EVAL_CONFIG = new EvaluatorConfiguration<>("simplified",SimplifiedEvaluator::new);
	private static final EvaluatorConfiguration<Move, Board<Move>> NAIVE_EVAL_CONFIG = new EvaluatorConfiguration<>("naive",NaiveEvaluator::new);
	private static final List<EvaluatorConfiguration<Move, Board<Move>>> EVALUATORS = Arrays.asList(SIMPLIFIED_EVAL_CONFIG, NAIVE_EVAL_CONFIG);
	private static final BasicTimeManager<Board<Move>> TIME_MANAGER = new BasicTimeManager<>(VuckovicSolakOracle.INSTANCE);

	private static final int SILLY_LEVEL_DEPTH = 4;
	private static final int AVERAGE_LEVEL_DEPTH = 6;
	private static final int BEST_LEVEL_DEPTH = 14;
	
	private static final String BEST_LEVEL = "best";
	private static final String AVERAGE_LEVEL = "average";
	private static final String SILLY_LEVEL = "silly";
	
	public JChessUCIEngine() {
		super (new JChessEngine(EVALUATORS.get(0).getBuilder(), AVERAGE_LEVEL_DEPTH), TIME_MANAGER);
		engine.getDeepeningPolicy().setDeepenOnForced(false);
		setEvaluators(EVALUATORS);
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
	public boolean hasOwnBook() {
		return true;
	}

	@Override
	public void setOwnBook(boolean activate) {
		engine.setOpenings(activate?DefaultOpenings.INSTANCE:null);
	}

	@Override
	protected TranspositionTable<Move> buildTranspositionTable(int sizeInMB) {
		return new TT(sizeInMB, SizeUnit.MB);
	}

	@Override
	public List<Option<?>> getOptions() {
		final List<Option<?>> options = super.getOptions();
		options.add(new ComboOption("level", this::setLevel, AVERAGE_LEVEL, new LinkedHashSet<>(Arrays.asList(SILLY_LEVEL, AVERAGE_LEVEL, BEST_LEVEL))));
		return options;
	}
	
	private void setLevel(String level) {
		final int depth;
		final int threads;
		final EvaluatorConfiguration<Move, Board<Move>> evaluation;  
		final boolean hasMoreThan1Core = PhysicalCores.count()>=2;
		if (SILLY_LEVEL.equals(level)) {
			depth = SILLY_LEVEL_DEPTH;
			evaluation = NAIVE_EVAL_CONFIG;
			threads = 1;
		} else if (AVERAGE_LEVEL.equals(level)) {
			depth = AVERAGE_LEVEL_DEPTH;
			evaluation = SIMPLIFIED_EVAL_CONFIG;
			threads = 1;
		} else if (BEST_LEVEL.equals(level)) {
			engine.getDeepeningPolicy().setMaxTime(30000);
			depth = BEST_LEVEL_DEPTH;
			evaluation = SIMPLIFIED_EVAL_CONFIG;
			threads = hasMoreThan1Core ? 2 : 1;
		} else {
			throw new IllegalArgumentException();
		}
		engine.setParallelism(threads);
		engine.getDeepeningPolicy().setDepth(depth);
		engine.setEvaluatorSupplier(evaluation.getBuilder());
	}
	
	@Override
	public boolean isChess960Supported() {
		return true;
	}
	
	@Override
	protected Move toMove(UCIMove move) {
		return toMove(board, move);
	}

	public static Move toMove(Board<Move> board, UCIMove move) {
		final CoordinatesSystem cs = board.getCoordinatesSystem();
		final int from = cs.getIndex(move.getFrom());
		final int to = cs.getIndex(move.getTo());
		String promotion = move.getPromotion();
		if (promotion!=null && board.isWhiteToMove()) {
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
	
	public static UCIMove toUCIMove(CoordinatesSystem cs, Move move) {
		final String promotion = move.getPromotion()==null ? null : move.getPromotion().getNotation().toLowerCase();
		return new UCIMove(cs.getAlgebraicNotation(move.getFrom()), cs.getAlgebraicNotation(move.getTo()), promotion);
	}

	@Override
	public UCIMove toUCI(Move move) {
		return toUCIMove(board.getCoordinatesSystem(), move);
	}

	@Override
	public void setStartPosition(String fen) {
		board = FENUtils.from(fen);
		board.setMoveComparatorBuilder(((JChessEngine)engine).getMoveComparatorSupplier());
	}
	
	@Override
	public Board<Move> fromFEN(String fen) {
		return FENUtils.from(fen);
	}

	@Override
	public String getFEN() {
		return board==null ? null : FENUtils.to(board);
	}
}
