package com.fathzer.jchess.swing.settings;

import javax.swing.JPanel;
import java.awt.GridBagLayout;
import javax.swing.JLabel;

import java.awt.GridBagConstraints;

import com.fathzer.jchess.bot.uci.EngineLoader.EngineData;
import com.fathzer.jchess.settings.GameSettings.ColorSetting;
import com.fathzer.jchess.settings.GameSettings.EngineSettings;
import com.fathzer.jchess.settings.GameSettings.PlayerSettings;
import com.fathzer.jchess.settings.GameSettings.Variant;
import com.fathzer.jchess.swing.widget.JComboBoxWithDisabledItems;
import com.fathzer.soft.ajlib.swing.widget.TextWidget;

import java.awt.Insets;
import java.beans.PropertyChangeListener;
import java.util.List;
import java.util.Objects;

import javax.swing.JComboBox;

/** A panel to select player.
 */
public class PlayerSelectionPanel extends JPanel {
	
	static class Player {
		EngineData engine;
		String name;
		
		Player(String name) {
			this.name = name;
		}
		
		Player(EngineData engine) {
			this.engine = engine;
			this.name = engine.getName();
		}
		
		boolean isHuman() {
			return engine==null;
		}

		@Override
		public String toString() {
			return isHuman() ? "Human" : engine.getName();
		}
	}
	
	private static final long serialVersionUID = 1L;
	public static final String SELECTED_PLAYER_PROPERTY_NAME = "selected player";
	
	private JLabel nameLabel;
	private TextWidget nameTxt;
	private JLabel typeLabel;
	private JComboBoxWithDisabledItems<Player> whoCombo;
	private JComboBox<ColorSetting> colorComboBox;
	private JLabel colorLabel;
	
	private transient Player currentPlayer;
	private Variant currentVariant;
	private final transient PropertyChangeListener nameListener;

	/**
	 * Create the panel.
	 */
	public PlayerSelectionPanel() {
		GridBagLayout gridBagLayout = new GridBagLayout();
		setLayout(gridBagLayout);
		
		typeLabel = new JLabel("Who: ");
		GridBagConstraints typeLabelGbc = new GridBagConstraints();
		typeLabelGbc.anchor = GridBagConstraints.WEST;
		typeLabelGbc.insets = new Insets(0, 0, 5, 5);
		typeLabelGbc.gridx = 0;
		typeLabelGbc.gridy = 0;
		add(typeLabel, typeLabelGbc);
		
		whoCombo = new JComboBoxWithDisabledItems<>();
		whoCombo.addItem(new Player("Human being"));
		whoCombo.setToolTipText("Select player. Engines can be added and configured in Engines panel");
		GridBagConstraints whoComboGbc = new GridBagConstraints();
		whoComboGbc.anchor = GridBagConstraints.NORTHWEST;
		whoComboGbc.insets = new Insets(0, 0, 5, 0);
		whoComboGbc.gridx = 1;
		whoComboGbc.gridy = 0;
		add(whoCombo, whoComboGbc);
		
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

		colorLabel = new JLabel("Color: ");
		GridBagConstraints player1ColorLabelGbc = new GridBagConstraints();
		player1ColorLabelGbc.anchor = GridBagConstraints.WEST;
		player1ColorLabelGbc.insets = new Insets(0, 0, 0, 5);
		player1ColorLabelGbc.gridx = 0;
		player1ColorLabelGbc.gridy = 2;
		add(colorLabel, player1ColorLabelGbc);
		
		colorComboBox = new JComboBox<>();
		GridBagConstraints playercomboBoxGbc = new GridBagConstraints();
		playercomboBoxGbc.anchor = GridBagConstraints.NORTHWEST;
		playercomboBoxGbc.gridx = 1;
		playercomboBoxGbc.gridy = 2;
		add(colorComboBox, playercomboBoxGbc);
		for (ColorSetting cs : ColorSetting.values()) {
			colorComboBox.addItem(cs);
		}
		colorComboBox.setSelectedIndex(0);
		
		whoCombo.setEnabledItems(this::isEnabled);
		whoCombo.addActionListener(e -> updateSelectedPlayer());
		nameListener = e->updatePlayerName();
		nameTxt.addPropertyChangeListener(TextWidget.TEXT_PROPERTY, nameListener);
	}

	public void setColor(ColorSetting player1Color) {
		colorComboBox.setSelectedItem(player1Color);
	}
	void setColorVisible(boolean visible) {
		colorLabel.setVisible(visible);
		colorComboBox.setVisible(visible);
	}
	
	void setVariant(Variant variant) {
		if (variant!=this.currentVariant) {
			this.currentVariant = variant;
			checkSelected();
		}
	}

	private void checkSelected() {
		final Player player = (Player) whoCombo.getSelectedItem();
		if (!isEnabled(player)) {
			whoCombo.setSelectedIndex(0);
		}
	}
	
	private boolean isEnabled(Player player) {
		return player.isHuman() || (player.engine.getEngine()!=null && player.engine.getEngine().isSupported(currentVariant));
	}
	
	public void setSettings(PlayerSettings settings, List<EngineData> engines, Variant variant) {
		this.currentVariant = variant;
		whoCombo.removeAllItems();
		final String name = settings.getName()==null ? "" : settings.getName();
		whoCombo.addItem(new Player(name));
		engines.forEach(e -> whoCombo.addItem(new Player(e)));
		
		final EngineSettings engine = settings.getEngine();
		boolean found = false;
		if (engine!=null) {
			// If an engine is selected
			for (int i = 1; i < whoCombo.getItemCount(); i++) {
				final Player player = whoCombo.getItemAt(i);
				if (player.name.equals(engine.getName()) && isEnabled(player)) {
					found = true;
					whoCombo.setSelectedIndex(i);
				}
			}
		}
		if (!found) {
			whoCombo.setSelectedIndex(0);
			nameTxt.setText(settings.getName());
		}
	}

	public PlayerSettings getPlayerSettings() {
		final PlayerSettings result = new PlayerSettings();
		final Player player = (Player) whoCombo.getSelectedItem();
		if (player.isHuman()) {
			result.setName(player.name);
		} else {
			final EngineSettings engine = new EngineSettings();
			engine.setName(player.engine.getName());
			result.setEngine(engine);
		}
		return result;
	}
	
	public ColorSetting getColorSetting() {
		return (ColorSetting) colorComboBox.getSelectedItem();
	}

	public void engineStarted(EngineData engine) {
		//TODO Nothing to be done
	}

	public void engineStopped(EngineData engine) {
		checkSelected();
	}
	
	private void updateSelectedPlayer() {
		Player update = (Player) whoCombo.getSelectedItem();
		if (!Objects.equals(update, currentPlayer)) {
			final boolean isHuman = update.isHuman();
			nameTxt.setEnabled(isHuman);
			nameTxt.setEditable(isHuman);
			eventFreeSetName(isHuman ? update.name : update.engine.getName()+" ("+"bot"+")");
			final Player old = currentPlayer;
			this.currentPlayer = update;
			firePropertyChange(SELECTED_PLAYER_PROPERTY_NAME, old, this.currentPlayer);
		}
	}
	
	private void updatePlayerName() {
		whoCombo.getItemAt(0).name = nameTxt.getText();
	}
	
	private void eventFreeSetName(String name) {
		nameTxt.removePropertyChangeListener(TextWidget.TEXT_PROPERTY, nameListener);
		nameTxt.setText(name);
		nameTxt.addPropertyChangeListener(TextWidget.TEXT_PROPERTY, nameListener);
	}
}
