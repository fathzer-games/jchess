package com.fathzer.jchess.bot.options;

import com.fathzer.jchess.bot.Option;

//TODO Is it usefull?
//FIXME it will never inform listener as value of Void type can't change
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
}
