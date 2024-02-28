package com.fathzer.jchess.swing.settings;

import javax.swing.JPanel;
import javax.swing.JScrollPane;

import java.awt.GridBagLayout;
import javax.swing.border.TitledBorder;

import com.fathzer.jchess.bot.uci.EngineLoader.EngineData;
import com.fathzer.soft.ajlib.swing.Utils;

import javax.swing.border.EtchedBorder;
import java.awt.Color;
import java.awt.Component;

import javax.swing.JList;
import javax.swing.JOptionPane;

import java.awt.GridBagConstraints;

import javax.swing.DefaultListCellRenderer;
import javax.swing.JButton;
import java.awt.Insets;
import java.io.IOException;
import java.util.List;
import java.util.function.Predicate;
import java.awt.BorderLayout;
import javax.swing.ListSelectionModel;

public class EnginesPanel extends JPanel {
	static final String STARTED_PROPERTY_NAME = "Started engines"; 
	
	private static final class Renderer extends DefaultListCellRenderer {
		private static final long serialVersionUID = 1L;

		@Override
		public Component getListCellRendererComponent(JList<?> list, Object value, int index, boolean isSelected, boolean cellHasFocus) {
			return super.getListCellRendererComponent(list, ((EngineData)value).getName(), index, isSelected, cellHasFocus);
		}
	}

	private static final long serialVersionUID = 1L;
	private JPanel startedPanel;
	private JList<EngineData> startedList;
	private JButton configureEngineButton;
	private JPanel availablePanel;
	private JList<EngineData> availableList;
	private JPanel centerPanel;
	private JButton startButton;
	private JButton stopButton;
	
	private transient List<EngineData> data;
	private Predicate<EngineData> isLocked;

	private EnginesPanel() {
		GridBagLayout gridBagLayout = new GridBagLayout();
		setLayout(gridBagLayout);
		
		startedPanel = new JPanel();
		startedPanel.setBorder(new TitledBorder(new EtchedBorder(EtchedBorder.LOWERED, new Color(255, 255, 255), new Color(160, 160, 160)), "Started", TitledBorder.LEADING, TitledBorder.TOP, null, new Color(0, 0, 0)));
		GridBagConstraints startedPanelGridBagConstraints = new GridBagConstraints();
		startedPanelGridBagConstraints.weighty = 1.0;
		startedPanelGridBagConstraints.weightx = 1.0;
		startedPanelGridBagConstraints.fill = GridBagConstraints.BOTH;
		startedPanelGridBagConstraints.gridx = 0;
		startedPanelGridBagConstraints.gridy = 0;
		add(startedPanel, startedPanelGridBagConstraints);
		GridBagLayout startedPanelGbl = new GridBagLayout();
		startedPanel.setLayout(startedPanelGbl);
		
		startedList = new JList<>();
		GridBagConstraints startedListGbc = new GridBagConstraints();
		startedListGbc.weightx = 1.0;
		startedListGbc.weighty = 1.0;
		startedListGbc.insets = new Insets(0, 0, 5, 0);
		startedListGbc.fill = GridBagConstraints.BOTH;
		startedListGbc.gridx = 0;
		startedListGbc.gridy = 0;
		startedPanel.add(new JScrollPane(startedList), startedListGbc);
		
		configureEngineButton = new JButton("Settings");
		configureEngineButton.setEnabled(false);
		GridBagConstraints configureEngineButtonGbc = new GridBagConstraints();
		configureEngineButtonGbc.anchor = GridBagConstraints.WEST;
		configureEngineButtonGbc.gridx = 0;
		configureEngineButtonGbc.gridy = 1;
		startedPanel.add(configureEngineButton, configureEngineButtonGbc);
		
		centerPanel = new JPanel();
		GridBagConstraints centerPanelGbc = new GridBagConstraints();
		centerPanelGbc.insets = new Insets(0, 5, 0, 5);
		centerPanelGbc.fill = GridBagConstraints.BOTH;
		centerPanelGbc.gridx = 1;
		centerPanelGbc.gridy = 0;
		add(centerPanel, centerPanelGbc);
		GridBagLayout centerPanelGbl = new GridBagLayout();
		centerPanel.setLayout(centerPanelGbl);
		
		startButton = new JButton("Start");
		startButton.setEnabled(false);
		GridBagConstraints startButtonGbc = new GridBagConstraints();
		startButtonGbc.anchor = GridBagConstraints.SOUTH;
		startButtonGbc.weighty = 1.0;
		startButtonGbc.insets = new Insets(0, 0, 5, 0);
		startButtonGbc.gridx = 0;
		startButtonGbc.gridy = 0;
		centerPanel.add(startButton, startButtonGbc);
		
		stopButton = new JButton("Stop");
		stopButton.setEnabled(false);
		GridBagConstraints stopButtonGbc = new GridBagConstraints();
		stopButtonGbc.anchor = GridBagConstraints.NORTH;
		stopButtonGbc.weighty = 1.0;
		stopButtonGbc.gridx = 0;
		stopButtonGbc.gridy = 1;
		centerPanel.add(stopButton, stopButtonGbc);
		
		availablePanel = new JPanel();
		availablePanel.setBorder(new TitledBorder(new EtchedBorder(EtchedBorder.LOWERED, new Color(255, 255, 255), new Color(160, 160, 160)), "Available", TitledBorder.LEADING, TitledBorder.TOP, null, new Color(0, 0, 0)));
		GridBagConstraints availablePanelGbc = new GridBagConstraints();
		availablePanelGbc.weightx = 1.0;
		availablePanelGbc.fill = GridBagConstraints.BOTH;
		availablePanelGbc.gridx = 2;
		availablePanelGbc.gridy = 0;
		add(availablePanel, availablePanelGbc);
		availablePanel.setLayout(new BorderLayout(0, 0));
		
		availableList = new JList<>();
		availableList.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
		availablePanel.add(new JScrollPane(availableList), BorderLayout.CENTER);
		
		availableList.setCellRenderer(new Renderer());
		availableList.addListSelectionListener(e -> {
			if (!e.getValueIsAdjusting()) {
				startButton.setEnabled(availableList.getSelectedIndex()>=0);
			}
		});
		
		startButton.addActionListener(e -> doStart());
		
		startedList.setCellRenderer(new Renderer());
		startedList.addListSelectionListener(e -> {
			if (!e.getValueIsAdjusting()) {
				final boolean sel = startedList.getSelectedIndex()>=0;
				stopButton.setEnabled(sel);
				configureEngineButton.setEnabled(sel);
			}
		});
		stopButton.addActionListener(e -> doStop());
		configureEngineButton.addActionListener(e -> doConfigure());
	}
	
