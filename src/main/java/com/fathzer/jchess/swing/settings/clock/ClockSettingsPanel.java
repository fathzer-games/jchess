package com.fathzer.jchess.swing.settings.clock;

import javax.swing.JPanel;
import java.awt.GridBagLayout;
import javax.swing.JLabel;
import java.awt.GridBagConstraints;
import javax.swing.JTextField;
import java.awt.Insets;
import java.math.BigInteger;

import javax.swing.JCheckBox;
import javax.swing.SwingConstants;

import com.fathzer.soft.ajlib.swing.widget.IntegerWidget;

import javax.swing.JButton;

public class ClockSettingsPanel extends JPanel {

	private static final long serialVersionUID = 1L;
	private IntegerWidget timeField;
	private IntegerWidget incrementField;
	private JLabel movesBeforeIncrementLabel;
	private JTextField movesBeforeIncrementField;

	/**
	 * Create the panel.
	 */
	public ClockSettingsPanel() {
		GridBagLayout gridBagLayout = new GridBagLayout();
		setLayout(gridBagLayout);
		
		final JLabel timeLabel = new JLabel("Time: ");
		timeLabel.setToolTipText("Maximum time in seconds");
		GridBagConstraints timeLabelGbc = new GridBagConstraints();
		timeLabelGbc.insets = new Insets(0, 0, 5, 5);
		timeLabelGbc.anchor = GridBagConstraints.WEST;
		timeLabelGbc.gridx = 0;
		timeLabelGbc.gridy = 0;
		add(timeLabel, timeLabelGbc);
		
		timeField = new IntegerWidget(BigInteger.ZERO, BigInteger.valueOf(24L*3600));
		GridBagConstraints timeFieldGbc = new GridBagConstraints();
		timeFieldGbc.insets = new Insets(0, 0, 5, 5);
		timeFieldGbc.fill = GridBagConstraints.HORIZONTAL;
		timeFieldGbc.gridx = 1;
		timeFieldGbc.gridy = 0;
		add(timeField, timeFieldGbc);
		timeField.setColumns(10);
		
		final JLabel incrementLabel = new JLabel("Increment (s): ");
		GridBagConstraints incrementLabelGbc = new GridBagConstraints();
		incrementLabelGbc.anchor = GridBagConstraints.WEST;
		incrementLabelGbc.insets = new Insets(0, 0, 5, 5);
		incrementLabelGbc.gridx = 0;
		incrementLabelGbc.gridy = 1;
		add(incrementLabel, incrementLabelGbc);
		
		incrementField = new IntegerWidget(BigInteger.ZERO, BigInteger.valueOf(3600));
		incrementField.setToolTipText("Increment of time in seconds");
		GridBagConstraints incrementFieldGbc = new GridBagConstraints();
		incrementFieldGbc.insets = new Insets(0, 0, 5, 5);
		incrementFieldGbc.fill = GridBagConstraints.HORIZONTAL;
		incrementFieldGbc.gridx = 1;
		incrementFieldGbc.gridy = 1;
		add(incrementField, incrementFieldGbc);
		incrementField.setColumns(10);
		
		movesBeforeIncrementLabel = new JLabel("Moves before increment: ");
		GridBagConstraints movesBeforeIncrementLabelGbc = new GridBagConstraints();
		movesBeforeIncrementLabelGbc.anchor = GridBagConstraints.WEST;
		movesBeforeIncrementLabelGbc.insets = new Insets(0, 0, 5, 5);
		movesBeforeIncrementLabelGbc.gridx = 0;
		movesBeforeIncrementLabelGbc.gridy = 2;
		add(movesBeforeIncrementLabel, movesBeforeIncrementLabelGbc);
		
		movesBeforeIncrementField = new JTextField();
		GridBagConstraints movesBeforeIncrementFieldGbc = new GridBagConstraints();
		movesBeforeIncrementFieldGbc.insets = new Insets(0, 0, 5, 5);
		movesBeforeIncrementFieldGbc.fill = GridBagConstraints.HORIZONTAL;
		movesBeforeIncrementFieldGbc.gridx = 1;
		movesBeforeIncrementFieldGbc.gridy = 2;
		add(movesBeforeIncrementField, movesBeforeIncrementFieldGbc);
		movesBeforeIncrementField.setColumns(10);
	}

}
