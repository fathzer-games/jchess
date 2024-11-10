package com.fathzer.games.game;

import java.io.IOException;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;

import com.fathzer.games.clock.Clock;
import com.fathzer.games.clock.ClockSettings;
import com.fathzer.games.clock.ClockState;
import com.fathzer.games.clock.CountDownState;
import com.fathzer.games.util.exec.CustomThreadFactory;
import com.fathzer.jchess.CoordinatesSystem;
import com.fathzer.jchess.Move;
import com.fathzer.jchess.bot.Engine;
import com.fathzer.jchess.fen.FENUtils;
import com.fathzer.jchess.uci.JChessUCIEngine;
import com.fathzer.jchess.uci.UCIMove;

import lombok.extern.slf4j.Slf4j;

@Slf4j
public class EnginePlayer implements Player {
	private final Executor executor; 
	private final Engine engine;

	public EnginePlayer(Engine engine) {
		this.executor = Executors.newSingleThreadExecutor(new CustomThreadFactory(new CustomThreadFactory.BasicThreadNameSupplier("Engine player"), true));
		this.engine = engine;
	}
	
	@Override
	public void requestMove(Game game) {
		// TODO Auto-generated method stub

	}

	private Move getMove(Game game) throws IOException {
		final GameHistory history = game.getHistory();
		final Clock clock = game.getClock(); 
		final CoordinatesSystem cs = history.getBoard().getCoordinatesSystem();
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
		return JChessUCIEngine.toMove(history.getBoard(), UCIMove.from(engine.getMove(params)));
	}
/*
	public void playEngine(Engine engine, BiConsumer<Game, Move> moveConsumer, Consumer<Exception> errorManager) {
		EXECUTOR.execute(() -> {
			try {
				moveConsumer.accept(this, getMove(engine));
			} catch (IOException e) {
				errorManager.accept(e);
			}
		});
	}
*/
}
