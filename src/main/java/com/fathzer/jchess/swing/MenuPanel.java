package com.fathzer.jchess.swing;

import javax.swing.Icon;
import javax.swing.JPanel;

import com.fathzer.soft.ajlib.swing.Utils;

import lombok.Setter;

import java.awt.GridBagLayout;

import javax.swing.JButton;

public class MenuPanel extends JPanel {
	private static final long serialVersionUID = 1L;

	private static final String PLAY_PATH = "/play.png";
	private static final String SETTINGS_PATH = "/settings.png";
	private static final Icon PLAY_ICON = Utils.createIcon(MenuPanel.class.getResource(PLAY_PATH), 64);
	private static final Icon SETTINGS_ICON = Utils.createIcon(MenuPanel.class.getResource(SETTINGS_PATH),64);

	private final JButton settings;
	@Setter
	private Runnable settingsAction;
	private final JButton play;
	@Setter
	private Runnable playAction;

	/**
	 * Create the panel.
	 */
	public MenuPanel() {
		setLayout(new GridBagLayout());
		
		JPanel panel = new JPanel();
		panel.setOpaque(false);
//TODO Rendering seems totally buggy when panel is semi-transparent!
//panel.setBackground(new java.awt.Color(0.0f, 0.0f, 0.0f, 0.5f));
		add(panel);

		settings = getIconButton(SETTINGS_ICON);
		settings.addActionListener(e -> {
			if (settingsAction!=null) {
				settingsAction.run();
			}
		});
		panel.add(settings);
		
		play = getIconButton(PLAY_ICON);
		play.addActionListener(e -> {
			if (playAction!=null) {
				playAction.run();
			}
		});
		panel.add(play);
	}
	
	private JButton getIconButton(Icon icon) {
		final JButton button = new JButton(icon);
		button.setBorderPainted(false);
		button.setContentAreaFilled(false);
		button.setFocusPainted(false);
		return button;
	}
}
