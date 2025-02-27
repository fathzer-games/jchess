package com.fathzer.jchess.swing;

import java.lang.reflect.InvocationTargetException;
import java.util.Optional;
import java.util.concurrent.atomic.AtomicReference;
import java.util.function.Supplier;

import javax.swing.JOptionPane;
import javax.swing.SwingUtilities;

import com.fathzer.games.Color;
import com.fathzer.games.GameHistory;
import com.fathzer.games.GameHistory.TerminationCause;
import com.fathzer.games.Status;
import com.fathzer.games.game.Game;
import com.fathzer.games.game.GameManager;
import com.fathzer.games.game.Player;
import com.fathzer.games.util.UncheckedException;
import com.fathzer.jchess.Board;
import com.fathzer.jchess.GameRecorder;
import com.fathzer.jchess.Move;
import com.fathzer.jchess.ai.evaluator.NaiveEvaluator;
import com.fathzer.jchess.settings.Settings;

import lombok.extern.slf4j.Slf4j;

@Slf4j
public class GameSession extends GameManager<Move, Board<Move>, Settings> {
	private long lastMoveTime = 0;
	private GamePanel gui;

	public GameSession(GamePanel panel, Settings settings, Player<Move, Board<Move>> player1, Player<Move, Board<Move>> player2) {
		super(settings, player1, player2);
		this.gui = panel;
		onNewGame(this.game);
		refreshAppearance(settings);
	}
	
	@Override
	protected Board<Move> getStartPosition() {
		return getSettings().getVariant().getRules().apply(getSettings().getFen());
	}

	protected boolean onlyHumans() {
		return this.getSettings().getPlayer1().getEngine()==null && this.getSettings().getPlayer2().getEngine()==null;
	}

	
	@Override
	protected void onStateChanged(State old, State current) {
		super.onStateChanged(old,  current);
		if (SwingUtilities.isEventDispatchThread()) {
			swingStateChanged(current);
		} else {
			SwingUtilities.invokeLater(() -> swingStateChanged(current));
		}
	}
	
	private void swingStateChanged(State current) {
		if (!State.RUNNING.equals(current)) {
			gui.getBoard().setManualMoveEnabled(false);
		}
	}

	@Override
	protected void onPlayerColorsChanged() {
		final Color player1Color = getPlayer1Color();
		gui.getBoard().setReverted(Color.BLACK.equals(player1Color));
		if (onlyHumans()) {
			// Change upside down color
			gui.getBoard().setUpsideDownColor(player1Color.opposite());
		}
	}
	
	@Override
	protected void onNewGame(Game<Move, Board<Move>> game) {
		this.game = game;
		if (gui!=null) {
			SwingUtilities.invokeLater(() -> {
				lastMoveTime = System.currentTimeMillis();
				game.addMoveListener(this::onMove);
				setEvaluation();
				gui.setPlayer1Color(getPlayer1Color());
				gui.setClock(game.getClock());
				gui.setScore(getScore());
				gui.getBoard().setBoard((Board<Move>) game.getHistory().getBoard().fork());
				gui.getBoard().setManualMoveEnabled(false);
			});
		}
	}

	protected void onMove(Game<Move, Board<Move>> game, Move move) {
		SwingUtilities.invokeLater(() -> {
			// WARNING: If a revenge is launched before the engine returns its choice, state can be RUNNING again
			// and the move would be transmitted to the panel if we omitted to check we are still in the same game!
			if (move!=null && game==this.game && State.RUNNING.equals(getState())) {
				log.debug("Transmitting {}'s move {} to panel's board",game.getHistory().getBoard().isWhiteToMove()?Color.WHITE:Color.BLACK,move);
				gui.getBoard().doMove(move);
			} else {
				log.debug("Ignore move {}, state is {}", move, getState());
			}
			gui.repaint();
			setEvaluation();
		});
	}
	
	// This makes, always when two bots are competing, and sometime when human plays against a bot,
	// closing the window not exiting the application (and not closing the engine's process)
	//TODO Should be investigated
//	@SuppressWarnings("unused")
//	private void think(long thinkTime) {
//		final long remaining = thinkTime - (System.currentTimeMillis() - lastMoveTime);
//		if (remaining>0) {
//			game.pause();
//			SwingUtilities.invokeLater(() -> {
//				System.out.println("Will wait for "+remaining+"ms");
//				try {
//					synchronized (this) {
//						this.wait(remaining);
//					}
//				} catch (InterruptedException e) {
//					Thread.currentThread().interrupt();
//				}
//				lastMoveTime = System.currentTimeMillis();
//				game.start();
//			});
//		} else {
//			lastMoveTime = System.currentTimeMillis();
//		}
//	}

