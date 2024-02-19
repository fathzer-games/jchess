package com.fathzer.jchess.swing.settings;

import javax.swing.JPanel;
import javax.swing.JTabbedPane;
import javax.swing.SwingConstants;

import com.fathzer.jchess.bot.uci.EngineLoader.EngineData;
import com.fathzer.jchess.settings.Context;
import com.fathzer.jchess.settings.GameSettings;

import java.awt.BorderLayout;
import java.util.Collections;

public class SettingsPanel extends JPanel {
	private static final long serialVersionUID = 1L;
	
	private final GameSettingsPanel gamePanel;

	// Used by Window builder editor
	@SuppressWarnings("unused")
	private SettingsPanel() {
		this(new Context(new GameSettings(), Collections.emptyList()));
	}

	/**
	 * Create the panel.
	 */
	public SettingsPanel(Context context) {
		setLayout(new BorderLayout(0, 0));
		final JTabbedPane tabbedPane = new JTabbedPane(SwingConstants.TOP);
		add(tabbedPane);
		this.gamePanel = new GameSettingsPanel(context);
		tabbedPane.addTab("Game settings", gamePanel);
		final EnginesPanel enginesPanel = new EnginesPanel(context.getEngines(), gamePanel::isEngineInUse);
		tabbedPane.addTab("Engines", enginesPanel);
		
		enginesPanel.addPropertyChangeListener(EnginesPanel.STARTED_PROPERTY_NAME, e -> {
			if (e.getOldValue()==null) {
				gamePanel.engineStarted((EngineData)e.getNewValue());
			} else {
				gamePanel.engineStoped((EngineData)e.getOldValue());
			}
		});
	}
	
	GameSettingsPanel getGameSettingsPanel() {
		return this.gamePanel;
	}
}
