package com.fathzer.jchess.bot.uci;

import static org.junit.jupiter.api.Assertions.*;

import java.util.Optional;

import org.junit.jupiter.api.Test;

import com.fathzer.jchess.bot.Option;
import com.fathzer.jchess.bot.options.StringOption;

class OptionParserTest {

	@Test
	void test() {
		Optional<Option<?>> ooption = OptionParser.parse("option name Book Directory type string default C:\\Program Files (x86)\\Arena\\Engines\\Dragon");
		assertTrue(ooption.isPresent());
		Option<?> option = ooption.get();
		assertEquals(StringOption.class, option.getClass());
		assertEquals("Book Directory", option.getName());
		assertEquals("C:\\Program Files (x86)\\Arena\\Engines\\Dragon", option.getValue());
		
		fail("Not yet implemented");
	}

}
