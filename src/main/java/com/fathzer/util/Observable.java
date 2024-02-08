package com.fathzer.util;

import java.util.Collection;
import java.util.LinkedList;
import java.util.function.BiConsumer;

public class Observable<T> {
	private Collection<BiConsumer<T,T>> listeners;
	private T value;
	
	public Observable(T value) {
		this.value = value;
		this.listeners = new LinkedList<>();
	}

	public void addListener(BiConsumer<T,T> listener) {
		this.listeners.add(listener);
	}
	
	public void setValue(T value) {
		if (!this.value.equals(value)) {
			final T previous = this.value;
			this.value = value;
			fireChange(previous, value);
		}
	}

	protected void fireChange(final T previous, T value) {
		listeners.forEach(c -> c.accept(previous, value));
	}
	
	public T getValue() {
		return this.value;
	}
}
