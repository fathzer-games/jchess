package com.fathzer.games.game;

import java.util.concurrent.Flow.Publisher;
import java.util.concurrent.Flow.Subscriber;
import java.util.concurrent.SubmissionPublisher;

import com.fathzer.jchess.Board;
import com.fathzer.jchess.Move;
import com.fathzer.jchess.pgn.PGNHeaders.TerminationCause;
import com.fathzer.games.Color;
import com.fathzer.games.Status;
import com.fathzer.games.clock.Clock;

import lombok.Getter;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class Game implements Runnable, Publisher<Game.OutcomingEvent> {
	/** A tagging interface that all events that can be sent to a game extend.
	 */
	public sealed interface IncomingEvent {
	}
	
	public static record MoveEvent(Move move) implements IncomingEvent {}
	public static record ResignationEvent(Color player) implements IncomingEvent {}
	public static record DrawProposal(Color player) implements IncomingEvent {}
	public static record DrawAcceptance(Color player, boolean accepted) implements IncomingEvent {}
	public static record TimeUpEvent() implements IncomingEvent {}
	public static record PauseEvent(boolean paused) implements IncomingEvent {}

	/** A tagging interface that all events sent by a game extend.
	 */
	public sealed interface OutcomingEvent {
	}

	public static record MoveMade(Move move) implements OutcomingEvent {}
	public static record GameEnded() implements OutcomingEvent {}
/*	
	private static class IncomingEventSubscriber extends BasicEventSubscriber<IncomingEvent>{
		private final Game game;

		private IncomingEventSubscriber(Game game) {
			super();
			this.game = game;
		}

		@Override
		public void onNext(IncomingEvent item) {
			game.doEvent(item);
			super.onNext(item);
		}
	}*/
	private Player white;
	private Player black;
	@Getter
	private Clock clock;
	@Getter
	private boolean paused;
	private boolean startClockAfterFirstMove = false;
	@Getter
	private GameHistory history;
//	private Queue<IncomingEvent> events;
	private SubmissionPublisher<OutcomingEvent> publisher;
	private SubmissionPublisher<IncomingEvent> events;

	public Game(Board<Move> board, Clock clock, Player white, Player black) {
		this.history = new GameHistory(board);
		this.clock = clock;
		if (clock!=null) {
			clock.pause();
		}
		this.paused = true;
//		events = new LinkedList<>();
		events = new SubmissionPublisher<>();
		publisher = new SubmissionPublisher<>(); 
	}
	
	private Player getPlayer(Color color) {
		return color==Color.WHITE ? white : black;
	}
	
	private Color getActiveColor() {
		return this.history.getBoard().getActiveColor();
	}
	
	
	@Override
	public void run() {
		events.subscribe(new BasicEventSubscriber<IncomingEvent>() {
			@Override
			public void onNext(IncomingEvent item) {
				doEvent(item);
				super.onNext(item);
			}
		});
		while (Status.PLAYING==this.getHistory().getStatus() || this.getHistory().getStatus()==null) {
			
//			synchronized(events) {
//				try {
//					log.trace("Waiting for incomming event");
//					events.wait();
//					IncomingEvent event = events.poll();
//					log.info("incomming event {}", event);
//					doEvent(event);
//				} catch (InterruptedException e) {
//					log.error("Thread {} was interrupted", Thread.currentThread(), e);
//					Thread.currentThread().interrupt();
//				}
//			}
		}
	}
	
	@Override
	public void subscribe(Subscriber<? super OutcomingEvent> subscriber) {
		publisher.subscribe(subscriber);
	}
	
	public synchronized void addEvent(IncomingEvent event) {
//		events.add(event);
//		events.notifyAll();
		events.submit(event);
	}
	
	void doEvent(IncomingEvent event) {
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
		if (status!=null) {
			if (event instanceof MoveEvent moveEvent) {
				log.info("Move {} is ignored because game status is {}", moveEvent.move(), status);
			} else {
				log.info("{} is ignored because game status is {}", event, status);
			}
		}
		return status==null;
	}
	
	private void doMove(Move move) {
		final Color playing = history.getBoard().getActiveColor();
		final boolean valid = history.add(move);
		if (!valid) {
			pause();
			this.getHistory().earlyEnd(playing==Color.WHITE?Status.BLACK_WON:Status.WHITE_WON, TerminationCause.RULES_INFRACTION);
		} else if (clock!=null) {
			clock.tap();
		}
	}
	
	private void doTimeUp() {
		pause();
		this.getHistory().earlyEnd(Color.WHITE==history.getBoard().getActiveColor()?Status.BLACK_WON:Status.WHITE_WON, TerminationCause.TIME_FORFEIT);
	}
	
	private void doResignation(Color player) {
		pause();
		this.getHistory().earlyEnd(Color.WHITE==history.getBoard().getActiveColor()?Status.BLACK_WON:Status.WHITE_WON, TerminationCause.TIME_FORFEIT);
	}

	public void setStartClockAfterFirstMove(boolean afterFirst) {
		this.startClockAfterFirstMove = afterFirst;
	}
	
	private boolean isFirstMove() {
		return history.getMoves().isEmpty();
	}
	
	public void start() {
		if (paused) {
			this.paused = false;
			if (clock!=null && !(isFirstMove() && startClockAfterFirstMove)) {
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
}
