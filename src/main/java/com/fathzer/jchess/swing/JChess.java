package com.fathzer.jchess.swing;

import java.awt.Container;
import java.awt.event.ActionEvent;
import java.awt.event.WindowEvent;
import java.io.IOException;
import java.io.UncheckedIOException;
import java.util.prefs.Preferences;

import javax.swing.AbstractAction;
import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JOptionPane;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fathzer.jchess.bot.uci.EngineLoader;
import com.fathzer.jchess.settings.GameSettings;
import com.fathzer.jchess.swing.settings.old.SettingsDialog;
import com.fathzer.jchess.uci.JChessUCI;
import com.fathzer.soft.ajlib.swing.framework.Application;
import java.awt.Color;

public class JChess extends Application {
	private static final String SETTINGS_PREF = "gameSettings";

	private final JChessPanel panel;
	private GameSettings settings;
	private AbstractAction startAction;
	private AbstractAction settingsAction;
	private GameSession game;

	public static void main(String[] args) {
		if (Boolean.getBoolean("uci")) {
			JChessUCI.main(args);
		} else {
			new JChess().launch();
		}
	}
	
	private JChess() {
		this.panel = new JChessPanel();
		panel.setBackground(Color.BLACK);
		settingsAction = new AbstractAction("Settings") {
			private static final long serialVersionUID = 1L;

			@Override
			public void actionPerformed(ActionEvent e) {
				final SettingsDialog dialog = new SettingsDialog(getJFrame(), settings);
				dialog.setVisible(true);
				final GameSettings result = dialog.getResult();
				if (result!=null) {
					settings = result;
					game.setSettings(settings);
					// I don't know why if this line is omitted, the buttons of the panel are not painted again
					panel.repaint();
				}
			}
		};
		this.startAction = new AbstractAction("Start") {
			private static final long serialVersionUID = 1L;

			@Override
			public void actionPerformed(ActionEvent e) {
				startGame();
			}
		};
		panel.setPlayAction(() -> this.startAction.actionPerformed(null));
		panel.setSettingsAction(() -> this.settingsAction.actionPerformed(null));
	}
	
	@Override
	protected Container buildMainPanel() {
		return panel;
	}

	@Override
	public String getName() {
		return "JChess";
	}

	@Override
	protected boolean onStart() {
		this.game = new GameSession(panel.getGamePanel(), settings);
		this.game.addListener((o,n) -> {
			if (GameSession.State.ENDED.equals(n)) {
				this.startAction.setEnabled(true);
				this.panel.setMenuVisible(true);
			}
		});
		try {
			EngineLoader.init();
		} catch (IOException e) {
			int result = JOptionPane.showConfirmDialog(null,"An error occured while reading the external engine configuration file (data/engines.json).\nWould you like to quit now?", "Engine configuration error",
		               JOptionPane.YES_NO_OPTION,
		               JOptionPane.ERROR_MESSAGE);
			return result!=0;
		}
		return true;
	}
	
	@Override
	protected void saveState() {
		super.saveState();
		final Preferences preferences = getPreferences();
		if (settings==null) {
			preferences.remove(SETTINGS_PREF);
		} else {
			try {
				String value = GameSettings.MAPPER.writeValueAsString(settings);
				preferences.put(SETTINGS_PREF, value);
			} catch (JsonProcessingException e) {
				throw new UncheckedIOException(e);
			}
		}
	}

	@Override
	protected void restoreState() {
		super.restoreState();
		final String value = getPreferences().get(SETTINGS_PREF, null);
		try {
			this.settings = value==null ? new GameSettings() : GameSettings.MAPPER.readValue(value, GameSettings.class);
		} catch (JsonProcessingException e) {
			throw new UncheckedIOException(e);
		}
	}

	@Override
	protected JMenuBar buildMenuBar() {
		final JMenuBar bar = super.buildMenuBar();
		final JMenu menu = bar.getMenu(0);
		menu.insert(settingsAction, 0);
		menu.insert(this.startAction, 0);
		return bar;
	}

	private void startGame() {
		this.startAction.setEnabled(false);
		this.panel.setMenuVisible(false);
		this.game.start();
	}

	@Override
	protected void onClose(WindowEvent event) {
		if (this.game!=null) {
			game.stop();
		}
		super.onClose(event);
	}
}
