package com.fathzer.jchess.swing.settings;

import java.awt.Window;

import javax.swing.JPanel;

import com.fathzer.jchess.bot.Engine;
import com.fathzer.soft.ajlib.swing.dialog.AbstractDialog;

public class EngineDialog extends AbstractDialog<Engine, Boolean> {
	private static final long serialVersionUID = 1L;

	public EngineDialog(Window owner, Engine data) {
		super(owner, "Settings", data);
		this.getCancelButton().setVisible(false);
		this.getOkButton().setVisible(false);
	}

	@Override
	protected JPanel createCenterPane() {
		return new EngineConfigurationPanel(data.getOptions());
	}

	@Override
	protected Boolean buildResult() {
		return true;
	}
}
