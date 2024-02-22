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
import com.fathzer.soft.ajlib.swing.widget.TextWidget;

import java.awt.Insets;
import java.beans.PropertyChangeListener;
import java.util.List;
import java.util.Objects;

import javax.swing.JComboBox;

/** A panel to select player.
 * //TODO Does work well when variant changes. Maybe (not sure) we should keep the whole engine list in memory,
 * maybe display all engines including those that does not support current variant, but disabling them
 * (see last answer of https://stackoverflow.com/questions/23722706/how-to-disable-certain-items-in-a-jcombobox
 * to have a combo box with disabled items.   
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

		@Override
		public String toString() {
			return engine==null ? "Human" : engine.getName();
		}
	}
	
	private static final long serialVersionUID = 1L;
	public static final String SELECTED_PLAYER_PROPERTY_NAME = "selected player";
	
	private JLabel nameLabel;
	private TextWidget nameTxt;
	private JLabel typeLabel;
	private JComboBox<Player> whoCombo;
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
		
		whoCombo = new JComboBox<>();
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
		this.currentVariant = variant;
	}
	
	public void setSettings(PlayerSettings settings, List<EngineData> engines, Variant variant) {
		this.currentVariant = variant;
		whoCombo.removeAllItems();
		final String name = settings.getName()==null ? "" : settings.getName();
		whoCombo.addItem(new Player(name));
		engines.forEach(e -> {
			if (e.getEngine()!=null && e.getEngine().isSupported(variant)) {
				whoCombo.addItem(new Player(e));
			}
		});
		final EngineSettings engine = settings.getEngine();
		boolean found = false;
		if (engine!=null) {
			for (int i = 1; i < whoCombo.getItemCount(); i++) {
				Player player = whoCombo.getItemAt(i);
				if (player.name.equals(engine.getName()) && player.engine.getEngine().isSupported(variant)) {
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
		if (player.engine==null) {
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
		if (engine.getEngine().isSupported(currentVariant)) {
			whoCombo.addItem(new Player(engine));
		}
	}

	public void engineStopped(EngineData engine) {
		for (int i = 1; i < whoCombo.getItemCount(); i++) {
			if (engine.getName().equals(whoCombo.getItemAt(i).name)) {
				whoCombo.remove(i);
				break;
			}
		}
	}
	
	private void updateSelectedPlayer() {
		Player update = (Player) whoCombo.getSelectedItem();
		if (!Objects.equals(update, currentPlayer)) {
			final boolean isHuman = update.engine==null;
			nameTxt.setEnabled(isHuman);
			nameTxt.setEditable(isHuman);
			eventFreeSetName(isHuman ? update.name : update.engine.getName()+" ("+"bot"+")");
			final Player old = currentPlayer;
			this.currentPlayer = update;
			firePropertyChange(SELECTED_PLAYER_PROPERTY_NAME, old, this.currentPlayer);
		}
	}
	
	private void updatePlayerName() {
		System.out.println("name changed");
		whoCombo.getItemAt(0).name = nameTxt.getText();
	}
	
	private void eventFreeSetName(String name) {
		nameTxt.removePropertyChangeListener(TextWidget.TEXT_PROPERTY, nameListener);
		nameTxt.setText(name);
		nameTxt.addPropertyChangeListener(TextWidget.TEXT_PROPERTY, nameListener);
	}
}
