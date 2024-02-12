package com.fathzer.jchess.bot.uci;

import java.util.EnumMap;
import java.util.HashSet;
import java.util.Optional;
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
	static final String OPTION_PREFIX = "option name ";
	
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
		PARSERS.put(Type.SPIN, (name, tokens) -> new SpinOption(name, Long.parseLong(getAfter(name, tokens, "default")), Long.parseLong(getAfter(name, tokens, "min")), Long.parseLong(getAfter(name, tokens, "max"))));
		PARSERS.put(Type.CHECK, (name, tokens) -> new CheckOption(name, Boolean.parseBoolean(tokens[4])));
		PARSERS.put(Type.BUTTON, (name, tokens) -> new ButtonOption(name));
		PARSERS.put(Type.STRING, (name, tokens) -> new StringOption(name, tokens[4]));
	}
	
	private static String getAfter(String name, String[] tokens, String token) {
		for (int i = 0; i < tokens.length; i++) {
			if (token.equals(tokens[i])) {
				if (i+1<tokens.length) {
					return tokens[i+1];
				}
			}
		}
		throw new IllegalArgumentException("Option "+name+" does not have "+token);
	}
	
	static Optional<Option<?>> parse(String optionString) {
		if (!optionString.startsWith(OPTION_PREFIX)) {
			return Optional.empty();
		}
		String current = optionString.substring(OPTION_PREFIX.length());
		final String typeToken = " type ";
		final int index = current.indexOf(typeToken);
		if (index<0) {
			throw new IllegalArgumentException("No option type declaration in "+optionString);
		}
		if (index==0) {
			throw new IllegalArgumentException("No option name in "+optionString);
		}
		final String name = current.substring(0, index);
		current = current.substring(name.length()+typeToken.length()).trim();
		// FIXME
		String[] tokens = current.split(" ");
		return Optional.of(get(name, tokens));
	}
	
	static Option<?> get(String optionName, String[] tokens) {
		final Type type = Type.valueOf(tokens[0].toUpperCase());
		return PARSERS.get(type).apply(optionName, tokens);
	}
}
