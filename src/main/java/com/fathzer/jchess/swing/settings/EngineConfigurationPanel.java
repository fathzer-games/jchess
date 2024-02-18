package com.fathzer.jchess.swing.settings;

import java.util.Collections;
import java.util.List;

import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JPanel;

import com.fathzer.jchess.bot.Option;
import com.fathzer.jchess.bot.options.ButtonOption;
import com.fathzer.jchess.bot.options.CheckOption;
import com.fathzer.jchess.bot.options.ComboOption;
import com.fathzer.jchess.bot.options.SpinOption;
import com.fathzer.jchess.bot.options.StringOption;
import com.fathzer.soft.ajlib.swing.widget.IntegerWidget;
import com.fathzer.soft.ajlib.swing.widget.TextWidget;

import java.awt.Component;
import java.awt.GridBagConstraints;
import java.awt.Insets;
import java.math.BigInteger;
import java.awt.GridBagLayout;

/** A panel to configure an engine.
 */
class EngineConfigurationPanel extends JPanel {
	private static final long serialVersionUID = 1L;

	/**
	 * Create the panel.
	 */
	EngineConfigurationPanel(List<Option<?>> options) {
		setLayout(new GridBagLayout());
		if (options==null) {
			options = Collections.emptyList();
		}
		GridBagConstraints ct = new GridBagConstraints();
		ct.gridx = 1;
		ct.anchor = GridBagConstraints.WEST;
		ct.insets = new Insets(0,0,0,0);
		for (Option<?> option : options) {
			ct.gridy++;
			addComponent(option, ct);
			ct.insets.top = 5;
		}
	}

	private void addComponent(Option<?> option, GridBagConstraints ct) {
		if (option instanceof CheckOption check) {
			ct.gridwidth = 2;
			add(getCheck(check), ct);
		} else if (option instanceof ButtonOption button) {
			ct.gridwidth = 2;
			add(getButton(button), ct);
		} else {
			add(new JLabel(option.getName()+": "), ct);
			ct.gridx++;
			if (option instanceof ComboOption combo) {
				add(getCombo(combo), ct);
			} else if (option instanceof SpinOption spin) {
				add(getSpin(spin), ct);
			} else if (option instanceof StringOption string) {
				ct.weightx = 1;
				ct.fill = GridBagConstraints.HORIZONTAL;
				add(getString(string), ct);
				ct.fill = GridBagConstraints.NONE;
				ct.weightx = 0;
			} else {
				throw new IllegalArgumentException("Type "+option.getType()+" is not supported");
			}
			ct.gridx--;
		}
		ct.gridwidth = 1;
	}
	
	private Component getButton(ButtonOption option) {
		final JButton button = new JButton(option.getName());
		button.addActionListener(e -> option.setValue(null));
		return button;
	}

	private Component getCheck(CheckOption option) {
		final JCheckBox box = new JCheckBox(option.getName());
		box.setSelected(option.getValue());
		box.addActionListener(e -> option.setValue(box.isSelected()));
		return box;
	}

	private Component getCombo(ComboOption option) {
		final JComboBox<String> comboBox = new JComboBox<>();
		for (String string : option.getValues()) {
			comboBox.addItem(string);
		}
		comboBox.setSelectedItem(option.getValue());
		comboBox.addActionListener(e -> option.setValue((String)comboBox.getSelectedItem()));
		return comboBox;
	}

	private Component getSpin(SpinOption option) {
		final IntegerWidget widget = new IntegerWidget(BigInteger.valueOf(option.getMin()), BigInteger.valueOf(option.getMax()));
		widget.setValue(BigInteger.valueOf(option.getValue()));
		widget.addPropertyChangeListener(IntegerWidget.VALUE_PROPERTY, e -> {
			final BigInteger value = widget.getValue();
			if (value!=null) {
				option.setValue(value.longValue());
			}
		});
		widget.setColumns(getWidgetColumnCount(option));
		return widget;
	}
	
	private int getWidgetColumnCount(SpinOption option) {
		int count = Math.max(2, Long.toString(option.getMin()).length());
		return Math.max(count, Long.toString(option.getMax()).length());
	}

	private Component getString(StringOption option) {
		final TextWidget text = new TextWidget();
		text.setText(option.getValue());
		text.addPropertyChangeListener(TextWidget.TEXT_PROPERTY, e -> option.setValue(text.getText()));
		return text;
	}
}
