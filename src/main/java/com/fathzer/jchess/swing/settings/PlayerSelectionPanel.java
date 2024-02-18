package com.fathzer.jchess.swing.settings;

import javax.swing.JPanel;
import java.awt.GridBagLayout;
import javax.swing.JLabel;
import javax.swing.JOptionPane;

import java.awt.Dimension;
import java.awt.GridBagConstraints;

import com.fathzer.jchess.bot.uci.EngineLoader.EngineData;
import com.fathzer.jchess.settings.GameSettings.ColorSetting;
import com.fathzer.jchess.settings.GameSettings.EngineSettings;
import com.fathzer.jchess.settings.GameSettings.PlayerSettings;
import com.fathzer.jchess.settings.GameSettings.PlayerType;
import com.fathzer.soft.ajlib.swing.Utils;
import com.fathzer.soft.ajlib.swing.widget.TextWidget;

import java.awt.Insets;
import java.util.List;
import java.util.Objects;

import javax.swing.JComboBox;

public class PlayerSelectionPanel extends JPanel {
	private static final long serialVersionUID = 1L;
	public static final String SELECTED_ENGINE_PROPERTY_NAME = "selected engine";
	
	private JLabel nameLabel;
	private TextWidget nameTxt;
	private JLabel typeLabel;
	private JComboBox<PlayerType> typeCombo;
	private JComboBox<ColorSetting> colorComboBox;
	private JLabel colorLabel;
	private JLabel engineLabel;
	private JComboBox<String> engineCombo;
	
	private String currentEngine;

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
		
		int height = engineCombo.getPreferredSize().height;
		int width = nameTxt.getPreferredSize().width;
		//TODO Should also use the label sizes to prevent problems 
		nameTxt.setPreferredSize(new Dimension(width, height));
		engineCombo.setPreferredSize(new Dimension(width, height));
		
		typeCombo.addActionListener(e -> {
			setType((PlayerType) typeCombo.getSelectedItem());
			updateSelectedEngine();
		});

		engineCombo.addItemListener(e -> updateSelectedEngine());
	}

	public void setColor(ColorSetting player1Color) {
		colorComboBox.setSelectedItem(player1Color);
	}
	void setColorVisible(boolean visible) {
		colorLabel.setVisible(visible);
		colorComboBox.setVisible(visible);
	}
	
	private void setType(PlayerType type) {
		final boolean isHuman = type==PlayerType.HUMAN;
		if (!isHuman && engineCombo.getItemCount()==0) {
			JOptionPane.showMessageDialog(Utils.getOwnerWindow(this), "Sorry, there's currently no engine started.\nPlease start an engine in '"+"Engines"+"' tab");
			typeCombo.setSelectedItem(PlayerType.HUMAN);
			return;
		}
		nameLabel.setVisible(isHuman);
		nameTxt.setVisible(isHuman);
		engineLabel.setVisible(!isHuman);
		engineCombo.setVisible(!isHuman);
	}

	public void setSettings(PlayerSettings settings, List<EngineData> engines) {
		engines.forEach(e -> {
			if (e.getEngine()!=null) {
				engineCombo.addItem(e.getName());
			}
		});
		final EngineSettings engine = settings.getEngine();
		if (engine==null) {
			typeCombo.setSelectedItem(PlayerType.HUMAN);
			nameTxt.setText(settings.getName());
		} else {
			typeCombo.setSelectedItem(PlayerType.ENGINE);
			System.out.println("Here !");
			engineCombo.setSelectedItem(engine.getName());
		}
	}

	public PlayerSettings getSettings() {
		//TODO
		throw new UnsupportedOperationException();
	}

	public void engineStarted(EngineData engine) {
		engineCombo.addItem(engine.getName());
	}

	public void engineStopped(EngineData engine) {
		engineCombo.removeItem(engine.getName());
		if (engineCombo.getItemCount()==0) {
			typeCombo.setSelectedItem(PlayerType.HUMAN);
		}
	}
	
	private void updateSelectedEngine() {
		String update = typeCombo.getSelectedItem()==PlayerType.HUMAN ? null : (String)engineCombo.getSelectedItem();
		if (!Objects.equals(update, currentEngine)) {
			final String old = currentEngine;
			this.currentEngine = update;
			firePropertyChange(SELECTED_ENGINE_PROPERTY_NAME, old, this.currentEngine);
		}
	}
}
