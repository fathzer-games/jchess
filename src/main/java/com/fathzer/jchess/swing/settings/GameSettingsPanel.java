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
	private JPanel timeDetailsPanel;
	private JPanel player1Panel;
	private JPanel player2Panel;

	/**
	 * Create the panel.
	 */
	public GameSettingsPanel() {
		GridBagLayout gridBagLayout = new GridBagLayout();
		setLayout(gridBagLayout);
		
		JLabel variantLabel = new JLabel("Variant: ");
		GridBagConstraints variantLabelGbc = new GridBagConstraints();
		variantLabelGbc.insets = new Insets(0, 0, 5, 5);
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
		variantComboGbc.insets = new Insets(0, 0, 5, 0);
		variantComboGbc.fill = GridBagConstraints.HORIZONTAL;
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
		touchMoveCheckBoxGbc.anchor = GridBagConstraints.WEST;
		touchMoveCheckBoxGbc.gridwidth = 2;
		touchMoveCheckBoxGbc.gridx = 0;
		touchMoveCheckBoxGbc.gridy = 3;
		add(touchMoveCheckBox, touchMoveCheckBoxGbc);
		
		player1Panel = new PlayerSelectionPanel();
		player1Panel.setBorder(new TitledBorder(null, "Player 1", TitledBorder.LEADING, TitledBorder.TOP, null, null));
		GridBagConstraints player1PanelGbc = new GridBagConstraints();
		player1PanelGbc.gridwidth = 2;
		player1PanelGbc.insets = new Insets(0, 0, 5, 5);
		player1PanelGbc.fill = GridBagConstraints.BOTH;
		player1PanelGbc.gridx = 0;
		player1PanelGbc.gridy = 4;
		add(player1Panel, player1PanelGbc);
		
		player2Panel = new PlayerSelectionPanel();
		player2Panel.setBorder(new TitledBorder(null, "Player 2", TitledBorder.LEADING, TitledBorder.TOP, null, null));
		GridBagConstraints player2PanelGbc = new GridBagConstraints();
		player2PanelGbc.gridwidth = 2;
		player2PanelGbc.insets = new Insets(0, 0, 5, 5);
		player2PanelGbc.fill = GridBagConstraints.BOTH;
		player2PanelGbc.gridx = 0;
		player2PanelGbc.gridy = 5;
		add(player2Panel, player2PanelGbc);
		
		timePanel = new JPanel();
		timePanel.setBorder(new TitledBorder(null, "Time control", TitledBorder.LEADING, TitledBorder.TOP, null, null));
		GridBagConstraints timePanelGridBagConstraints = new GridBagConstraints();
		timePanelGridBagConstraints.gridwidth = 2;
		timePanelGridBagConstraints.fill = GridBagConstraints.BOTH;
		timePanelGridBagConstraints.gridx = 0;
		timePanelGridBagConstraints.gridy = 6;
		add(timePanel, timePanelGridBagConstraints);
		GridBagLayout gbl_timePanel = new GridBagLayout();
		gbl_timePanel.columnWidths = new int[]{0, 0};
		gbl_timePanel.rowHeights = new int[]{0, 0, 0};
		gbl_timePanel.columnWeights = new double[]{1.0, Double.MIN_VALUE};
		gbl_timePanel.rowWeights = new double[]{0.0, 1.0, Double.MIN_VALUE};
		timePanel.setLayout(gbl_timePanel);
		
		timeControlCheckBox = new JCheckBox("Limit time");
		GridBagConstraints timeControlCheckBoxGbc = new GridBagConstraints();
		timeControlCheckBoxGbc.insets = new Insets(0, 0, 5, 0);
		timeControlCheckBoxGbc.anchor = GridBagConstraints.WEST;
		timeControlCheckBoxGbc.gridx = 0;
		timeControlCheckBoxGbc.gridy = 0;
		timePanel.add(timeControlCheckBox, timeControlCheckBoxGbc);
		
		timeDetailsPanel = new ClockSettingsPanel();
		GridBagConstraints timeDetailsPanelGbc = new GridBagConstraints();
		timeDetailsPanelGbc.fill = GridBagConstraints.BOTH;
		timeDetailsPanelGbc.gridx = 0;
		timeDetailsPanelGbc.gridy = 1;
		timePanel.add(timeDetailsPanel, timeDetailsPanelGbc);

	}

}
