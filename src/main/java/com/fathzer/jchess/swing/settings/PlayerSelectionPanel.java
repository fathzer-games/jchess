package com.fathzer.jchess.swing.settings;

import javax.swing.JPanel;
import java.awt.GridBagLayout;
import javax.swing.JLabel;

import java.awt.Dimension;
import java.awt.GridBagConstraints;

import com.fathzer.jchess.swing.settings.GameSettings.ColorSetting;
import com.fathzer.jchess.swing.settings.GameSettings.PlayerType;
import com.fathzer.soft.ajlib.swing.widget.TextWidget;

import java.awt.Insets;
import javax.swing.JComboBox;

public class PlayerSelectionPanel extends JPanel {
	private static final long serialVersionUID = 1L;
	
	private JLabel nameLabel;
	private TextWidget nameTxt;
	private JLabel typeLabel;
	private JComboBox<PlayerType> typeCombo;
	private JComboBox<ColorSetting> colorComboBox;
	private JLabel colorLabel;
	private JLabel engineLabel;
	private JComboBox<String> engineCombo;

	/**
	 * Create the panel.
	 */
	public PlayerSelectionPanel() {
		GridBagLayout gridBagLayout = new GridBagLayout();
		setLayout(gridBagLayout);
		
		typeLabel = new JLabel("Type: ");
		GridBagConstraints typeLabelGbc = new GridBagConstraints();
		typeLabelGbc.anchor = GridBagConstraints.WEST;
		typeLabelGbc.insets = new Insets(0, 0, 5, 5);
		typeLabelGbc.gridx = 0;
		typeLabelGbc.gridy = 0;
		add(typeLabel, typeLabelGbc);
		
		typeCombo = new JComboBox<>();
		for (PlayerType type : PlayerType.values()) {
			typeCombo.addItem(type);
		}
		typeCombo.addActionListener(e -> {
			setType((PlayerType) typeCombo.getSelectedItem());
		});
		typeCombo.setToolTipText("Select player's type. Engines can be added and configured in Engines panel");
		GridBagConstraints typeComboGbc = new GridBagConstraints();
		typeComboGbc.anchor = GridBagConstraints.NORTHWEST;
		typeComboGbc.insets = new Insets(0, 0, 5, 0);
		typeComboGbc.gridx = 1;
		typeComboGbc.gridy = 0;
		add(typeCombo, typeComboGbc);
		
		nameLabel = new JLabel("Name: ");
		GridBagConstraints nameLabelGbc = new GridBagConstraints();
		nameLabelGbc.insets = new Insets(0, 0, 5, 0);
		nameLabelGbc.anchor = GridBagConstraints.WEST;
		nameLabelGbc.gridx = 0;
		nameLabelGbc.gridy = 1;
		add(nameLabel, nameLabelGbc);
		
		nameTxt = new TextWidget();
		nameTxt.setColumns(15);
		GridBagConstraints nameTxtGridBagConstraints = new GridBagConstraints();
		nameTxtGridBagConstraints.insets = new Insets(0, 0, 5, 0);
		nameTxtGridBagConstraints.fill = GridBagConstraints.HORIZONTAL;
		nameTxtGridBagConstraints.gridx = 1;
		nameTxtGridBagConstraints.gridy = 1;
		add(nameTxt, nameTxtGridBagConstraints);
		
		engineLabel = new JLabel("Engine: ");
		GridBagConstraints engineLabelGbc = new GridBagConstraints();
		engineLabelGbc.insets = new Insets(0, 0, 0, 5);
		engineLabelGbc.anchor = GridBagConstraints.WEST;
		engineLabelGbc.gridx = 0;
		engineLabelGbc.gridy = 2;
		add(engineLabel, engineLabelGbc);
		
		engineCombo = new JComboBox<>();
		//TODO
		engineCombo.addItem("My cool engine");
		engineCombo.addItem("An UCI engine");
		engineCombo.setSelectedIndex(0);
		//End of TODO
		GridBagConstraints engineComboGbc = new GridBagConstraints();
		engineComboGbc.insets = new Insets(0, 0, 5, 0);
		engineComboGbc.anchor = GridBagConstraints.NORTHWEST;
		engineComboGbc.gridx = 1;
		engineComboGbc.gridy = 2;
		add(engineCombo, engineComboGbc);

		colorLabel = new JLabel("Color: ");
		GridBagConstraints player1ColorLabelGbc = new GridBagConstraints();
		player1ColorLabelGbc.anchor = GridBagConstraints.WEST;
		player1ColorLabelGbc.insets = new Insets(0, 0, 0, 5);
		player1ColorLabelGbc.gridx = 0;
		player1ColorLabelGbc.gridy = 3;
		add(colorLabel, player1ColorLabelGbc);
		
		colorComboBox = new JComboBox<>();
		GridBagConstraints playercomboBoxGbc = new GridBagConstraints();
		playercomboBoxGbc.anchor = GridBagConstraints.NORTHWEST;
		playercomboBoxGbc.gridx = 1;
		playercomboBoxGbc.gridy = 3;
		add(colorComboBox, playercomboBoxGbc);
		for (ColorSetting cs : ColorSetting.values()) {
			colorComboBox.addItem(cs);
		}
		colorComboBox.setSelectedIndex(0);
		
		int height = colorComboBox.getPreferredSize().height;
		nameTxt.setPreferredSize(new Dimension(nameTxt.getPreferredSize().width, height));
		typeCombo.setSelectedIndex(0);
	}

	void setColorVisible(boolean visible) {
		colorLabel.setVisible(visible);
		colorComboBox.setVisible(visible);
	}
	
	private void setType(PlayerType type) {
		final boolean isHuman = type==PlayerType.HUMAN;
		nameLabel.setVisible(isHuman);
		nameTxt.setVisible(isHuman);
		engineLabel.setVisible(!isHuman);
		engineCombo.setVisible(!isHuman);
	}
}
