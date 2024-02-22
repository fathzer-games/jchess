package com.fathzer.jchess.swing;

import java.util.concurrent.Executor;
import java.util.concurrent.Executors;
import java.util.function.BiConsumer;

import com.fathzer.jchess.Board;
import com.fathzer.jchess.CoordinatesSystem;
import com.fathzer.jchess.GameHistory;
import com.fathzer.jchess.Move;
import com.fathzer.jchess.bot.Engine;
import com.fathzer.jchess.fen.FENUtils;
import com.fathzer.jchess.uci.JChessUCIEngine;
import com.fathzer.jchess.uci.UCIMove;
import com.fathzer.games.clock.Clock;
import com.fathzer.games.clock.ClockSettings;
import com.fathzer.games.clock.CountDownState;

import lombok.Getter;

public class Game {
	private static final Executor EXECUTOR = Executors.newSingleThreadExecutor(r -> {
	    Thread t = Executors.defaultThreadFactory().newThread(r);
	    t.setDaemon(true);
	    return t;
	});
	
	private class EngineTurn implements Runnable {
		private final Engine engine;
		private final BiConsumer<Game, Move> moveConsumer;
		
		private EngineTurn(Engine engine, BiConsumer<Game,Move> moveConsumer) {
			this.moveConsumer = moveConsumer;
			this.engine = engine;
		}
		
		@Override
		public void run() {
			final CoordinatesSystem cs = board.getCoordinatesSystem();
			engine.setPosition(FENUtils.to(history.getStartBoard()), history.getMoves().stream().map(m -> JChessUCIEngine.toUCIMove(cs, m)).
					map(UCIMove::toString).toList());
			final CountDownState params;
			if (clock==null) {
				params = null;
			} else {
				final long remainingTime = clock.getRemaining(clock.getPlaying());
				final ClockSettings clockSettings = clock.getCurrentSettings(clock.getPlaying());
				final int increment = clockSettings.getIncrement()>0 ? clockSettings.getIncrement()/clockSettings.getMovesNumberBeforeIncrement() : 0;
				final int movesToGo = clock.getRemainingMovesBeforeNext(clock.getPlaying());
				params = new CountDownState(remainingTime, increment, movesToGo);
			}
			Move move = JChessUCIEngine.toMove(board, UCIMove.from(engine.play(params)));
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

	public Game(Board<Move> board, Clock clock) {
		this.board = board;
		this.history = new GameHistory(board);
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

	public void playEngine(Engine engine, BiConsumer<Game, Move> moveConsumer) {
		EXECUTOR.execute(new EngineTurn(engine, moveConsumer));
	}
	
	public void onMove(Move move) {
		if (clock!=null) {
			clock.tap();
		}
		history.add(move);
	}
}
