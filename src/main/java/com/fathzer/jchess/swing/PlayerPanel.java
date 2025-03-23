package com.fathzer.jchess.swing;

import javax.swing.JPanel;
import javax.swing.SwingUtilities;

import com.fathzer.games.clock.Clock;
import com.fathzer.games.clock.ClockState;
import com.fathzer.soft.ajlib.swing.widget.RotatingLabel;

import java.awt.BorderLayout;
import java.time.Instant;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;

import java.awt.Color;
import java.awt.Font;
import java.text.DecimalFormat;

public class PlayerPanel extends JPanel {
	private static final long serialVersionUID = 1L;

	private final JPanel flagPanel;
	private final ResignationButton resignButton;
	private final RotatingLabel scoreLabel;
	private final RotatingLabel clockLabel;
	private transient Clock clock;
	private com.fathzer.games.Color playerColor;
	private transient ScheduledFuture<?> refreshTask;

	private int evaluation = 0;
	private double score = -1.0;
	
	/**
	 * Create the panel.
	 */
	public PlayerPanel() {
		setOpaque(false);
		setLayout(new BorderLayout(0, 0));

		scoreLabel = new RotatingLabel();
		scoreLabel.setFont(new Font("Dialog", Font.BOLD, 20));
		scoreLabel.setForeground(Color.WHITE);
		scoreLabel.setText(" ");
		flagPanel = new JPanel();
		flagPanel.setOpaque(false);
		
		resignButton = new ResignationButton();
		
		clockLabel = new RotatingLabel();
		clockLabel.setText(" ");
		clockLabel.setFont(clockLabel.getFont().deriveFont(Font.BOLD, 16));
		clockLabel.setForeground(Color.WHITE);

		addComponents(false);
	}
	
	public void setReverted(boolean reverted) {
		getLayout().removeLayoutComponent(flagPanel);
		getLayout().removeLayoutComponent(clockLabel);
		flagPanel.getLayout().removeLayoutComponent(scoreLabel);
		flagPanel.getLayout().removeLayoutComponent(resignButton);
		addComponents(reverted);
	}
	
	private void addComponents(boolean reverted) {
		resignButton.setReverted(reverted);
		clockLabel.setRotation(reverted ? 180 : 0);
		scoreLabel.setRotation(reverted ? 180 : 0);

		if (reverted) {
			flagPanel.add(scoreLabel);
			flagPanel.add(resignButton);
		} else {
			flagPanel.add(resignButton);
			flagPanel.add(scoreLabel);
		}

		add(flagPanel, reverted ? BorderLayout.EAST : BorderLayout.WEST);
		add(clockLabel, reverted ? BorderLayout.WEST : BorderLayout.EAST);
	}

	public void setClock(Clock clock, com.fathzer.games.Color playerColor) {
		this.playerColor = playerColor;
		this.clock = clock;
		if (refreshTask!=null) {
			refreshTask.cancel(false);
		}
		if (clock==null) {
			clockLabel.setText(" ");
		} else {
			refreshClock();
			clock.addClockListener(e -> setRefresh(e.getNewState()==ClockState.COUNTING && clock.getPlaying()==playerColor));
		}
	}
	
	private void setRefresh(boolean on) {
		if (on) {
			refreshTask = clock.getScheduler().scheduleAtFixedRate(this::refreshClock, 0, 87, TimeUnit.MILLISECONDS);
		} else {
			if (refreshTask!=null) refreshTask.cancel(false);
			refreshClock();
		}
	}

	private void refreshClock() {
		SwingUtilities.invokeLater(() -> {
			final long remaining = Math.max(clock.getRemaining(playerColor), 0);
			final String pattern = remaining > 3600000L ? "HH:mm:ss.SSS " : "mm:ss.SSS ";
			final String text = DateTimeFormatter.ofPattern(pattern).withZone(ZoneId.of("UTC")).format(Instant.ofEpochMilli(remaining));
			clockLabel.setText(text);
		});
	}
	
	public void setWhiteFlagVisible(boolean visible) {
		this.resignButton.setVisible(visible);
	}
	
	public void setEvaluation(int evaluation) {
		this.evaluation = evaluation;
		setScoreLabel();
	}
	
	private void setScoreLabel() {
		final StringBuilder text = new StringBuilder();
		if (Math.signum(score)>=0) {
			text.append('[');
			synchronized (this) {
				text.append(SCORE_FORMAT.format(score));
			}
			text.append("]");
		}
		if (evaluation!=0) {
			if (text.length()>0) {
				text.append(' ');
			}
			if (evaluation>0) {
				text.append('+').append(evaluation);
			} else {
				text.append(evaluation);
			}
		}
		this.scoreLabel.setText(text.length()>0 ? text.toString() : " ");
	}

	private static final DecimalFormat SCORE_FORMAT = new DecimalFormat("#.##");

	public void setScore(double playerScore) {
		this.score = playerScore;
		setScoreLabel();
	}
	
	public void clearScore() {
		this.score = -1.0;
		setScoreLabel();
	}

	public void setResignationHandler(Runnable resignationHandler) {
		this.resignButton.setResignationHandler(resignationHandler);
	}
}
