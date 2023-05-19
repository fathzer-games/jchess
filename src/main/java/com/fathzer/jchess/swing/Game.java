package com.fathzer.jchess.swing;

import java.util.concurrent.Executor;
import java.util.concurrent.Executors;
import java.util.function.BiConsumer;
import java.util.function.Function;

import com.fathzer.jchess.Board;
import com.fathzer.jchess.ChessRules;
import com.fathzer.jchess.GameHistory;
import com.fathzer.jchess.Move;
import com.fathzer.games.clock.Clock;

import lombok.Getter;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class Game {
	private static final Executor EXECUTOR = Executors.newSingleThreadExecutor((r) -> {
	    Thread t = Executors.defaultThreadFactory().newThread(r);
	    t.setDaemon(true);
	    return t;
	});
	
	private class EngineTurn implements Runnable {
		private final Function<Board<Move>, Move> engine;
		private final BiConsumer<Game, Move> moveConsumer;
		
		private EngineTurn(Function<Board<Move>, Move> engine, BiConsumer<Game,Move> moveConsumer) {
			this.moveConsumer = moveConsumer;
			this.engine = engine;
		}
		
		@Override
		public void run() {
			log.info("Engine starts searching move for {}",board.getActiveColor());
			Move move = engine.apply(board);
			log.info("Engine has choosed move {} for {}",move, board.getActiveColor());
			moveConsumer.accept(Game.this, move);
		}
	}
	
	@Getter
	private final Board<Move> board;
	@Getter
	private boolean firstMove;
	@Getter
	private Clock clock;
	@Getter
	private boolean paused;
	private boolean startClockAfterFirstMove = false;
	@Getter
	private GameHistory history;

	public Game(Board<Move> board, ChessRules rules, Clock clock) {
		this.board = board;
		this.history = new GameHistory(rules);
		this.firstMove = true;
		this.clock = clock;
		if (clock!=null) {
			clock.pause();
		}
		this.paused = true;
	}
	
	void setStartClockAfterFirstMove(boolean afterFirst) {
		this.startClockAfterFirstMove = afterFirst;
	}
	
	public void start() {
		if (paused) {
			this.paused = false;
			if (clock!=null && !(firstMove && startClockAfterFirstMove)) {
				this.clock.tap();
			}
		}
	}
	
	public void pause() {
		if (!paused) {
			this.paused = true;
			if (clock!=null) {
				this.clock.pause();
			}
		}
	}

	public void playEngine(Function<Board<Move>, Move> engine, BiConsumer<Game, Move> moveConsumer) {
		EXECUTOR.execute(new EngineTurn(engine, moveConsumer));
	}
	
	public void onMove(Move move) {
		if (clock!=null) {
			clock.tap();
		}
		history.add(move);
	}
}
