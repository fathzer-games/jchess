package com.fathzer.jchess;

import java.io.IOException;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;
import java.util.function.BiConsumer;
import java.util.function.Consumer;

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
	
	public void setStartClockAfterFirstMove(boolean afterFirst) {
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
	
	private Move getMove(Engine engine) throws IOException {
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
		return JChessUCIEngine.toMove(board, UCIMove.from(engine.getMove(params)));
	}

	public void playEngine(Engine engine, BiConsumer<Game, Move> moveConsumer, Consumer<Exception> errorManager) {
		EXECUTOR.execute(() -> {
			try {
				moveConsumer.accept(this, getMove(engine));
			} catch (IOException e) {
				errorManager.accept(e);
			}
		});
	}
	
	public void onMove(Move move) {
		if (clock!=null) {
			clock.tap();
		}
		history.add(move);
	}
}
