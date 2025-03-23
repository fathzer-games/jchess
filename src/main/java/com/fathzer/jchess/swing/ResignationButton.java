package com.fathzer.jchess.swing;

import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;

import javax.swing.Icon;
import javax.swing.JButton;
import javax.swing.SwingUtilities;

import com.fathzer.games.util.exec.CustomThreadFactory;
import com.fathzer.soft.ajlib.swing.Utils;

import lombok.Setter;

public class ResignationButton extends JButton {
	private static final long serialVersionUID = 1L;
	
	private static final ScheduledExecutorService TIMER = Executors.newScheduledThreadPool(1, new CustomThreadFactory(() -> "Resignation button daemon", true));
	
	private static final int FLAG_SIZE = 48;
	private static final String IMAGE_PATH = "/whiteFlag.png";
	private static final String IMAGE_PATH_REVERTED = "/whiteFlagReverted.png";
	private static final String ARMED_IMAGE_PATH = "/orangeFlag.png";
	private static final String ARMED_IMAGE_PATH_REVERTED = "/orangeFlagReverted.png";
	private static final Icon FLAG_ICON = Utils.createIcon(PlayerPanel.class.getResource(IMAGE_PATH), FLAG_SIZE);
	private static final Icon FLAG_ICON_REVERTED = Utils.createIcon(PlayerPanel.class.getResource(IMAGE_PATH_REVERTED), FLAG_SIZE);
	private static final Icon ARMED_FLAG_ICON = Utils.createIcon(PlayerPanel.class.getResource(ARMED_IMAGE_PATH), FLAG_SIZE);
	private static final Icon ARMED_FLAG_ICON_REVERTED = Utils.createIcon(PlayerPanel.class.getResource(ARMED_IMAGE_PATH_REVERTED), FLAG_SIZE);

	private boolean reverted;
	private transient ScheduledFuture<?> armed;

	@Setter
	private transient Runnable resignationHandler; 
	

	/**
	 * Create the panel.
	 */
	public ResignationButton() {
		setOpaque(false);

		addActionListener(e -> this.processClick());
		setOpaque(false);
		setBorderPainted(false);
		setContentAreaFilled(false);
		setFocusPainted(false);
	}

	public void setReverted(boolean reverted) {
		this.reverted = reverted;
		setIcon();
	}

	private void setIcon() {
		if (armed==null) {
			setIcon(reverted ? FLAG_ICON_REVERTED : FLAG_ICON);
		} else {
			setIcon(reverted ? ARMED_FLAG_ICON_REVERTED : ARMED_FLAG_ICON);
		}
	}
	
	private void processClick() {
		if (armed!=null) {
			if (resignationHandler!=null) {
				resignationHandler.run();
			}
			armed.cancel(false);
			armed = null;
		} else {
			armed = TIMER.schedule(() -> {
				armed = null;
				SwingUtilities.invokeLater(this::setIcon);
			}, 4, TimeUnit.SECONDS);
		}
		setIcon();
	}
}
