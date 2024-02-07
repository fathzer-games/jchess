package com.fathzer.jchess.bot.uci;

import java.io.IOException;
import java.util.EnumMap;
import java.util.HashSet;
import java.util.Set;
import java.util.function.BiFunction;

import com.fathzer.jchess.bot.Option;
import com.fathzer.jchess.bot.Option.Type;
import com.fathzer.jchess.bot.options.ButtonOption;
import com.fathzer.jchess.bot.options.CheckOption;
import com.fathzer.jchess.bot.options.ComboOption;
import com.fathzer.jchess.bot.options.SpinOption;
import com.fathzer.jchess.bot.options.StringOption;

class OptionParser {
	private static final EnumMap<Type, BiFunction<String, String[], Option<?>>> PARSERS = new EnumMap<>(Type.class);
	
	private OptionParser() {
		super();
	}
	
	static {
		PARSERS.put(Type.COMBO, (name, tokens) -> {
			final Set<String> values = new HashSet<>();
			for (int i = 6; i < tokens.length; i=i+2) {
				values.add(tokens[i]);
			}
			final String defaultValue = tokens[4];
			if (!values.contains(defaultValue)) {
				throw new IllegalArgumentException("Default value "+defaultValue+" is not in values: "+values);
			}
			return new ComboOption(name, defaultValue, values);
		});
		PARSERS.put(Type.SPIN, (name, tokens) -> new SpinOption(name, Long.parseLong(tokens[4]), Long.parseLong(tokens[6]), Long.parseLong(tokens[8])));
		PARSERS.put(Type.CHECK, (name, tokens) -> new CheckOption(name, Boolean.parseBoolean(tokens[4])));
		PARSERS.put(Type.BUTTON, (name, tokens) -> new ButtonOption(name));
		PARSERS.put(Type.STRING, (name, tokens) -> new StringOption(name, tokens[4]));
	}
	
	static Option<?> get(String[] tokens) throws IOException {
		final String optionName = tokens[0];
		final Type type = toType(tokens[2].toUpperCase());
		try {
			return PARSERS.get(type).apply(optionName, tokens);
		} catch (IllegalArgumentException e) {
			throw new IOException(e);
		}
	}
	
	private static Type toType(String type) throws IOException {
		try {
			return Type.valueOf(type);
		} catch (IllegalArgumentException e) {
			throw new IOException("Unknown option type: "+type, e);
		}
	}
}
