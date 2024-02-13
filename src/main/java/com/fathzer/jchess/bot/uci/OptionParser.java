package com.fathzer.jchess.bot.uci;

import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;

import com.fathzer.jchess.bot.Option;
import com.fathzer.jchess.bot.Option.Type;
import com.fathzer.jchess.bot.options.ButtonOption;
import com.fathzer.jchess.bot.options.CheckOption;
import com.fathzer.jchess.bot.options.ComboOption;
import com.fathzer.jchess.bot.options.SpinOption;
import com.fathzer.jchess.bot.options.StringOption;

class OptionParser {
	static final String OPTION_PREFIX = "option ";
	private static final String NAME_PREFIX = " name ";
	private static final String TYPE_PREFIX = " type ";
	private static final String DEFAULT_PREFIX = " default ";
	private static final String VAR_PREFIX = " var ";
	private static final String MIN_PREFIX = " min ";
	private static final String MAX_PREFIX = " max ";
	
	private final String line;
	private String current;
	private OptionParser(String line) {
		this.line = line;
		this.current = this.line.substring(OPTION_PREFIX.length()-1);
	}
	
	static Optional<Option<?>> parse(String optionString) {
		if (!optionString.startsWith(OPTION_PREFIX)) {
			return Optional.empty();
		}
		return Optional.of(new OptionParser(optionString).get());
	}
	
	private Option<?> get() {
		final String name = getToken(NAME_PREFIX, Arrays.asList(TYPE_PREFIX, DEFAULT_PREFIX, MAX_PREFIX, MIN_PREFIX, VAR_PREFIX));
		final Type type = Type.valueOf(getToken(TYPE_PREFIX, Arrays.asList(DEFAULT_PREFIX, MAX_PREFIX, MIN_PREFIX, VAR_PREFIX)).toUpperCase());
		if (type==Type.BUTTON) {
			return new ButtonOption(name);
		}
		final String defaultValue = getToken(DEFAULT_PREFIX, Arrays.asList(MAX_PREFIX, MIN_PREFIX, VAR_PREFIX));
		if (type==Type.STRING) {
			return new StringOption(name, emptyable(defaultValue));
		}
		if (type==Type.CHECK) {
			return new CheckOption(name, Boolean.parseBoolean(defaultValue));
		}
		if (type==Type.COMBO) {
			return buildComboOption(name, defaultValue);
		}
		if (type==Type.SPIN) {
			final long min = Long.parseLong(getToken(MIN_PREFIX, Arrays.asList(MAX_PREFIX)));
			final long max = Long.parseLong(getToken(MAX_PREFIX, Arrays.asList(MIN_PREFIX)));
			return new SpinOption(name, Long.parseLong(defaultValue), min, max);
		}
		throw new UnsupportedOperationException("No option for type "+type);
	}
	
	private ComboOption buildComboOption(String name, String defaultValue) {
		final Set<String> values = new HashSet<>();
		while (current.indexOf(VAR_PREFIX)>=0) {
			values.add(emptyable(getToken(VAR_PREFIX, Collections.singletonList(VAR_PREFIX))));
		}
		return new ComboOption(name, emptyable(defaultValue), values);
	}
	
	private String getToken(String searched, List<String> delimiters) {
		final int startIndex = current.indexOf(searched);
		if (startIndex<0) {
			throw new IllegalArgumentException("No option"+searched+"declaration in "+line);
		}
		final String remaining = current.substring(startIndex+searched.length());
		int endIndex = remaining.length();
		for (String delim : delimiters) {
			int index = remaining.indexOf(delim);
			if (index>=0 && index<endIndex) {
				endIndex = index;
			}
		}
		current = current.substring(0, startIndex)+remaining.substring(endIndex);
		return remaining.substring(0, endIndex);
	}
	
	private static String emptyable(String value) {
		return "<EMPTY>".equalsIgnoreCase(value) ? "" : value;
	}
}
