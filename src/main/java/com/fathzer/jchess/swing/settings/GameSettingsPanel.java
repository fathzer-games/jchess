package com.fathzer.jchess.swing.settings;

import static com.fathzer.jchess.swing.settings.GameSettings.Variant;

import javax.swing.JPanel;

import com.fathzer.jchess.swing.settings.clock.ClockSettingsPanel;

import java.awt.GridBagLayout;
import javax.swing.JLabel;
import java.awt.GridBagConstraints;
import javax.swing.JComboBox;
import java.awt.Insets;
import javax.swing.JCheckBox;
import javax.swing.border.TitledBorder;

public class GameSettingsPanel extends JPanel {
	private static final long serialVersionUID = 1L;

	private JComboBox<Variant> variantCombo;
	private JCheckBox tabletModeCheckBox;
	private JCheckBox showMovesCheckBox;
	private JCheckBox touchMoveCheckBox;
	private JPanel timePanel;
	private JCheckBox timeControlCheckBox;
	private ClockSettingsPanel timeDetailsPanel;
	private PlayerSelectionPanel player1Panel;
	private PlayerSelectionPanel player2Panel;
	private JCheckBox startAfterFirstMoveCheckBox;

	/**
	 * Create the panel.
	 */
	private GameSettingsPanel() {
		GridBagLayout gridBagLayout = new GridBagLayout();
		gridBagLayout.columnWeights = new double[]{0.0, 1.0, 0.0, 1.0};
		setLayout(gridBagLayout);
		
		JLabel variantLabel = new JLabel("Variant: ");
		GridBagConstraints variantLabelGbc = new GridBagConstraints();
		variantLabelGbc.insets = new Insets(0, 5, 5, 0);
		variantLabelGbc.anchor = GridBagConstraints.WEST;
		variantLabelGbc.gridx = 0;
		variantLabelGbc.gridy = 0;
		add(variantLabel, variantLabelGbc);
		
		variantCombo = new JComboBox<>();
		for (Variant v : Variant.values()) {
			variantCombo.addItem(v);
		}
		variantCombo.setSelectedIndex(0);
		variantCombo.setEditable(true);
		GridBagConstraints variantComboGbc = new GridBagConstraints();
		variantComboGbc.anchor = GridBagConstraints.WEST;
		variantComboGbc.insets = new Insets(0, 0, 5, 0);
		variantComboGbc.gridx = 1;
		variantComboGbc.gridy = 0;
		add(variantCombo, variantComboGbc);
		
		tabletModeCheckBox = new JCheckBox("Tablet mode");
		GridBagConstraints tabletModeCheckBoxGbc = new GridBagConstraints();
		tabletModeCheckBoxGbc.insets = new Insets(0, 0, 5, 0);
		tabletModeCheckBoxGbc.anchor = GridBagConstraints.WEST;
		tabletModeCheckBoxGbc.gridwidth = 2;
		tabletModeCheckBoxGbc.gridx = 0;
		tabletModeCheckBoxGbc.gridy = 1;
		add(tabletModeCheckBox, tabletModeCheckBoxGbc);
		
		showMovesCheckBox = new JCheckBox("Show possible moves");
		GridBagConstraints showMovesCheckBoxGbc = new GridBagConstraints();
		showMovesCheckBoxGbc.insets = new Insets(0, 0, 5, 0);
		showMovesCheckBoxGbc.anchor = GridBagConstraints.WEST;
		showMovesCheckBoxGbc.gridwidth = 2;
		showMovesCheckBoxGbc.gridx = 0;
		showMovesCheckBoxGbc.gridy = 2;
		add(showMovesCheckBox, showMovesCheckBoxGbc);
		
		touchMoveCheckBox = new JCheckBox("Always move touched piece");
		GridBagConstraints touchMoveCheckBoxGbc = new GridBagConstraints();
		touchMoveCheckBoxGbc.insets = new Insets(0, 0, 5, 0);
		touchMoveCheckBoxGbc.anchor = GridBagConstraints.NORTHWEST;
		touchMoveCheckBoxGbc.gridwidth = 2;
		touchMoveCheckBoxGbc.gridx = 0;
		touchMoveCheckBoxGbc.gridy = 3;
		add(touchMoveCheckBox, touchMoveCheckBoxGbc);
		
		player1Panel = new PlayerSelectionPanel();
		player1Panel.setBorder(new TitledBorder(null, "Player 1", TitledBorder.LEADING, TitledBorder.TOP, null, null));
		GridBagConstraints player1PanelGbc = new GridBagConstraints();
		player1PanelGbc.anchor = GridBagConstraints.NORTHWEST;
		player1PanelGbc.gridwidth = 2;
		player1PanelGbc.insets = new Insets(0, 0, 5, 0);
		player1PanelGbc.gridx = 0;
		player1PanelGbc.gridy = 4;
		add(player1Panel, player1PanelGbc);
		
		player2Panel = new PlayerSelectionPanel();
		player2Panel.setColorVisible(false);
		player2Panel.setBorder(new TitledBorder(null, "Player 2", TitledBorder.LEADING, TitledBorder.TOP, null, null));
		GridBagConstraints player2PanelGbc = new GridBagConstraints();
		player2PanelGbc.anchor = GridBagConstraints.NORTHWEST;
		player2PanelGbc.gridwidth = 2;
		player2PanelGbc.insets = new Insets(0, 0, 5, 0);
		player2PanelGbc.gridx = 2;
		player2PanelGbc.gridy = 4;
		add(player2Panel, player2PanelGbc);
		
		timePanel = new JPanel();
		timePanel.setBorder(new TitledBorder(null, "Time control", TitledBorder.LEADING, TitledBorder.TOP, null, null));
		GridBagConstraints timePanelGridBagConstraints = new GridBagConstraints();
		timePanelGridBagConstraints.gridheight = 4;
		timePanelGridBagConstraints.gridwidth = 2;
		timePanelGridBagConstraints.fill = GridBagConstraints.BOTH;
		timePanelGridBagConstraints.gridx = 2;
		timePanelGridBagConstraints.gridy = 0;
		add(timePanel, timePanelGridBagConstraints);
		GridBagLayout timePanelGbl = new GridBagLayout();
		timePanelGbl.columnWidths = new int[]{0, 0};
		timePanelGbl.rowHeights = new int[]{0, 0, 0, 0};
		timePanelGbl.columnWeights = new double[]{1.0, Double.MIN_VALUE};
		timePanelGbl.rowWeights = new double[]{0.0, 1.0, 0.0, Double.MIN_VALUE};
		timePanel.setLayout(timePanelGbl);
		
		timeControlCheckBox = new JCheckBox("Limit time");
		timeControlCheckBox.setSelected(true);
		GridBagConstraints timeControlCheckBoxGbc = new GridBagConstraints();
		timeControlCheckBoxGbc.insets = new Insets(0, 0, 5, 0);
		timeControlCheckBoxGbc.anchor = GridBagConstraints.WEST;
		timeControlCheckBoxGbc.gridx = 0;
		timeControlCheckBoxGbc.gridy = 0;
		timePanel.add(timeControlCheckBox, timeControlCheckBoxGbc);
		
		startAfterFirstMoveCheckBox = new JCheckBox("Start after first move");
		GridBagConstraints startAfterFirstMoveCheckBoxGbc = new GridBagConstraints();
		startAfterFirstMoveCheckBoxGbc.anchor = GridBagConstraints.WEST;
		startAfterFirstMoveCheckBoxGbc.insets = new Insets(0, 0, 5, 0);
		startAfterFirstMoveCheckBoxGbc.gridx = 0;
		startAfterFirstMoveCheckBoxGbc.gridy = 1;
		timePanel.add(startAfterFirstMoveCheckBox, startAfterFirstMoveCheckBoxGbc);
		
		timeDetailsPanel = new ClockSettingsPanel();
		GridBagConstraints timeDetailsPanelGbc = new GridBagConstraints();
		timeDetailsPanelGbc.insets = new Insets(0, 5, 0, 0);
		timeDetailsPanelGbc.anchor = GridBagConstraints.NORTHWEST;
		timeDetailsPanelGbc.gridx = 0;
		timeDetailsPanelGbc.gridy = 2;
		timePanel.add(timeDetailsPanel, timeDetailsPanelGbc);
		
		timeControlCheckBox.addItemListener(e -> {
			boolean enabled = timeControlCheckBox.isSelected();
			timeDetailsPanel.setEnabled(enabled);
			startAfterFirstMoveCheckBox.setEnabled(enabled);
		});
	}
	
	public GameSettingsPanel(GameSettings settings) {
		this();
		setSettings(settings);
	}
	
	private void setSettings(GameSettings settings) {
		boolean hasClock = settings.getClock()!=null;
		this.timeControlCheckBox.setSelected(hasClock);
	}
}
