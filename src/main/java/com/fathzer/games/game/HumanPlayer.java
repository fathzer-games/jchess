package com.fathzer.games.game;

import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.util.function.Consumer;

import javax.swing.SwingUtilities;

import com.fathzer.jchess.Board;
import com.fathzer.jchess.Move;
import com.fathzer.jchess.swing.ChessBoardPanel;
import com.fathzer.jchess.swing.PlayerPanel;

public class HumanPlayer implements Player<Move, Board<Move>> {
	private final ChessBoardPanel chessBoardPanel;
	private final PlayerPanel playerPanel;
	private Consumer<Move> moveConsumer;
	
	private class MoveListener implements PropertyChangeListener {
		@Override
		public void propertyChange(PropertyChangeEvent evt) {
			// We must ignore the moves of other players with whom we share the board panel.
			if (moveConsumer!=null) {
				moveConsumer.accept((Move) evt.getNewValue());
				moveConsumer = null;
			}
		}
	}

	public HumanPlayer(ChessBoardPanel chessBoardPanel, PlayerPanel playerPanel) {
		this.chessBoardPanel = chessBoardPanel;
		this.playerPanel = playerPanel;
		chessBoardPanel.addPropertyChangeListener(ChessBoardPanel.TARGET, new MoveListener());
	}
	
	@Override
	public void onNewGame(Game<Move, Board<Move>> game) {
		moveConsumer = null;
	}

	@Override
	public void requestMove(Game<Move, Board<Move>> game, Consumer<Move> moveConsumer) {
		SwingUtilities.invokeLater(() -> {
			this.moveConsumer = moveConsumer;
			chessBoardPanel.setManualMoveEnabled(true);
		});
	}

	@Override
	public void setResignationMethod(Game<Move, Board<Move>> game, Runnable resignation) {
		this.playerPanel.setResignationHandler(resignation);
	}
}
