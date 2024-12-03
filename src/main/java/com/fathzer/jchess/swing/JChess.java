package com.fathzer.jchess.swing;

import java.awt.Container;
import java.awt.event.ActionEvent;
import java.awt.event.WindowEvent;
import java.io.IOException;
import java.util.List;
import java.util.Optional;
import java.util.prefs.Preferences;

import javax.swing.AbstractAction;
import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JOptionPane;

import org.json.JSONException;
import org.json.JSONObject;
import org.slf4j.LoggerFactory;

import com.fathzer.games.game.EnginePlayer;
import com.fathzer.games.game.GameManager;
import com.fathzer.games.game.GameManager.State;
import com.fathzer.games.game.HumanPlayer;
import com.fathzer.games.game.Player;
import com.fathzer.jchess.Board;
import com.fathzer.jchess.Move;
import com.fathzer.jchess.bot.uci.EngineLoader;
import com.fathzer.jchess.bot.uci.EngineLoader.EngineData;
import com.fathzer.jchess.settings.Context;
import com.fathzer.jchess.settings.Settings;
import com.fathzer.jchess.settings.Settings.EngineSettings;
import com.fathzer.jchess.settings.Settings.PlayerSettings;
import com.fathzer.jchess.swing.settings.SettingsDialog;
import com.fathzer.jchess.tournament.Tournament;
import com.fathzer.jchess.uci.JChessUCI;
import com.fathzer.soft.ajlib.swing.framework.Application;
import com.fathzer.util.TinyJackson;

import lombok.extern.slf4j.Slf4j;

import java.awt.Color;

@Slf4j
public class JChess extends Application {
	private static final String SETTINGS_PREF = "gameSettings";

	private final JChessPanel panel;
	private Settings settings;
	private AbstractAction startAction;
	private AbstractAction settingsAction;
	private GameSession game;

	public static void main(String[] args) {
		if (Boolean.getBoolean("uci")) {
			JChessUCI.main(args);
		} else if (Boolean.getBoolean("tournament")) {
			new Tournament().launch(args);
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
				final Context context = new Context(settings, EngineLoader.getEngines());
				final SettingsDialog dialog = new SettingsDialog(getJFrame(), context);
				dialog.setVisible(true);
				final Settings result = dialog.getResult();
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
				startGameSession();
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
		try {
			EngineLoader.init();
		} catch (IOException e) {
			LoggerFactory.getLogger(JChess.class).error("An error occurred while reading the external engine configuration file (data/engines.json)", e);
			int result = JOptionPane.showConfirmDialog(null,"An error occurred while reading the external engine configuration file (data/engines.json).\nWould you like to quit now?", "Engine configuration error",
		               JOptionPane.YES_NO_OPTION,
		               JOptionPane.ERROR_MESSAGE);
			if (result==0) {
				return false;
			}
		}
		fixSettings();
		final GamePanel gamePanel = panel.getGamePanel();
		this.game = new GameSession(panel.getGamePanel(), settings, getPlayer(settings.getPlayer1(), gamePanel.getPlayer1()), getPlayer(settings.getPlayer2(), gamePanel.getPlayer2()));
		this.game.addListener((o,n) -> {
			if (GameManager.State.ENDED.equals(n)) {
				this.startAction.setEnabled(true);
				this.panel.setMenuVisible(true);
			}
		});
		return true;
	}
	
	private Player<Move, Board<Move>> getPlayer(PlayerSettings playerSettings, PlayerPanel playerPanel) {
		final EngineSettings engineSettings = playerSettings.getEngine();
		if (engineSettings==null) {
			return new HumanPlayer(panel.getGamePanel().getBoard(), playerPanel);
		} else {
			final String name = engineSettings.getName();
			Optional<EngineData> found = EngineLoader.getEngines().stream().filter(e -> e.getEngine()!=null && e.getName().equals(name)).findAny();
			return new EnginePlayer(found.orElseThrow().getEngine());
		}
	}
	
	/** Fixes incompatibilities between settings and available engines and starts used engines.
	 */
	private void fixSettings() {
		final List<EngineData> engines = EngineLoader.getEngines();
		fixSettings(settings.getPlayer1(), engines);
		fixSettings(settings.getPlayer2(), engines);
	}
	
	private void fixSettings(PlayerSettings player, List<EngineData> engines) {
		final String engineName = player.getEngine()!=null ? player.getEngine().getName() : null;
		if (engineName!=null && !engineName.isBlank()) {
			final Optional<EngineData> engine = engines.stream().filter(e -> engineName.equals(e.getName())).findFirst();
			boolean ok = false;
			if (engine.isPresent()) {
				// Ensure engine is started
				if (engine.get().getEngine()!=null) {
					ok = true;
				} else {
					try {
						engine.get().start();
						ok = true;
					} catch (IOException e) {
						log.error("Error while launching engine {}", engine.get().getName(), e);
					}
				}
			}
			if (!ok) {
				// Engine does not exists or can't start, switch to human player
				player.setEngine(null);
			}
		}
	}
	
	@Override
	protected void saveState() {
		super.saveState();
		final Preferences preferences = getPreferences();
		if (settings==null) {
			preferences.remove(SETTINGS_PREF);
		} else {
			try {
				final String value = TinyJackson.toJSONObject(settings).toString();
				preferences.put(SETTINGS_PREF, value);
				log.debug("Application state written: {}", value);
			} catch (JSONException e) {
				log.error("Error while saving application state", e);
			}
		}
	}

	@Override
	protected void restoreState() {
		super.restoreState();
		final String value = getPreferences().get(SETTINGS_PREF, null);
		log.debug("Application state read: {}", value);
		try {
			this.settings = value==null ? new Settings() : TinyJackson.toObject(new JSONObject(value), Settings.class);
		} catch (JSONException e) {
			log.error("Error while reading previous application state", e);
			this.settings = new Settings();
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

	private void startGameSession() {
		this.startAction.setEnabled(false);
		this.panel.setMenuVisible(false);
		if (game.getState()==State.ENDED) {
			this.game = new GameSession(panel.getGamePanel(), settings, game.getPlayer1(), game.getPlayer2());
		}
		new Thread(this.game).start();
	}

	@Override
	protected void onClose(WindowEvent event) {
		if (this.game!=null) {
			game.stop();
		}
		super.onClose(event);
	}
}
