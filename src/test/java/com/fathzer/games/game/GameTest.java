package com.fathzer.games.game;

import static org.junit.jupiter.api.Assertions.*;

import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;
import java.util.Queue;
import java.util.function.BiConsumer;
import java.util.function.Consumer;

import org.junit.jupiter.api.Test;
import org.slf4j.LoggerFactory;

import com.fathzer.games.Color;
import com.fathzer.games.Status;
import com.fathzer.games.clock.Clock;
import com.fathzer.games.clock.ClockSettings;
import com.fathzer.jchess.Board;
import com.fathzer.jchess.CoordinatesSystem;
import com.fathzer.jchess.Move;
import com.fathzer.jchess.generic.BasicMove;
import com.fathzer.jchess.pgn.PGNHeaders.TerminationCause;
import com.fathzer.jchess.settings.Settings.Variant;

class GameTest {
	private static class EventCounter implements Consumer<Game<Move, Board<Move>>>, BiConsumer<Game<Move, Board<Move>>, Move> {
		private int moveCounter, endCounter;
		@Override
		public void accept(Game<Move, Board<Move>> game, Move move) {
			moveCounter++;
		}

		@Override
		public void accept(Game<Move, Board<Move>> game) {
			endCounter++;
		}
	}

	@Test
	void testNoClock() throws InterruptedException {
		LoggerFactory.getLogger(GameTest.class).debug("Here we are");
		
		Board<Move> board = Variant.STANDARD.getRules().apply(null);
		CoordinatesSystem cs = board.getCoordinatesSystem();
		TestPlayer white = new TestPlayer(Color.WHITE, Arrays.asList(new BasicMove(cs.getIndex("e2"), cs.getIndex("e4")), new BasicMove(cs.getIndex("f1"), cs.getIndex("c4")), new BasicMove(cs.getIndex("d1"), cs.getIndex("h5")), new BasicMove(cs.getIndex("h5"), cs.getIndex("f7"))));
		TestPlayer black = new TestPlayer(Color.BLACK, Arrays.asList(new BasicMove(cs.getIndex("e7"), cs.getIndex("e5")), new BasicMove(cs.getIndex("f8"), cs.getIndex("c5")), new BasicMove(cs.getIndex("b8"), cs.getIndex("c6"))));
		final Game<Move, Board<Move>> game = new Game<>(board, null, white, black);
		assertThrows(IllegalStateException.class, () -> game.setStartClockAfterFirstMove(true));
		assertTrue(game.isPaused());
		final EventCounter counter = new EventCounter();
		game.addMoveListener(counter);
		game.addEndGameListener(counter);
		Thread gameThread = new Thread(game);
		gameThread.start();
		gameThread.join();
		assertTrue(game.isEnded());
		assertEquals(Status.WHITE_WON, game.getHistory().getStatus());
		assertEquals(TerminationCause.NORMAL, game.getHistory().getTerminationCause());
		assertFalse(white.errorOccured);
		assertFalse(black.errorOccured);
		assertEquals(1, counter.endCounter);
		assertEquals(7, counter.moveCounter);
	}

	
	@Test
	void test() throws InterruptedException {
		Board<Move> board = Variant.STANDARD.getRules().apply(null);
		CoordinatesSystem cs = board.getCoordinatesSystem();
		TestPlayer white = new TestPlayer(Color.WHITE, Arrays.asList(new BasicMove(cs.getIndex("e2"), cs.getIndex("e4")), new BasicMove(cs.getIndex("f1"), cs.getIndex("c4")), new BasicMove(cs.getIndex("d1"), cs.getIndex("h5")), new BasicMove(cs.getIndex("h5"), cs.getIndex("f7"))));
		TestPlayer black = new TestPlayer(Color.BLACK, Arrays.asList(new BasicMove(cs.getIndex("e7"), cs.getIndex("e5")), new BasicMove(cs.getIndex("f8"), cs.getIndex("c5")), new BasicMove(cs.getIndex("b8"), cs.getIndex("c6"))));
		black.thinkTime = 550;
		
		ClockSettings settings = new ClockSettings(1);
		Game<Move, Board<Move>> game = new Game<>(board, new Clock(settings), white, black);
		game.setStartClockAfterFirstMove(true);
		final EventCounter counter = new EventCounter();
		game.addMoveListener(counter);
		game.addEndGameListener(counter);
		assertTrue(game.isPaused());
		Thread gameThread = new Thread(game);
		gameThread.start();
		gameThread.join();
		assertTrue(game.isEnded());
		assertEquals(Status.WHITE_WON, game.getHistory().getStatus());
		assertEquals(TerminationCause.TIME_FORFEIT, game.getHistory().getTerminationCause());
		white.join();
		black.join();
		assertFalse(white.errorOccured);
		assertFalse(black.errorOccured);
		assertEquals(1, counter.endCounter);
		assertEquals(3, counter.moveCounter);
	}
	
	
	private static final class TestPlayer implements Player<Move, Board<Move>> {
		private final Queue<Move> moves;
		private final Color color;
		private boolean errorOccured;
		private Thread requestThread;
		private long thinkTime = 0;
		
		TestPlayer(Color color, List<Move> moves) {
			this.color = color;
			this.moves = new LinkedList<>(moves);
		}

		@Override
		public void requestMove(Game<Move, Board<Move>> game, Consumer<Move> callBack) {
			requestThread = new Thread(() -> {
				try {
					if (thinkTime>0) {
						Thread.sleep(thinkTime);
					}
					callBack.accept(moves.poll());
				} catch (InterruptedException e) {
					e.printStackTrace();
					Thread.currentThread().interrupt();
				} catch (Throwable e) {
					e.printStackTrace();
					errorOccured = true;
				}
			});
			requestThread.start();
		}
		
		private void join() throws InterruptedException {
			if (requestThread!=null) {
				requestThread.join();
			}
		}
	}

}
