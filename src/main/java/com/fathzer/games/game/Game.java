package com.fathzer.games.game;

import java.util.Collections;
import java.util.LinkedList;
import java.util.List;
import java.util.concurrent.atomic.AtomicLong;
import java.util.function.BiConsumer;
import java.util.function.Consumer;

import com.fathzer.jchess.Board;
import com.fathzer.jchess.Move;
import com.fathzer.jchess.pgn.PGNHeaders.TerminationCause;
import com.fathzer.games.Color;
import com.fathzer.games.Status;
import com.fathzer.games.clock.Clock;

import lombok.Getter;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class Game implements Runnable {
	private static final AtomicLong GAME_ID_GENERATOR = new AtomicLong(); 
	
	/** A tagging interface that all events that can be sent to a game extend.
	 */
	public sealed interface IncomingEvent {}
	
	public static record MoveEvent(Move move) implements IncomingEvent {}
	public static record ResignationEvent(Color player) implements IncomingEvent {}
//	public static record DrawProposal(Color player) implements IncomingEvent {}
//	public static record DrawAcceptance(Color player, boolean accepted) implements IncomingEvent {}
//	public static record PauseEvent(boolean paused) implements IncomingEvent {}
	private static record TimeUpEvent() implements IncomingEvent {}

	/** A tagging interface that all events sent by a game extend.
	 */
	public sealed interface OutcomingEvent {}

	public static record MoveMade(Move move) implements OutcomingEvent {}
	public static record GameEnded() implements OutcomingEvent {}

	private final long id = GAME_ID_GENERATOR.incrementAndGet();
	private final Player white;
	private final Player black;
	@Getter
	private final Clock clock;
	@Getter
	private final GameHistory history;
	private final ItemPublisher<IncomingEvent> events;
	private final List<BiConsumer<Game, Move>> moveListeners;
	private final List<Consumer<Game>> endGameListeners;
	private boolean startClockAfterFirstMove = false;
	@Getter
	private boolean paused;

	/** Constructor.
	 * @param board The start board of a game.
	 * @param clock The clock used for the game (null if time allowed is infinite)
	 * @param white The white player
	 * @param black The black player
	 */
	public Game(Board<Move> board, Clock clock, Player white, Player black) {
		if (white==null || black==null) {
			throw new IllegalArgumentException("Players can't be null");
		}
		this.white = white;
		this.black = black;
		this.history = new GameHistory(board);
		this.clock = clock;
		if (clock!=null) {
			clock.addStatusListener(s -> addEvent(new TimeUpEvent()));
			clock.pause();
		}
		this.paused = true;
		events = new ItemPublisher<>();
		moveListeners = new LinkedList<>();
		endGameListeners = new LinkedList<>();
	}
	
	private Player getPlayer(Color color) {
		return color==Color.WHITE ? white : black;
	}
	
	private Color getActiveColor() {
		return this.history.getBoard().getActiveColor();
	}
	
	@Override
	public void run() {
		events.subscribe(this::doEvent);
		this.start();
		getPlayer(getActiveColor()).requestMove(this);
		events.run();
	}
	
	public void addMoveListener(BiConsumer<Game, Move> listener) {
		moveListeners.add(listener);
	}
	
	public void addEndGameListener(Consumer<Game> listener) {
		endGameListeners.add(listener);
	}

	public synchronized void addEvent(IncomingEvent event) {
		events.submit(Collections.singleton(event));
	}
	
	void doEvent(IncomingEvent event) {
		log.debug("Game {} Receives event {}", id, event);
		if (!checkAlive(event)) {
			return;
		}
		if (event instanceof MoveEvent moveEvent) {
			doMove(moveEvent.move());
		} else if (event instanceof TimeUpEvent) {
			doTimeUp();
		} else if (event instanceof ResignationEvent resignation) {
			doResignation(resignation.player());
		} else {
			throw new UnsupportedOperationException(event+" is not yet supported"); //TODO
		}
	}
	
	private boolean checkAlive(IncomingEvent event) {
		final Status status = history.getStatus();
		if (status!=null && status!=Status.PLAYING) {
			if (event instanceof MoveEvent moveEvent) {
				log.debug("Move {} is ignored because game status is {}", moveEvent.move(), status);
			} else {
				log.debug("{} is ignored because game status is {}", event, status);
			}
			return false;
		}
		return true;
	}
	
	private void doMove(Move move) {
		final Color playing = history.getBoard().getActiveColor();
		final boolean valid = history.add(move);
		if (!valid) {
			log.debug("Move {} is illegal. Declare the game won by rules infraction", move);
			this.getHistory().earlyEnd(playing==Color.WHITE?Status.BLACK_WON:Status.WHITE_WON, TerminationCause.RULES_INFRACTION);
			onEndGame();
			return;
		}
		log.debug("Move {} played by {}", move, playing);
		if (clock!=null) {
			clock.tap();
		}
		moveListeners.forEach(l -> l.accept(this, move));
		if (!isEnded()) {
			getPlayer(playing.opposite()).requestMove(this);
		} else {
			log.debug("Game is ended");
			onEndGame();
		}
	}
	
	private void onEndGame() {
		pause();
		events.close();
		endGameListeners.forEach(l -> l.accept(this));
	}
	
	private void doTimeUp() {
		this.getHistory().earlyEnd(Color.WHITE==history.getBoard().getActiveColor()?Status.BLACK_WON:Status.WHITE_WON, TerminationCause.TIME_FORFEIT);
		onEndGame();
	}
	
	private void doResignation(Color player) {
		this.getHistory().earlyEnd(Color.WHITE==history.getBoard().getActiveColor()?Status.BLACK_WON:Status.WHITE_WON, TerminationCause.TIME_FORFEIT);
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
}
