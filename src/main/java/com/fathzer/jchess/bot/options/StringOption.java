package com.fathzer.jchess.bot.options;

import com.fathzer.jchess.bot.Option;

public class StringOption extends Option<String> {
	public StringOption(String name, String defaultValue) {
		super(name, defaultValue);
		if (defaultValue==null) {
			throw new IllegalArgumentException();
		}
	}

	@Override
	public Type getType() {
		return Type.STRING;
	}

	@Override
	public boolean isValid(String value) {
		return value!=null;
	}
}
