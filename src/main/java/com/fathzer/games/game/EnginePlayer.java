package com.fathzer.games.game;

import java.io.IOException;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.function.Consumer;

import com.fathzer.games.clock.Clock;
import com.fathzer.games.clock.ClockSettings;
import com.fathzer.games.clock.ClockState;
import com.fathzer.games.clock.CountDownState;
import com.fathzer.games.util.exec.CustomThreadFactory;
import com.fathzer.jchess.Board;
import com.fathzer.jchess.CoordinatesSystem;
import com.fathzer.jchess.Move;
import com.fathzer.jchess.bot.Engine;
import com.fathzer.jchess.fen.FENUtils;
import com.fathzer.jchess.uci.JChessUCIEngine;
import com.fathzer.jchess.uci.UCIMove;

import lombok.extern.slf4j.Slf4j;

@Slf4j
public class EnginePlayer implements Player<Move, Board<Move>> {
	private static final CustomThreadFactory threadFactory = new CustomThreadFactory(new CustomThreadFactory.BasicThreadNameSupplier("Engine player"), true);

	private final Engine engine;
	@SuppressWarnings("java:S3077")
	private volatile Future<Void> currentSearch;

	public EnginePlayer(Engine engine) {
		this.engine = engine;
	}
	
	@Override
	public synchronized void requestMove(Game<Move, Board<Move>> game, Consumer<Move> callBack) {
		if (currentSearch!=null) {
			throw new IllegalStateException();
		} else {
			this.currentSearch = Executors.newSingleThreadExecutor(threadFactory).submit(() -> {
				synchronized (EnginePlayer.this) {
					currentSearch = null;
					callBack.accept(getMove(game));
				}
				return null;
			});
		}
	}
	
	@Override
	public synchronized void cancel(Game game) {
		if (game.isEnded() && currentSearch!=null) {
			// Game is ended => End the search
			//TODO, currently, nothing allows to stop a search
			log.warn("Currently no way to stop the search");
			currentSearch.cancel(false);
			currentSearch = null;
		}
	}

	private Move getMove(Game<Move, Board<Move>> game) throws IOException {
		final GameHistory<Move, Board<Move>> history = game.getHistory();
		final CoordinatesSystem cs = history.getBoard().getCoordinatesSystem();
		engine.setPosition(FENUtils.to(history.getStartBoard()), history.getMoves().stream().map(m -> JChessUCIEngine.toUCIMove(cs, m)).
				map(UCIMove::toString).toList());
		final CountDownState params;
		final Clock clock = game.getClock(); 
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
		return JChessUCIEngine.toMove(history.getBoard(), UCIMove.from(engine.getMove(params)));
	}
}
