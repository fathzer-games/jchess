package com.fathzer.jchess.swing;

import javax.swing.JPanel;
import javax.swing.SwingUtilities;

import com.fathzer.games.clock.Clock;
import com.fathzer.games.clock.ClockState;
import com.fathzer.soft.ajlib.swing.Utils;
import com.fathzer.soft.ajlib.swing.widget.RotatingLabel;

import lombok.Setter;

import java.awt.BorderLayout;
import java.time.Instant;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;
import java.util.function.Consumer;

import javax.swing.Icon;
import javax.swing.JButton;

import java.awt.Color;
import java.awt.Font;

public class PlayerPanel extends JPanel {
	private static final long serialVersionUID = 1L;

	private static final int FLAG_SIZE = 48;
	private static final String IMAGE_PATH = "/whiteFlag.png";
	private static final String IMAGE_PATH_REVERTED = "/whiteFlagReverted.png";
	private static final Icon FLAG_ICON = Utils.createIcon(PlayerPanel.class.getResource(IMAGE_PATH), FLAG_SIZE);
	private static final Icon FLAG_ICON_REVERTED = Utils.createIcon(PlayerPanel.class.getResource(IMAGE_PATH_REVERTED), FLAG_SIZE);

	private final JPanel flagPanel;
	private final JButton whiteFlag;
	private final RotatingLabel scoreLabel;
	private final RotatingLabel clockLabel;
	private transient Clock clock;
	private com.fathzer.games.Color playerColor;
	private transient ScheduledFuture<?> refreshTask;
	@Setter
	private transient Consumer<com.fathzer.games.Color> resignationHandler; 

	/**
	 * Create the panel.
	 */
	public PlayerPanel() {
		setOpaque(false);
		setLayout(new BorderLayout(0, 0));

		whiteFlag = new JButton();
		whiteFlag.addActionListener(e -> {
			if (resignationHandler!=null) {
				resignationHandler.accept(playerColor);
			}
		});
		whiteFlag.setOpaque(false);
		whiteFlag.setBorderPainted(false);
		whiteFlag.setContentAreaFilled(false);
		whiteFlag.setFocusPainted(false);
		
		scoreLabel = new RotatingLabel();
		scoreLabel.setFont(new Font("Dialog", Font.BOLD, 20));
		scoreLabel.setForeground(Color.WHITE);
		scoreLabel.setText(" ");
		flagPanel = new JPanel();
		flagPanel.setOpaque(false);
		
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
		flagPanel.getLayout().removeLayoutComponent(whiteFlag);
		addComponents(reverted);
	}
	
	private void addComponents(boolean reverted) {
		whiteFlag.setIcon(reverted ? FLAG_ICON_REVERTED : FLAG_ICON);
		clockLabel.setRotation(reverted ? 180 : 0);
		scoreLabel.setRotation(reverted ? 180 : 0);

		if (reverted) {
			flagPanel.add(scoreLabel);
			flagPanel.add(whiteFlag);
		} else {
			flagPanel.add(whiteFlag);
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
		this.whiteFlag.setVisible(visible);
	}
	
	public void setScore(int score) {
		final String text;
		if (score==0) {
			text = " ";
		} else if (score>0) {
			text = "+"+score;
		} else {
			text = Integer.toString(score);
		}
		this.scoreLabel.setText(text);
	}
}
