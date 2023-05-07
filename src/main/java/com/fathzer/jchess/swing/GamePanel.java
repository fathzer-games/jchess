package com.fathzer.jchess.swing;

import java.util.function.Consumer;

import javax.swing.JPanel;

import com.fathzer.games.Color;
import com.fathzer.games.clock.Clock;

import lombok.Getter;

public class GamePanel extends JPanel {
	private static final long serialVersionUID = 1L;

	@Getter
	private ChessBoardPanel board;

	private Color player1Color;

	private PlayerPanel player1;
	private PlayerPanel player2;

	public GamePanel() {
		setLayout(new ChessLayout());
		
		player2 = new PlayerPanel();
		add(ChessLayout.NORTH, player2);
		
		player1 = new PlayerPanel();
		add(ChessLayout.SOUTH, player1);
		
		this.board = new ChessBoardPanel();
		add(ChessLayout.CENTER, this.board);
	}
	
	public void setPlayer1Color(Color player1Color) {
		this.player1Color = player1Color;
	}
	
	public void setPlayer1Human(boolean human) {
		player1.setWhiteFlagVisible(human);
	}

	public void setPlayer2Human(boolean human) {
		player2.setReverted(human);
		player2.setWhiteFlagVisible(human);
	}
	
	public void setClock(Clock clock) {
		player1.setClock(clock, player1Color);
		player2.setClock(clock, player1Color.opposite());
	}
	
	public void setResignationHandler(Consumer<Color> resignationHandler) {
		player1.setResignationHandler(resignationHandler);
		player2.setResignationHandler(resignationHandler);
	}

	@Override
	public void setBackground(java.awt.Color bg) {
		super.setBackground(bg);
		if (board!=null) {
			board.setBackground(bg);
		}
	}
}
