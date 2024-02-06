package com.fathzer.jchess.bot;

public abstract class Option<T> {
	public enum Type {
		CHECK, SPIN, COMBO, BUTTON, STRING
	}
	
	private final String name;
	private T defaultValue;

	protected Option(String name, T defaultValue) {
		if (name==null || defaultValue==null) {
			throw new IllegalArgumentException();
		}
		this.name = name;
		this.defaultValue = defaultValue;
	}

	public String getName() {
		return name;
	}
	
	public abstract Type getType();
	
	public T getDefaultValue() {
		return defaultValue;
	}
}
