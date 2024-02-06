package com.fathzer.jchess.bot.options;

import com.fathzer.jchess.bot.Option;

public class SpinOption<N extends Number & Comparable<N>> extends Option<N> {
	private final N min;
	private final N max;
	
	public SpinOption(String name, N defaultValue, N min, N max) {
		super(name, defaultValue);
		if (defaultValue.compareTo(min)<0 || defaultValue.compareTo(max)>0) {
			throw new IllegalArgumentException("default ("+defaultValue+") is not between min ("+min+") and max("+max+")");
		}
		this.min = min;
		this.max = max;
	}

	@Override
	public Type getType() {
		return Type.SPIN;
	}

	public N getMin() {
		return min;
	}

	public N getMax() {
		return max;
	}
}