	/**
	 * Create the panel.
	 */
	public EnginesPanel(List<EngineData> data, Predicate<EngineData> isLocked) {
		this();
		this.data = data;
		this.isLocked = isLocked;
		refreshLists();
	}
	
	private void refreshLists() {
		availableList.setListData(data.stream().filter(e->e.getEngine()==null).toArray(EngineData[]::new));
		startedList.setListData(data.stream().filter(e -> e.getEngine()!=null).toArray(EngineData[]::new));
	}

	private void doStart() {
		final EngineData engine = availableList.getSelectedValue();
		try {
			engine.start();
			firePropertyChange(STARTED_PROPERTY_NAME, null, engine);
			refreshLists();
		} catch (IOException e) {
			JOptionPane.showMessageDialog(Utils.getOwnerWindow(this), "An error occurred while starting "+engine.getName()+" engine", "Error", JOptionPane.ERROR_MESSAGE);
		}
	}

	private void doStop() {
		final EngineData engine = startedList.getSelectedValue();
		if (isLocked.test(engine)) {
			JOptionPane.showMessageDialog(Utils.getOwnerWindow(this), "Sorry, you can't stop an engine selected in the '"+"Game settings"+"' panel");
			return;
		}
		try {
			engine.stop();
			firePropertyChange(STARTED_PROPERTY_NAME, engine, null);
			refreshLists();
		} catch (IOException e) {
			JOptionPane.showMessageDialog(Utils.getOwnerWindow(this), "An exception occurred while closing "+engine.getName()+" engine", "Error", JOptionPane.ERROR_MESSAGE);
		}
	}

	private void doConfigure() {
		final EngineDialog dialog = new EngineDialog(Utils.getOwnerWindow(this), startedList.getSelectedValue().getEngine());
		dialog.setVisible(true);
	}
}
