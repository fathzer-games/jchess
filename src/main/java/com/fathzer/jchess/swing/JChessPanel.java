package com.fathzer.jchess.swing;

import java.awt.Color;

import javax.swing.JPanel;
import javax.swing.OverlayLayout;

import lombok.Getter;

public class JChessPanel extends JPanel {
	private static final long serialVersionUID = 1L;
	
	@Getter
	private GamePanel gamePanel;
	private MenuPanel menuPanel;

	/**
	 * Create the panel.
	 */
	public JChessPanel() {
		this.setLayout(new OverlayLayout(this));
		
		menuPanel = new MenuPanel();
		menuPanel.setOpaque(false);
		add(menuPanel);
		
		gamePanel = new GamePanel();
		add(gamePanel);
	}
	
	public void setMenuVisible(boolean visible) {
		this.menuPanel.setVisible(visible);
	}

	@Override
	public void setBackground(Color bg) {
		super.setBackground(bg);
		if (this.gamePanel!=null) {
			this.gamePanel.setBackground(bg);
		}
	}

	public void setPlayAction(Runnable action) {
		this.menuPanel.setPlayAction(action);
	}

	public void setSettingsAction(Runnable action) {
		this.menuPanel.setSettingsAction(action);
	}
}
