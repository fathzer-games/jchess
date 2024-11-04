package com.fathzer.jchess.tournament;

import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.List;

import com.fathzer.games.Color;
import com.fathzer.games.Status;
import com.fathzer.games.MoveGenerator.MoveConfidence;
import com.fathzer.jchess.AbstractGameSession;
import com.fathzer.jchess.Game;
import com.fathzer.jchess.GameRecorder;
import com.fathzer.jchess.Move;
import com.fathzer.jchess.bot.Engine;
import com.fathzer.jchess.pgn.MoveAlgebraicNotationBuilder;
import com.fathzer.jchess.settings.Settings;

import lombok.extern.slf4j.Slf4j;

@Slf4j
public class TournamentGameSession extends AbstractGameSession<Void> implements Runnable {
	private boolean ended = false;
	private final List<Event> events;
	private static final MoveAlgebraicNotationBuilder ANB = new MoveAlgebraicNotationBuilder();
	
	private enum Event {NEXT_MOVE, GAME_ENDED}

	protected TournamentGameSession(Settings settings) {
		super(null, settings);
		this.events = new ArrayList<>();
	}

	@Override
	protected void nextMove() {
		final Color activeColor = game.getBoard().getActiveColor();
		final Engine engine = getEngine(activeColor);
		game.playEngine(engine, this::play, e -> log.error("Error while communicating with "+engine.getName()+" engine", e));
	}

	@Override
	protected void play(Game game, Move move) {
		log.debug("{} plays {}", getEngine(game.getBoard().getActiveColor()).getName(), ANB.get(game.getBoard(), move));
		//TODO Make game win if opponent makes an illegal move 
		game.onMove(move);
		game.getBoard().makeMove(move, MoveConfidence.UNSAFE);
		synchronized (events) {
			if (game.getBoard().getStatus().equals(Status.PLAYING)) {
				events.add(Event.NEXT_MOVE);
			} else {
				events.add(Event.GAME_ENDED);
			}
			events.notifyAll();
		}
	}
	
	@Override
	protected void onGameEnded() {
		PrintWriter writer = new PrintWriter(System.out);
		try {
			if (this.getScore().getGameCount()>1) {
				writer.println();
				writer.println("===========");
				writer.println();
			}
			GameRecorder.print(this.game.getHistory(), this.getSettings(), this.player1Color, (long) this.getScore().getGameCount(), writer);
		} catch (Exception e) {
			log.error("An error occured while writing pgn",e);
		}
	}

	@Override
	protected void onStateChanged(State old, State current) {
		log.debug("Game session's state changes from {} to {}!", old, current);
		super.onStateChanged(old, current);
		if (State.ENDED.equals(current)) {
			log.info("Game session ended");
			ended = true;
		}
	}

	@Override
	public void run() {
		log.info("Launching game session on thread {}", Thread.currentThread());
		start();
		while (!ended) {
			Event event;
			synchronized (events) {
				try {
					log.trace("Waiting for incomming event");
					events.wait();
				} catch (InterruptedException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
				event = events.remove(0);
				log.trace("incomming event {}", event);
			}
			if (event==Event.NEXT_MOVE) {
				nextMove();
			} else if (event==Event.GAME_ENDED) {
				endOfGame(game.getBoard().getStatus());
			}
			log.trace("Event processing complete");
		}
	}
}
