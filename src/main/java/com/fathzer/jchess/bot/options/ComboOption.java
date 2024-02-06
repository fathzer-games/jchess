package com.fathzer.jchess.bot.options;

import java.util.Set;

import com.fathzer.jchess.bot.Option;

public class ComboOption extends Option<String> {
	private final Set<String> values;
	
	public ComboOption(String name, String defaultValue, Set<String> values) {
		super(name, defaultValue);
		if (!values.contains(defaultValue) || values.isEmpty()) {
			throw new IllegalArgumentException();
		}
		this.values = values;
	}

	public Set<String> getValues() {
		return values;
	}

	@Override
	public Type getType() {
		return Type.COMBO;
	}
}
