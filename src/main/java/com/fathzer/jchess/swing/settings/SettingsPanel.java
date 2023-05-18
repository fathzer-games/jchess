package com.fathzer.jchess.swing.settings;

import javax.swing.JPanel;
import javax.swing.JTextArea;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;

import com.fasterxml.jackson.core.JsonProcessingException;

import java.awt.BorderLayout;
import java.io.UncheckedIOException;
import javax.swing.JScrollPane;

public class SettingsPanel extends JPanel {
	private static final long serialVersionUID = 1L;
	public static final String VALID_SETTINGS_PROPERTY = "validSettings";
	
	private JTextArea textArea;
	private transient GameSettings settings;

	/**
	 * Create the panel.
	 */
	public SettingsPanel() {
		setLayout(new BorderLayout(0, 0));
		textArea = new JTextArea();
		textArea.getDocument().addDocumentListener(new DocumentListener() {
			
			@Override
			public void removeUpdate(DocumentEvent e) {
				textChanged(e);
			}
			
			@Override
			public void insertUpdate(DocumentEvent e) {
				textChanged(e);
			}
			
			@Override
			public void changedUpdate(DocumentEvent e) {
				textChanged(e);
			}
		});
		JScrollPane scrollPane = new JScrollPane(textArea);
		add(scrollPane);
	}

	private void textChanged(DocumentEvent e) {
		final boolean old = this.settings!=null;
		try {
			this.settings = GameSettings.MAPPER.readValue(textArea.getText(), GameSettings.class);
		} catch (JsonProcessingException e1) {
			this.settings = null;
		}
		if (old != (this.settings!=null)) {
			firePropertyChange(VALID_SETTINGS_PROPERTY, old, this.settings!=null);
		}
	}

	public void setSettings(GameSettings settings) {
		try {
			this.textArea.setText(GameSettings.MAPPER.writerWithDefaultPrettyPrinter().writeValueAsString(settings));
			this.settings = settings;
		} catch (JsonProcessingException e) {
			throw new UncheckedIOException(e);
		}
	}

	public GameSettings getSettings() {
		return settings;
	}
}
