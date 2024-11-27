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
import com.fathzer.games.Color;
import com.fathzer.games.GameHistory;
import com.fathzer.games.GameHistory.TerminationCause;
import com.fathzer.games.Status;
import com.fathzer.games.clock.Clock;
import com.fathzer.games.clock.ClockSettings;
import com.fathzer.games.clock.ClockState;
import com.fathzer.games.clock.CountDownState;
import com.fathzer.games.util.exec.CustomThreadFactory;

import lombok.Getter;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class Game {
	private static final Executor EXECUTOR = Executors.newSingleThreadExecutor(new CustomThreadFactory(new CustomThreadFactory.BasicThreadNameSupplier("Game thread"), true));
	
	@Getter
	private boolean firstMove;
	@Getter
	private Clock clock;
	@Getter
	private boolean paused;
	private boolean startClockAfterFirstMove = false;
	@Getter
	private GameHistory<Move, Board<Move>> history;

	public Game(Board<Move> board, Clock clock) {
		this.history = new GameHistory<>(board);
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
		final CoordinatesSystem cs = this.history.getBoard().getCoordinatesSystem();
		engine.setPosition(FENUtils.to(history.getStartBoard()), history.getMoves().stream().map(m -> JChessUCIEngine.toUCIMove(cs, m)).
				map(UCIMove::toString).toList());
		final CountDownState params;
		if (clock==null || clock.getState()==ClockState.ENDED) {
			params = null;
		} else {
			final long remainingTime = clock.getRemaining(clock.getPlaying());
			final ClockSettings clockSettings = clock.getCurrentSettings(clock.getPlaying());
			final int increment = clockSettings.getIncrement()>0 ? clockSettings.getIncrement()*1000/clockSettings.getMovesNumberBeforeIncrement() : 0;
			final int movesToGo = clock.getRemainingMovesBeforeNext(clock.getPlaying());
			params = new CountDownState(remainingTime, increment, movesToGo);
			//TODO
			log.info("engine "+engine.getName()+" will wait "+(remainingTime+500)+" to have time forfeit");
			try {
				Thread.sleep(remainingTime+500);
			} catch (InterruptedException e) {
				log.error("Interrupted", e);
				Thread.currentThread().interrupt();
			}
			//End of TODO
		}
		return JChessUCIEngine.toMove(this.history.getBoard(), UCIMove.from(engine.getMove(params)));
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
		synchronized (history) {
			final Status status = history.getStatus();
			if (status!=null) {
				// If game is already ended, ignore the move (probably, the game was ended by a time forfeit).
				log.info("Move {} is ignored because game status is {}", move, status); //TODO
				return;
			}
			final Color playing = history.getBoard().getActiveColor();
			final boolean valid = history.add(move);
			if (!valid) {
				pause();
				this.getHistory().earlyEnd(playing==Color.WHITE?Status.BLACK_WON:Status.WHITE_WON, TerminationCause.RULES_INFRACTION);
			} else if (clock!=null) {
				clock.tap();
			}
		}
	}
}
