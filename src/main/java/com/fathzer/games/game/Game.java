package com.fathzer.games.game;

import java.util.Arrays;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;
import java.util.Objects;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;
import java.util.concurrent.atomic.AtomicLong;
import java.util.function.BiConsumer;
import java.util.function.Consumer;

import com.fathzer.games.Color;
import com.fathzer.games.GameHistory;
import com.fathzer.games.GameHistory.TerminationCause;
import com.fathzer.games.MoveGenerator;
import com.fathzer.games.Status;
import com.fathzer.games.clock.Clock;
import com.fathzer.games.util.exec.CustomThreadFactory;
import com.fathzer.games.util.exec.ItemPublisher;

import lombok.Getter;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class Game<M,B extends MoveGenerator<M>> implements Runnable {
	private static final AtomicLong GAME_ID_GENERATOR = new AtomicLong(); 
	
	/** A tagging interface that all events that can be sent to a game extend.
	 */
	private sealed interface IncomingEvent<T> {}
	
	private static record MoveEvent<T>(T move) implements IncomingEvent<T> {}
	private static record ResignationEvent<T>(Color color) implements IncomingEvent<T> {}
	private static record DrawProposal<T>(Color color) implements IncomingEvent<T> {}
	private static record DrawAcceptance<T>(Color color, boolean accepted) implements IncomingEvent<T> {}
	private static record TimeUpEvent<T>() implements IncomingEvent<T> {}

	private final long id = GAME_ID_GENERATOR.incrementAndGet();
	private final Player<M,B> white;
	private final Player<M,B> black;
	@Getter
	private final Clock clock;
	@Getter
	private final GameHistory<M, B> history;
	private final ItemPublisher<IncomingEvent<M>> events;
	private final List<BiConsumer<Game<M, B>, M>> moveListeners;
	private final List<Consumer<Game<M,B>>> endGameListeners;
	private boolean startClockAfterFirstMove = false;
	@Getter
	private boolean paused;

	/** Constructor.
	 * @param board The start board of a game.
	 * @param clock The clock used for the game (null if time allowed is infinite)
	 * @param white The white player
	 * @param black The black player
	 */
	public Game(B board, Clock clock, Player<M, B> white, Player<M,B> black) {
		if (white==null || black==null) {
			throw new IllegalArgumentException("Players can't be null");
		}
		this.white = white;
		white.setResignationMethod(this, () -> addEvent(new ResignationEvent<>(Color.WHITE)));
		white.setDrawRequestMethod(this, () -> addEvent(new DrawProposal<>(Color.WHITE)));
		this.black = black;
		black.setResignationMethod(this, () -> addEvent(new ResignationEvent<>(Color.BLACK)));
		black.setDrawRequestMethod(this, () -> addEvent(new DrawProposal<>(Color.BLACK)));
		this.history = new GameHistory<>(board);
		this.clock = clock;
		if (clock!=null) {
			clock.addStatusListener(s -> addEvent(new TimeUpEvent<>()));
			clock.pause();
		}
		this.paused = true;
		events = new ItemPublisher<>();
		moveListeners = new LinkedList<>();
		endGameListeners = new LinkedList<>();
	}
	
	private static record PlayerFuture<M,B extends MoveGenerator<M>>(Player<M, B> player, Future<Void> future) {
		private void check() {
			try {
				future.get(5, TimeUnit.SECONDS);
			} catch (InterruptedException e) {
				Thread.currentThread().interrupt();
			} catch (ExecutionException | TimeoutException e) {
				throw new UnresponsivePlayerException(player);
			}
		}
	}

	private void checkPlayerReadiness(List<Player<M, B>> players) {
		final ExecutorService exec = Executors.newCachedThreadPool(new CustomThreadFactory(new CustomThreadFactory.BasicThreadNameSupplier("Player readiness waiter"), true));
		try {
			players.stream().map(p -> new PlayerFuture<>(p, exec.submit(() -> {p.onNewGame(this); return null;}))).forEach(pf -> pf.check());
		} finally {
			exec.shutdown();
		}
	}
	
	private Player<M,B> getPlayer(Color color) {
		return color==Color.WHITE ? white : black;
	}
	
	private Color getActiveColor() {
		return this.history.getBoard().isWhiteToMove() ? Color.WHITE : Color.BLACK;
	}
	
	@Override
	public void run() {
		events.subscribe(this::doEvent);
		checkPlayerReadiness(Arrays.asList(white, black));
		this.start();
		requestMove(getActiveColor());
		events.run();
	}
	
	private void requestMove(Color color) {
		getPlayer(color).requestMove(this, m -> this.addEvent(new MoveEvent<>(m)));
	}
	
	public void addMoveListener(BiConsumer<Game<M, B>, M> listener) {
		moveListeners.add(listener);
	}
	
	public void addEndGameListener(Consumer<Game<M, B>> listener) {
		endGameListeners.add(listener);
	}

	public synchronized void addEvent(IncomingEvent<M> event) {
		events.submit(Collections.singleton(event));
	}
	
	void doEvent(IncomingEvent<M> event) {
		log.debug("Game {} Receives event {}", id, event);
		if (!checkAlive(event)) {
			return;
		}
		if (event instanceof MoveEvent<M> moveEvent) {
			doMove(moveEvent.move());
		} else if (event instanceof TimeUpEvent) {
			doTimeUp();
		} else if (event instanceof ResignationEvent<M> resignation) {
			doResignation(resignation.color());
		} else {
			throw new UnsupportedOperationException(event+" is not yet supported"); //TODO
		}
	}
	
	private boolean checkAlive(IncomingEvent<M> event) {
		final Status status = history.getStatus();
		if (status!=null && status!=Status.PLAYING) {
			if (event instanceof MoveEvent<M> moveEvent) {
				log.debug("Move {} is ignored because game status is {}", moveEvent.move(), status);
			} else {
				log.debug("{} is ignored because game status is {}", event, status);
			}
			return false;
		}
		return true;
	}
	
	private void doMove(M move) {
		final Color playing = getActiveColor();
		final boolean valid = history.add(move);
		if (!valid) {
			log.debug("Move {} is illegal. Declare the game won by rules infraction", move);
			this.getHistory().earlyEnd(playing==Color.WHITE?Status.BLACK_WON:Status.WHITE_WON, TerminationCause.RULES_INFRACTION);
			onEndGame();
			return;
		}
		if (clock!=null) {
			// Stops the clock because listeners can be not as fast as expected
			clock.pause();
		}
		log.debug("Move {} played by {}", move, playing);
		moveListeners.forEach(l -> l.accept(this, move));
		if (!isEnded()) {
			if (clock!=null) {
				// Restarts clock
				clock.tap();
				// Change player
				clock.tap();
			}
			requestMove(playing.opposite());
		} else {
			onEndGame();
		}
	}
	
	private void onEndGame() {
		log.debug("Game is ended");
		pause();
		events.close();
		white.onEndGame(this);
		black.onEndGame(this);
		endGameListeners.forEach(l -> l.accept(this));
	}
	
	private void doTimeUp() {
		this.getHistory().earlyEnd(Color.WHITE==getActiveColor()?Status.BLACK_WON:Status.WHITE_WON, TerminationCause.TIME_FORFEIT);
		onEndGame();
	}
	
	private void doResignation(Color player) {
		this.getHistory().earlyEnd(Color.WHITE==player?Status.BLACK_WON:Status.WHITE_WON, TerminationCause.ABANDONED);
		onEndGame();
	}

	public void setStartClockAfterFirstMove(boolean afterFirst) {
		if (clock==null) {
			throw new IllegalStateException("This game has no clock");
		}
		this.startClockAfterFirstMove = afterFirst;
		this.clock.withStartingColor(afterFirst ? Color.BLACK : Color.WHITE);
	}
	
	private boolean isFirstMove() {
		return history.getMoves().isEmpty();
	}
	
	private void start() {
		if (paused) {
			this.paused = false;
			if (clock!=null && !(isFirstMove() && startClockAfterFirstMove)) {
				this.clock.tap();
			}
		}
	}
	
	private void pause() {
		if (!paused) {
			this.paused = true;
			if (clock!=null) {
				this.clock.pause();
			}
		}
	}
	
	public boolean isEnded() {
		final Status status = this.getHistory().getStatus();
		return status!=null && status!=Status.PLAYING;
	}

	/** Gets the game id.
	 * @return a long that identifies this game (no other game can have the same id)
	 */
	public long getId() {
		return id;
	}

	@Override
	public int hashCode() {
		return Objects.hash(id);
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj) {
			return true;
		}
		if (obj == null) {
			return false;
		}
		if (getClass() != obj.getClass()) {
			return false;
		}
		@SuppressWarnings("rawtypes")
		Game other = (Game) obj;
		return id == other.id;
	}
	
	
}
