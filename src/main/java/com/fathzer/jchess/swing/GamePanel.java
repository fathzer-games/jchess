package com.fathzer.jchess.swing;

import javax.swing.JPanel;

import com.fathzer.games.Color;
import com.fathzer.games.clock.Clock;
import com.fathzer.jchess.Score;

import lombok.Getter;

public class GamePanel extends JPanel {
	private static final long serialVersionUID = 1L;

	@Getter
	private ChessBoardPanel board;

	private Color player1Color;

	@Getter
	private PlayerPanel player1;
	@Getter
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

	@Override
	public void setBackground(java.awt.Color bg) {
		super.setBackground(bg);
		if (board!=null) {
			board.setBackground(bg);
		}
	}
	
	public void setEvaluation(int whiteScore) {
		if (player1Color!=Color.WHITE) {
			whiteScore = -whiteScore;
		}
		player1.setEvaluation(whiteScore);
		player2.setEvaluation(-whiteScore);
	}

	public void setScore(Score score) {
		if (score.getGameCount()==0) {
			player1.clearScore();
			player2.clearScore();
		} else {
			player1.setScore(score.getPlayerScore(true));
			player2.setScore(score.getPlayerScore(false));
		}
	}
}
