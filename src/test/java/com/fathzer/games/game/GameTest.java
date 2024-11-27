package com.fathzer.games.game;

import static org.junit.jupiter.api.Assertions.*;

import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;
import java.util.Queue;
import java.util.function.BiConsumer;
import java.util.function.Consumer;

import org.junit.jupiter.api.Test;

import com.fathzer.games.Color;
import com.fathzer.games.GameHistory.TerminationCause;
import com.fathzer.games.Status;
import com.fathzer.games.clock.Clock;
import com.fathzer.games.clock.ClockSettings;
import com.fathzer.jchess.Board;
import com.fathzer.jchess.CoordinatesSystem;
import com.fathzer.jchess.Move;
import com.fathzer.jchess.generic.BasicMove;
import com.fathzer.jchess.settings.Settings.Variant;

class GameTest {
	private static final CoordinatesSystem CS;
	
	static {
		Board<Move> board = Variant.STANDARD.getRules().apply(null);
		CS = board.getCoordinatesSystem();
	}
	
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
	
	private static MoveAction mv(String from, String to) {
		return new MoveAction(new BasicMove(CS.getIndex(from), CS.getIndex(to)));
	}
	
	@Test
	void testNoClock() throws InterruptedException {
		Board<Move> board = Variant.STANDARD.getRules().apply(null);
		TestPlayer white = new TestPlayer(Color.WHITE, Arrays.asList(mv("e2","e4"), mv("f1","c4"), mv("d1","h5"), mv("h5","f7")));
		TestPlayer black = new TestPlayer(Color.BLACK, Arrays.asList(mv("e7","e5"), mv("f8","c5"), mv("b8", "c6")));
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
		assertFalse(white.errorOccurred);
		assertFalse(black.errorOccurred);
		assertEquals(1, counter.endCounter);
		assertEquals(7, counter.moveCounter);
	}

	@Test
	void testResignation() throws InterruptedException {
		Board<Move> board = Variant.STANDARD.getRules().apply(null);
		TestPlayer white = new TestPlayer(Color.WHITE, Arrays.asList(mv("e2","e4"), mv("f1","c4"), new ResignAction()));
		TestPlayer black = new TestPlayer(Color.BLACK, Arrays.asList(mv("e7","e5"), mv("f8","c5"), mv("b8", "c6")));
		final Game<Move, Board<Move>> game = new Game<>(board, null, white, black);
		final EventCounter counter = new EventCounter();
		game.addMoveListener(counter);
		game.addEndGameListener(counter);
		Thread gameThread = new Thread(game);
		gameThread.start();
		gameThread.join();
		assertTrue(game.isEnded());
		assertEquals(Status.BLACK_WON, game.getHistory().getStatus());
		assertEquals(TerminationCause.ABANDONED, game.getHistory().getTerminationCause());
		assertFalse(white.errorOccurred);
		assertFalse(black.errorOccurred);
		assertEquals(1, counter.endCounter);
		assertEquals(4, counter.moveCounter);
	}
	
	
	@Test
	void testDrawNegociation() throws InterruptedException {
		fail("Not yet implemented");
	}

	@Test
	void testTimeForfeit() throws InterruptedException {
		Board<Move> board = Variant.STANDARD.getRules().apply(null);
		TestPlayer white = new TestPlayer(Color.WHITE, Arrays.asList(mv("e2","e4"), mv("f1","c4"), mv("d1","h5"), mv("h5","f7")));
		TestPlayer black = new TestPlayer(Color.BLACK, Arrays.asList(mv("e7","e5"), mv("f8","c5"), mv("b8", "c6")));
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
		assertFalse(white.errorOccurred);
		assertFalse(black.errorOccurred);
		assertEquals(1, counter.endCounter);
		assertEquals(3, counter.moveCounter);
	}
	
	private sealed interface Action {}
	private record MoveAction(Move move) implements Action{}
	private record ResignAction() implements Action{}
	
	private static final class TestPlayer implements Player<Move, Board<Move>> {
		private final Queue<Action> actions;
		private final Color color;
		private boolean errorOccurred;
		private Thread requestThread;
		private long thinkTime = 0;
		private Runnable resignation;
		
		TestPlayer(Color color, List<Action> actions) {
			this.color = color;
			this.actions = new LinkedList<>(actions);
		}

		@Override
		public void setResignationMethod(Game<Move, Board<Move>> game, Runnable resignation) {
			this.resignation = resignation;
		}

		@Override
		public void requestMove(Game<Move, Board<Move>> game, Consumer<Move> callBack) {
			requestThread = new Thread(() -> {
				try {
					if (thinkTime>0) {
						Thread.sleep(thinkTime);
					}
					final Action action = actions.poll();
					if (action instanceof MoveAction mv) {
						callBack.accept(mv.move);
					} else if (action instanceof ResignAction) {
						resignation.run();
					} else {
						throw new UnsupportedOperationException();
					}
				} catch (InterruptedException e) {
					e.printStackTrace();
					Thread.currentThread().interrupt();
				} catch (Throwable e) {
					e.printStackTrace();
					errorOccurred = true;
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
