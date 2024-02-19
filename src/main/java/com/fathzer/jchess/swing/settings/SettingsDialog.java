package com.fathzer.jchess.swing.settings;

import java.awt.Window;
import java.io.IOException;

import javax.swing.JPanel;

import com.fathzer.jchess.bot.uci.EngineLoader;
import com.fathzer.jchess.settings.Context;
import com.fathzer.jchess.settings.GameSettings;
import com.fathzer.soft.ajlib.swing.dialog.AbstractDialog;

public class SettingsDialog extends AbstractDialog<Context, GameSettings> {
	private static final long serialVersionUID = 1L;

	private SettingsPanel panel;
	private boolean okEnabled;

	public SettingsDialog(Window owner, Context data) {
		super(owner, "Settings", data);
		super.setResizable(true);
	}

	@Override
	protected JPanel createCenterPane() {
		this.panel = new SettingsPanel(data);
//		panel.addPropertyChangeListener(SettingsPanel.VALID_SETTINGS_PROPERTY, e -> {
//			this.okEnabled = (Boolean) e.getNewValue();
//			updateOkButtonEnabled();
//		});
//		panel.setSettings(super.data);
		return panel;
	}

	@Override
	protected GameSettings buildResult() {
		return null; //TODO
	}
//
//	@Override
//	protected JComponent createExtraComponent() {
//		return new JButton(new AbstractAction("Default") {
//			private static final long serialVersionUID = 1L;
//
//			@Override
//			public void actionPerformed(ActionEvent e) {
//				panel.setSettings(new GameSettings());
//			}
//		});
//	}
	
	public static void main(String[] args) throws IOException {
		EngineLoader.init();
		Context context = new Context(new GameSettings(), EngineLoader.getEngines());
		
		final SettingsDialog dialog = new SettingsDialog(null, context);
		do {
			dialog.setVisible(true);
			Object result = dialog.getResult();
			if (result==null) {
				break;
			}
		} while (true);
	}
}
