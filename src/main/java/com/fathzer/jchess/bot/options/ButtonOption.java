package com.fathzer.jchess.bot.options;

import com.fathzer.jchess.bot.Option;

public class ButtonOption extends Option<Void> {
	
	public ButtonOption(String name) {
		super(name, null);
	}
	
	@Override
	public Type getType() {
		return Type.BUTTON;
	}

	@Override
	public boolean isValid(Void value) {
		return true;
	}

	@Override
	public void setValue(Void value) {
		super.fireChange(value, value);
	}
}
