package com.fathzer.jchess.bot.options;

import static org.junit.jupiter.api.Assertions.*;

import java.util.Set;
import java.util.function.BiConsumer;

import org.junit.jupiter.api.Test;

import com.fathzer.jchess.bot.Option;

class OptionsTest {
	private static class OptionListener<T> implements BiConsumer<T, T> {
		T previous;
		T newOne;
		
		@Override
		public void accept(T t, T u) {
			previous = t;
			newOne = u;
		}
	}

	@Test
	void testCombo() {
		final Set<String> abSet = Set.of("a","b");
		assertThrows(IllegalArgumentException.class, () -> new ComboOption("combo", "c", abSet));
		assertThrows(IllegalArgumentException.class, () -> new ComboOption(null, "a", abSet));
		final Option<String> combo = new ComboOption("combo", "a", Set.of("a","b"));
		assertEquals("a", combo.getValue());
		OptionListener<String> listener = new OptionListener<>();
		combo.addListener(listener);

		assertThrows(IllegalArgumentException.class, () -> combo.setValue(null));
		assertThrows(IllegalArgumentException.class, () -> combo.setValue("c"));
		assertNull(listener.previous);
		assertNull(listener.newOne);
		
		combo.setValue(combo.getValue());
		assertNull(listener.previous);
		assertNull(listener.newOne);

		combo.setValue("b");
		assertEquals("b", combo.getValue());
		assertEquals("a", listener.previous);
		assertEquals("b", listener.newOne);
	}

	@Test
	void testSpin() {
		assertThrows(IllegalArgumentException.class, () -> new SpinOption(null, 4, 0, 8));
		assertThrows(IllegalArgumentException.class, () -> new SpinOption("spin", -1, 0, 8));
		assertThrows(IllegalArgumentException.class, () -> new SpinOption("spin", 9, 0, 8));
		final Option<Long> spin = new SpinOption("spin", 4, 0, 8);
		assertEquals(4, spin.getValue());
		OptionListener<Long> listener = new OptionListener<>();
		spin.addListener(listener);

		assertThrows(IllegalArgumentException.class, () -> spin.setValue(10L));
		assertThrows(IllegalArgumentException.class, () -> spin.setValue(-2L));
		assertNull(listener.previous);
		assertNull(listener.newOne);
		
		spin.setValue(spin.getValue());
		assertNull(listener.previous);
		assertNull(listener.newOne);

		spin.setValue(0L);
		assertEquals(0, spin.getValue());
		assertEquals(4, listener.previous);
		assertEquals(0, listener.newOne);
	}

	@Test
	void testString() {
		assertThrows(IllegalArgumentException.class, () -> new StringOption("string", null));
		final Option<String> str = new StringOption("string", "a");
		assertEquals("a", str.getValue());
		OptionListener<String> listener = new OptionListener<>();
		str.addListener(listener);

		assertThrows(IllegalArgumentException.class, () -> str.setValue(null));
		assertNull(listener.previous);
		assertNull(listener.newOne);
		
		str.setValue(str.getValue());
		assertNull(listener.previous);
		assertNull(listener.newOne);

		str.setValue("b");
		assertEquals("b", str.getValue());
		assertEquals("a", listener.previous);
		assertEquals("b", listener.newOne);
	}


	@Test
	void testCheck() {
		assertThrows(IllegalArgumentException.class, () -> new CheckOption(null, false));
		final Option<Boolean> check = new CheckOption("check", true);
		assertTrue(check.getValue());
		OptionListener<Boolean> listener = new OptionListener<>();
		check.addListener(listener);

		assertThrows(IllegalArgumentException.class, () -> check.setValue(null));
		assertNull(listener.previous);
		assertNull(listener.newOne);
		
		check.setValue(true);
		assertNull(listener.previous);
		assertNull(listener.newOne);

		check.setValue(false);
		assertFalse(check.getValue());
		assertTrue(listener.previous);
		assertFalse(listener.newOne);
	}
	
	@Test
	void testButton() {
		fail("Not Yet Implemented");
	}
}
