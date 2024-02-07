package com.fathzer.jchess.bot.options;

import com.fathzer.jchess.bot.Option;

public class SpinOption extends Option<Long> {
	private final long min;
	private final long max;
	
	public SpinOption(String name, long defaultValue, long min, long max) {
		super(name, defaultValue);
		if (defaultValue<min || defaultValue>max) {
			throw new IllegalArgumentException("default ("+defaultValue+") is not between min ("+min+") and max("+max+")");
		}
		this.min = min;
		this.max = max;
	}

	@Override
	public Type getType() {
		return Type.SPIN;
	}

	public long getMin() {
		return min;
	}

	public long getMax() {
		return max;
	}

	@Override
	public boolean isValid(Long value) {
		return min <= value && max >= value;
	}
}
