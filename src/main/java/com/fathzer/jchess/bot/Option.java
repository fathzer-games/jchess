package com.fathzer.jchess.bot;

public abstract class Option<T> {
	enum Type {
		CHECK, SPIN, COMBO, BUTTON, STRING
	}
	
	private final String name;
	private T defaultValue;

	Option(String name, T defaultValue) {
		if (name==null || defaultValue==null) {
			throw new IllegalArgumentException();
		}
		this.name = name;
		this.defaultValue = defaultValue;
	}

	public String getName() {
		return name;
	}
	
	abstract Type getType();
	
	public T getDefaultValue() {
		return defaultValue;
	}
}
