package com.fathzer.jchess.bot.options;

import com.fathzer.jchess.bot.Option;

//TODO Is it usefull?
public class ButtonOption extends Option<Void> {
	
	public ButtonOption(String name) {
		super(name, null);
	}
	
	@Override
	public Type getType() {
		return Type.BUTTON;
	}
}
