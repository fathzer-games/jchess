package com.fathzer.jchess.swing;

import static org.junit.jupiter.api.Assertions.*;

import java.io.IOException;

import org.junit.jupiter.api.Test;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fathzer.games.clock.ClockSettings;

class ClockSettingsDeserializerTest {

	@Test
	void test() throws IOException {
		assertThrows(JsonProcessingException.class, () -> get("{}"));
		assertThrows(JsonProcessingException.class, () -> get("{'initialTime':'notANumber'}"));
		ClockSettings settings = get("{'initialTime':10}");
		assertEquals(10, settings.getInitialTime());
		assertEquals(0, settings.getIncrement());
		assertNull(settings.getNext());
		
		settings = get("{'initialTime':10,'increment':3}");
		assertEquals(10, settings.getInitialTime());
		assertEquals(3, settings.getIncrement());
		assertEquals(1, settings.getMovesNumberBeforeIncrement());
		assertTrue(settings.isCanAccumulate());

		settings = get("{'initialTime':12,'increment':5, 'canAccumulate':false, 'movesNumberBeforeIncrement':2}");
		assertEquals(12, settings.getInitialTime());
		assertEquals(5, settings.getIncrement());
		assertEquals(2, settings.getMovesNumberBeforeIncrement());
		assertFalse(settings.isCanAccumulate());

		assertThrows(JsonProcessingException.class, () -> get("{'initialTime':'notANumber','increment':-3}}"));

		settings = get("{'initialTime':12,'next':null}");
		assertNull(settings.getNext());

		settings = get("{'initialTime':3600, 'movesNumberBeforeNext':40, 'next':{'initialTime':180}}");
		assertEquals(3600, settings.getInitialTime());
		assertEquals(40, settings.getMovesNumberBeforeNext());
		assertEquals(0, settings.getMaxRemainingKept());
		settings = settings.getNext();
		assertEquals(180, settings.getInitialTime());

		settings = get("{'initialTime':3600, 'movesNumberBeforeNext':40, 'maxRemainingKept':120, 'next':{'initialTime':180}}");
		assertEquals(120, settings.getMaxRemainingKept());
		
		assertThrows(JsonProcessingException.class, () -> get("{'initialTime':3600, 'next':{'initialTime':180}}"));

	}

	private ClockSettings get(String json) throws IOException {
		return GameSettings.MAPPER.readValue(json.replace('\'', '"'), ClockSettings.class);
	}
}