	private void setEvaluation() {
		final NaiveEvaluator ev = new NaiveEvaluator();
		ev.init(game.getHistory().getBoard());
		gui.setEvaluation(ev.evaluateAsWhite(game.getHistory().getBoard())/100);
	}
	
	@Override
	protected Optional<Game<Move, Board<Move>>> getResumedGame() {
		if (TerminationCause.TIME_FORFEIT==game.getHistory().getTerminationCause()) {
			try {
				boolean resume = safeSwingCall(() -> continueOnTimeup(game.getHistory().getStatus()));
				if (resume) {
					final GameHistory<Move, Board<Move>> oldHistory = game.getHistory();
					final GameHistory<Move, Board<Move>> history = new GameHistory<>(oldHistory.getStartBoard());
					oldHistory.getMoves().forEach(history::add);
					return Optional.of(new Game<Move, Board<Move>>(history, null, getPlayer(Color.WHITE), getPlayer(Color.BLACK)));
				}
			} catch (InterruptedException e) {
				log.error("An interruption occurred while waiting for user reply", e);
				Thread.currentThread().interrupt();
			}
		}
		return super.getResumedGame();
	}
	
	private <T> T safeSwingCall(Supplier<T> callable) throws InterruptedException {
		if (SwingUtilities.isEventDispatchThread()) {
			return callable.get();
		} else {
			final AtomicReference<T> result = new AtomicReference<>();
			try {
				SwingUtilities.invokeAndWait(() -> result.set(callable.get()));
				return result.get();
			} catch (InvocationTargetException e) {
				throw new UncheckedException(e);
			}
		}
	}

	private boolean continueOnTimeup(Status status) {
		return JOptionPane.showConfirmDialog(gui, getMessage(status)+" Do you want to continue game without clock?","Time is up",JOptionPane.YES_NO_OPTION)==0;
	}


//
//	@Override
//	protected boolean isResignationConfirmed() {
//		return JOptionPane.showConfirmDialog(gui, "Are you sure you want to resign?","Resignation",JOptionPane.YES_NO_OPTION)==0;
//	}
//	
//	private void onEngineError(Engine engine) {
//		JOptionPane.showMessageDialog(gui, "An error occurred while communicating with the "+engine.getName()+" engine. Assuming it resigns", "Error", JOptionPane.ERROR_MESSAGE);
//		final Status status = Color.WHITE.equals(game.getHistory().getBoard().getActiveColor()) ? Status.BLACK_WON : Status.WHITE_WON;
//		endOfGame(status);
//	}
	
	@Override
	protected void onGameEnded() {
		gui.setScore(getScore());
		try {
			GameRecorder.print(this.game.getHistory(), this.getSettings(), this.getPlayer1Color(), (long) this.getScore().getGameCount());
		} catch (Exception e) {
			log.error("An error occurred while writing pgn",e);
		}
	}

	@Override
	protected boolean isMakeRevenge(final Status status) {
		boolean result = super.isMakeRevenge(status);
		if (!result && getTournamentGamesCount()==0) {
			final String revenge = "Revenge";
			int choice = JOptionPane.showOptionDialog(gui, getMessage(status), "End of game", JOptionPane.YES_NO_OPTION, JOptionPane.QUESTION_MESSAGE, null, new String[] {revenge,"Enough for today"}, revenge);
			result = choice==0;
		}
		return result;
	}
	
	private String getMessage(Status status) {
		if (Status.DRAW.equals(status)) {
			return "Draw!";
		} else if (Status.BLACK_WON.equals(status)) {
			return "Black wins!";
		} else if (Status.WHITE_WON.equals(status)) {
			return "White wins!";
		} else {
			throw new IllegalArgumentException();
		}
	}

	@Override
	public void setSettings(Settings settings) {
		super.setSettings(settings);
		refreshAppearance(settings);
	}

	private void refreshAppearance(Settings settings) {
		if (gui!=null) {
			gui.setPlayer1Human(settings.getPlayer1().getEngine()==null);
			gui.setPlayer2Human(settings.getPlayer2().getEngine()==null);
			gui.getBoard().setShowPossibleMoves(settings.isShowPossibleMoves());
			gui.getBoard().setTouchMove(settings.isTouchMove());
			gui.getBoard().setReverted(Color.BLACK.equals(getPlayer1Color()));
			if (onlyHumans() && settings.isTabletMode()) {
				gui.getBoard().setUpsideDownColor(getPlayer1Color().opposite());
			} else {
				gui.getBoard().setUpsideDownColor(null);
			}
		}
	}
}
