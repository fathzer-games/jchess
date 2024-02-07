package com.fathzer.jchess.bot;

import com.fathzer.util.Observable;

public abstract class Option<T> extends Observable<T> {
	public enum Type {
		CHECK, SPIN, COMBO, BUTTON, STRING
	}
	
	private final String name;

	protected Option(String name, T defaultValue) {
		super(defaultValue);
		if (name==null || defaultValue==null) {
			throw new IllegalArgumentException();
		}
		this.name = name;
	}

	public String getName() {
		return name;
	}
	
	public abstract Type getType();
	protected abstract boolean isValid(T value);
	
	@Override
	public void setValue(T value) {
		if (!isValid(value)) {
			throw new IllegalArgumentException();
		}
		super.setValue(value);
	}
}
