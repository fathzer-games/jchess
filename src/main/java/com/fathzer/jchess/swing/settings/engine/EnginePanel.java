package com.fathzer.jchess.swing.settings.engine;

import java.util.Collections;
import java.util.List;

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

public class EnginePanel extends JPanel {
	private static final long serialVersionUID = 1L;

	/**
	 * Create the panel.
	 */
	public EnginePanel(List<Option<?>> options) {
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
			add(getCheck(check), ct);
		} else if (option instanceof ButtonOption button) {
			add(getButton(button), ct);
		} else {
			add(new JLabel(option.getName()+": "),ct);
			ct.gridx++;
			if (option instanceof ComboOption combo) {
				add(getCombo(combo),ct);
			} else if (option instanceof SpinOption spin) {
				add(getSpin(spin),ct);
			} else if (option instanceof StringOption string) {
				add(getString(string),ct);
			} else {
				throw new IllegalArgumentException("Type "+option.getType()+" is not supported");
			}
			ct.gridx--;
		}
	}
	
	private Component getButton(ButtonOption option) {
		// TODO Auto-generated method stub
		return new JLabel("TODO");
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
		widget.addActionListener(e -> option.setValue(widget.getValue().longValue()));
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
		text.setColumns(10);
		text.addActionListener(e -> System.out.println(text.getText()));
		return text;
	}
}
