package com.fathzer.jchess.swing.settings;

import javax.swing.JPanel;
import java.awt.GridBagLayout;
import javax.swing.JLabel;
import java.awt.GridBagConstraints;
import java.awt.Insets;
import java.math.BigInteger;

import com.fathzer.games.clock.ClockSettings;
import com.fathzer.soft.ajlib.swing.widget.IntegerWidget;

public class ClockSettingsPanel extends JPanel {
	private static final long serialVersionUID = 1L;
	
	private final JLabel timeLabel; 
	private IntegerWidget timeField;
	private IntegerWidget incrementField;
	private JLabel movesBeforeIncrementLabel;
	private IntegerWidget movesBeforeIncrementField;

	/**
	 * Create the panel.
	 */
	public ClockSettingsPanel() {
		GridBagLayout gridBagLayout = new GridBagLayout();
		gridBagLayout.columnWeights=new double[] {0.0, 1.0};
		setLayout(gridBagLayout);
		
		timeLabel = new JLabel("Time: ");
		timeLabel.setToolTipText("Maximum time in seconds");
		GridBagConstraints timeLabelGbc = new GridBagConstraints();
		timeLabelGbc.insets = new Insets(0, 0, 5, 5);
		timeLabelGbc.anchor = GridBagConstraints.NORTHWEST;
		timeLabelGbc.gridx = 0;
		timeLabelGbc.gridy = 0;
		add(timeLabel, timeLabelGbc);
		
		timeField = new IntegerWidget(BigInteger.ZERO, BigInteger.valueOf(24L*3600));
		timeField.setToolTipText("Total time in seconds");
		GridBagConstraints timeFieldGbc = new GridBagConstraints();
		timeFieldGbc.anchor = GridBagConstraints.NORTHWEST;
		timeFieldGbc.insets = new Insets(0, 0, 5, 5);
		timeFieldGbc.gridx = 1;
		timeFieldGbc.gridy = 0;
		add(timeField, timeFieldGbc);
		timeField.setColumns(5);
		
		final JLabel incrementLabel = new JLabel("Increment: ");
		GridBagConstraints incrementLabelGbc = new GridBagConstraints();
		incrementLabelGbc.anchor = GridBagConstraints.WEST;
		incrementLabelGbc.insets = new Insets(0, 0, 5, 5);
		incrementLabelGbc.gridx = 0;
		incrementLabelGbc.gridy = 1;
		add(incrementLabel, incrementLabelGbc);
		
		incrementField = new IntegerWidget(BigInteger.ZERO, BigInteger.valueOf(3600));
		incrementField.setToolTipText("Increment of time in seconds");
		GridBagConstraints incrementFieldGbc = new GridBagConstraints();
		incrementFieldGbc.anchor = GridBagConstraints.WEST;
		incrementFieldGbc.insets = new Insets(0, 0, 5, 5);
		incrementFieldGbc.gridx = 1;
		incrementFieldGbc.gridy = 1;
		add(incrementField, incrementFieldGbc);
		incrementField.setColumns(5);
		
		movesBeforeIncrementLabel = new JLabel("Moves before increment: ");
		GridBagConstraints movesBeforeIncrementLabelGbc = new GridBagConstraints();
		movesBeforeIncrementLabelGbc.anchor = GridBagConstraints.WEST;
		movesBeforeIncrementLabelGbc.insets = new Insets(0, 0, 5, 5);
		movesBeforeIncrementLabelGbc.gridx = 0;
		movesBeforeIncrementLabelGbc.gridy = 2;
		add(movesBeforeIncrementLabel, movesBeforeIncrementLabelGbc);
		
		movesBeforeIncrementField = new IntegerWidget(BigInteger.ZERO, BigInteger.valueOf(Integer.MAX_VALUE));
		GridBagConstraints movesBeforeIncrementFieldGbc = new GridBagConstraints();
		movesBeforeIncrementFieldGbc.anchor = GridBagConstraints.WEST;
		movesBeforeIncrementFieldGbc.insets = new Insets(0, 0, 5, 5);
		movesBeforeIncrementFieldGbc.gridx = 1;
		movesBeforeIncrementFieldGbc.gridy = 2;
		add(movesBeforeIncrementField, movesBeforeIncrementFieldGbc);
		movesBeforeIncrementField.setColumns(5);
	}

	@Override
	public void setEnabled(boolean enabled) {
		super.setEnabled(enabled);
		for (int i=0; i<this.getComponentCount(); i++) {
			this.getComponent(i).setEnabled(enabled);
		}
	}

	public void setSettings(ClockSettings clock) {
		if (clock==null) {
			timeField.setValue((BigInteger)null);
			incrementField.setValue((BigInteger)null);
			movesBeforeIncrementField.setValue((BigInteger)null);
		} else {
			timeField.setValue(clock.getInitialTime());
			incrementField.setValue(clock.getIncrement());
			movesBeforeIncrementField.setValue(clock.getMovesNumberBeforeIncrement());
		}
	}
	
	public ClockSettings getSettings() {
		// TODO Auto-generated method stub
		throw new UnsupportedOperationException();
	}
}
