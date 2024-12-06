package com.fathzer.jchess.bot;

import java.io.IOException;
import java.util.Optional;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.atomic.AtomicReference;
import java.util.function.Consumer;

import com.fathzer.games.GameHistory;
import com.fathzer.games.clock.Clock;
import com.fathzer.games.clock.ClockSettings;
import com.fathzer.games.clock.ClockState;
import com.fathzer.games.clock.CountDownState;
import com.fathzer.games.game.Game;
import com.fathzer.games.game.Player;
import com.fathzer.games.util.UncheckedException;
import com.fathzer.games.util.exec.CustomThreadFactory;
import com.fathzer.jchess.Board;
import com.fathzer.jchess.CoordinatesSystem;
import com.fathzer.jchess.Move;
import com.fathzer.jchess.fen.FENUtils;
import com.fathzer.jchess.settings.Settings.Variant;
import com.fathzer.jchess.uci.JChessUCIEngine;
import com.fathzer.jchess.uci.UCIMove;

import lombok.extern.slf4j.Slf4j;

@Slf4j
public class EnginePlayer implements Player<Move, Board<Move>> {
	private static final CustomThreadFactory threadFactory = new CustomThreadFactory(new CustomThreadFactory.BasicThreadNameSupplier("Engine player"), true);
	private final ExecutorService moveWaiter = Executors.newSingleThreadExecutor(threadFactory);
	
	private final Engine engine;
	private AtomicReference<Future<Void>> currentSearch;
	private Variant variant;

	public EnginePlayer(Engine engine) {
		this.engine = engine;
		this.currentSearch = new AtomicReference<>();
		this.variant = Variant.STANDARD;
	}
	
	@Override
	public void onNewGame(Game<Move, Board<Move>> game) {
		log.debug("Engine {} received new game event for game id {}", getName(), game.getId());
		try {
			engine.newGame(variant);
		} catch (IOException e) {
			throw new UncheckedException(e);
		}
	}

	@Override
	public synchronized void requestMove(Game<Move, Board<Move>> game, Consumer<Move> callBack) {
		if (currentSearch.get()!=null) {
			throw new IllegalStateException(getName()+" is already searching for the best move");
		} else {
			this.currentSearch.set(moveWaiter.submit(() -> {
				log.debug("{} starts searching best move on game {}", getName(), game.getId());
				final Optional<Move> move;
				try {
					move = getMove(game);
					synchronized (EnginePlayer.this) {
						currentSearch.set(null);
					}
				} catch (Exception e) {
					log.error("An error occurred while searching for move, declare the engine dead");
					callBack.accept(null);
					return null;
				}
				if (move.isPresent()) {
					log.debug("{} returns a move on game {}", getName(), game.getId());
					callBack.accept(move.get());
				} else {
					log.debug("No time remaining for {} to compute best move on game {}", getName(), game.getId());
				}
				return null;
			}));
		}
	}
	
	@Override
	public void onEndGame(Game<Move, Board<Move>> game) {
		log.debug("Engine {} received end of game event for game {}", getName(), game.getId());
		final Future<Void> future = currentSearch.get();
		if (future!=null) {
			currentSearch.set(null);
			// Game is ended => End the search
			log.debug("Engine {} trying to stop pending move search", getName());
			try {
				engine.stop();
			} catch (IOException e) {
				log.error("Fail to send stop command to engine {}", getName(), e);
			}
			future.cancel(true);
			log.debug("Engine {} cancelled the internal task on pending move search", getName());
		}
	}

	private Optional<Move> getMove(Game<Move, Board<Move>> game) throws IOException {
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
		}
		if (params==null) {
			System.out.println("We are fucked"); //TODO
		}
		if (params.getRemainingMs()<0) {
			return Optional.empty();
		}
		return Optional.of(JChessUCIEngine.toMove(history.getBoard(), UCIMove.from(engine.getMove(params))));
	}
	
	public String getName() {
		return engine.getName();
	}
	
	public void setVariant(Variant variant) {
		this.variant = variant;
	}
}
