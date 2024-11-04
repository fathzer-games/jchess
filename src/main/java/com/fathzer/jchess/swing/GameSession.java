package com.fathzer.jchess.swing;

import javax.swing.JOptionPane;
import javax.swing.SwingUtilities;

import com.fathzer.games.Color;
import com.fathzer.games.Status;
import com.fathzer.jchess.AbstractGameSession;
import com.fathzer.jchess.Game;
import com.fathzer.jchess.GameRecorder;
import com.fathzer.jchess.Move;
import com.fathzer.jchess.ai.evaluator.NaiveEvaluator;
import com.fathzer.jchess.bot.Engine;
import com.fathzer.jchess.settings.Settings;

import lombok.extern.slf4j.Slf4j;

@Slf4j
public class GameSession extends AbstractGameSession<GamePanel> {
	private long lastMoveTime = 0;

	public GameSession(GamePanel panel, Settings settings) {
		super(panel, settings);
		panel.getBoard().addPropertyChangeListener(ChessBoardPanel.TARGET, evt -> onMove((Move) evt.getNewValue()));
		panel.setResignationHandler(this::resign);
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
		gui.getBoard().setReverted(Color.BLACK.equals(player1Color));
		if (onlyHumans()) {
			// Change upside down color
			gui.getBoard().setUpsideDownColor(player1Color.opposite());
		}
	}
	
	@Override
	protected void newGame() {
		super.newGame();
		setEvaluation();
		gui.setPlayer1Color(player1Color);
		gui.setClock(game.getClock());
		gui.setScore(getScore());
		gui.getBoard().setBoard(game.getBoard());
		gui.getBoard().setManualMoveEnabled(false);
		lastMoveTime = System.currentTimeMillis();
	}
	
	@Override
	protected void nextMove() {
		final Color activeColor = game.getBoard().getActiveColor();
		final Engine engine = getEngine(activeColor);
		gui.getBoard().setManualMoveEnabled(engine==null);
		if (engine!=null) {
			log.debug("Engine detected for {}",activeColor);
			game.playEngine(engine, this::play, e -> {
				log.error("Error while communicating with "+engine.getName()+" engine", e);
				SwingUtilities.invokeLater(() -> onEngineError(engine));
			});
		}
	}
	
	@Override
	protected void play(Game game, Move move) {
		SwingUtilities.invokeLater(() -> {
			// WARNING: If a revenge is launched before the engine returns its choice, state can be RUNNING again
			// and the move would be transmitted to the panel if we omitted to check we are still in the same game!
			if (move!=null && game==this.game && State.RUNNING.equals(getState())) {
				log.debug("Transmitting {}'s move {} to panel's board",game.getBoard().getActiveColor(),move);
				gui.getBoard().doMove(move);
			} else {
				log.debug("Ignore move {}, state is {}", move, getState());
			}
		});
	}

	protected void onMove(Move move) {
		gui.repaint();
		this.game.onMove(move);
		setEvaluation();
//TODO		think(5000);
		final Status status = gui.getBoard().getStatus();
		if (!Status.PLAYING.equals(status)) {
			// Game is ended
			endOfGame(status);
		} else {
			if (getState()==State.RUNNING) {
				nextMove();
			}
		}
	}
	
	// This makes, always when two bots are competing, and sometime when human plays against a bot,
	// closing the window not exiting the application (and not closing the engine's process)
	//TODO Should be investigated
	@SuppressWarnings("unused")
	private void think(long thinkTime) {
		final long remaining = thinkTime - (System.currentTimeMillis() - lastMoveTime);
		if (remaining>0) {
			game.pause();
			SwingUtilities.invokeLater(() -> {
				System.out.println("Will wait for "+remaining+"ms");
				try {
					synchronized (this) {
						this.wait(remaining);
					}
				} catch (InterruptedException e) {
					Thread.currentThread().interrupt();
				}
				lastMoveTime = System.currentTimeMillis();
				game.start();
			});
		} else {
			lastMoveTime = System.currentTimeMillis();
		}
	}

	private void setEvaluation() {
		final NaiveEvaluator ev = new NaiveEvaluator();
		ev.init(game.getBoard());
		gui.setEvaluation(ev.evaluateAsWhite(game.getBoard())/100);
	}

	@Override
	protected void doTimeUp(Status status) {
		SwingUtilities.invokeLater(()->super.doTimeUp(status));
	}
	
	@Override
	protected boolean continueOnTimeup(Status status) {
		return JOptionPane.showConfirmDialog(gui, getMessage(status)+" Do you want to continue game without clock?","Time is up",JOptionPane.YES_NO_OPTION)==0;
	}

	@Override
	protected boolean isResignationConfirmed() {
		return JOptionPane.showConfirmDialog(gui, "Are you sure you want to resign?","Resignation",JOptionPane.YES_NO_OPTION)==0;
	}
	
	private void onEngineError(Engine engine) {
		JOptionPane.showMessageDialog(gui, "An error occured while communicating with the "+engine.getName()+" engine. Assuming it resigns", "Error", JOptionPane.ERROR_MESSAGE);
		final Status status = Color.WHITE.equals(game.getBoard().getActiveColor()) ? Status.BLACK_WON : Status.WHITE_WON;
		endOfGame(status);
	}
	
	@Override
	protected void onGameEnded() {
		gui.setScore(getScore());
		try {
			GameRecorder.print(this.game.getHistory(), this.getSettings(), this.player1Color, (long) this.getScore().getGameCount());
		} catch (Exception e) {
			log.error("An error occured while writing pgn",e);
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
		gui.setPlayer1Human(settings.getPlayer1().getEngine()==null);
		gui.setPlayer2Human(settings.getPlayer2().getEngine()==null);
		gui.getBoard().setShowPossibleMoves(settings.isShowPossibleMoves());
		gui.getBoard().setTouchMove(settings.isTouchMove());
		gui.getBoard().setReverted(Color.BLACK.equals(player1Color));
		if (onlyHumans() && settings.isTabletMode()) {
			gui.getBoard().setUpsideDownColor(player1Color.opposite());
		} else {
			gui.getBoard().setUpsideDownColor(null);
		}
	}
}
