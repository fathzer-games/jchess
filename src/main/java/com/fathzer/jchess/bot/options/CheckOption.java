package com.fathzer.jchess.bot.options;

import com.fathzer.jchess.bot.Option;

public class CheckOption extends Option<Boolean> {
	public CheckOption(String name, boolean defaultValue) {
		super(name, defaultValue);
	}

	@Override
	public Type getType() {
		return Type.CHECK;
	}
}
