package com.fathzer.jchess.swing.settings;

import static com.fathzer.jchess.swing.settings.GameSettings.PlayerType;

import javax.swing.JPanel;
import java.awt.GridBagLayout;
import javax.swing.JLabel;
import java.awt.GridBagConstraints;

import com.fathzer.soft.ajlib.swing.widget.TextWidget;

import java.awt.Insets;
import javax.swing.JComboBox;

public class PlayerSelectionPanel extends JPanel {
	private static final long serialVersionUID = 1L;
	
	private JLabel nameLabel;
	private TextWidget txtName;
	private JLabel typeLabel;
	private JComboBox<PlayerType> comboBox;

	/**
	 * Create the panel.
	 */
	public PlayerSelectionPanel() {
		GridBagLayout gridBagLayout = new GridBagLayout();
		gridBagLayout.columnWidths = new int[]{0, 0, 0};
		gridBagLayout.rowHeights = new int[]{0, 0, 0};
		gridBagLayout.columnWeights = new double[]{0.0, 1.0, Double.MIN_VALUE};
		gridBagLayout.rowWeights = new double[]{0.0, 0.0, Double.MIN_VALUE};
		setLayout(gridBagLayout);
		
		typeLabel = new JLabel("Type: ");
		GridBagConstraints typeLabelGbc = new GridBagConstraints();
		typeLabelGbc.anchor = GridBagConstraints.EAST;
		typeLabelGbc.insets = new Insets(0, 0, 5, 5);
		typeLabelGbc.gridx = 0;
		typeLabelGbc.gridy = 0;
		add(typeLabel, typeLabelGbc);
		
		comboBox = new JComboBox<>();
		for (PlayerType type : PlayerType.values()) {
			comboBox.addItem(type);
		}
		comboBox.setSelectedIndex(0);
		comboBox.setToolTipText("Select player's type. Engines can be added and configured in Engines panel");
		GridBagConstraints comboBoxGridBagConstraints = new GridBagConstraints();
		comboBoxGridBagConstraints.insets = new Insets(0, 0, 5, 0);
		comboBoxGridBagConstraints.fill = GridBagConstraints.HORIZONTAL;
		comboBoxGridBagConstraints.gridx = 1;
		comboBoxGridBagConstraints.gridy = 0;
		add(comboBox, comboBoxGridBagConstraints);
		
		nameLabel = new JLabel("Name: ");
		GridBagConstraints nameLabelGbc = new GridBagConstraints();
		nameLabelGbc.insets = new Insets(0, 0, 0, 5);
		nameLabelGbc.anchor = GridBagConstraints.EAST;
		nameLabelGbc.gridx = 0;
		nameLabelGbc.gridy = 1;
		add(nameLabel, nameLabelGbc);
		
		txtName = new TextWidget();
		GridBagConstraints txtNameGridBagConstraints = new GridBagConstraints();
		txtNameGridBagConstraints.fill = GridBagConstraints.HORIZONTAL;
		txtNameGridBagConstraints.gridx = 1;
		txtNameGridBagConstraints.gridy = 1;
		add(txtName, txtNameGridBagConstraints);
		txtName.setColumns(10);

	}

}
