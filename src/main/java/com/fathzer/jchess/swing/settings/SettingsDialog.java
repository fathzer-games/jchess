package com.fathzer.jchess.swing.settings;

import java.awt.Window;
import java.awt.event.ActionEvent;

import javax.swing.AbstractAction;
import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.JPanel;

import com.fathzer.jchess.settings.GameSettings;
import com.fathzer.soft.ajlib.swing.dialog.AbstractDialog;

public class SettingsDialog extends AbstractDialog<GameSettings, GameSettings> {
	private static final long serialVersionUID = 1L;

	private SettingsPanel panel;
	private boolean okEnabled;

	public SettingsDialog(Window owner, GameSettings data) {
		super(owner, "Settings", data);
		super.setResizable(true);
	}

	@Override
	protected JPanel createCenterPane() {
		this.panel = new SettingsPanel();
		panel.addPropertyChangeListener(SettingsPanel.VALID_SETTINGS_PROPERTY, e -> {
			this.okEnabled = (Boolean) e.getNewValue();
			updateOkButtonEnabled();
		});
		panel.setSettings(super.data);
		return panel;
	}

	@Override
	protected String getOkDisabledCause() {
		return okEnabled ? null : "Invalid JSON";
	}

	@Override
	protected GameSettings buildResult() {
		return panel.getSettings();
	}

	@Override
	protected JComponent createExtraComponent() {
		return new JButton(new AbstractAction("Default") {
			private static final long serialVersionUID = 1L;

			@Override
			public void actionPerformed(ActionEvent e) {
				panel.setSettings(new GameSettings());
			}
		});
	}
}
