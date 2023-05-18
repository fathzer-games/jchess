package com.fathzer.jchess.swing.settings;

import java.io.IOException;

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.deser.std.StdDeserializer;
import com.fasterxml.jackson.databind.node.BooleanNode;
import com.fasterxml.jackson.databind.node.IntNode;
import com.fathzer.games.clock.ClockSettings;

public class ClockSettingsDeserializer extends StdDeserializer<ClockSettings> {
	private static final long serialVersionUID = 1L;
	
	private static final String INITIAL_TIME = "initialTime";

	private static final String INCREMENT = "increment";
	private static final String CAN_ACCUMULATE = "canAccumulate";
	private static final String MOVES_NUMBER_BEFORE_INCREMENT = "movesNumberBeforeIncrement";
	
	private static final String NEXT = "next";
	private static final String MOVES_NUMBER_BEFORE_NEXT = "movesNumberBeforeNext";
	private static final String MAX_REMAINING_KEPT = "maxRemainingKept";
	
	@SuppressWarnings("java:S110")
	private static class InvalidClockSettingsException extends JsonProcessingException {
		private static final long serialVersionUID = 1L;

		private InvalidClockSettingsException(Throwable rootCause) {
			super(rootCause);
		}
	}

	public ClockSettingsDeserializer() {
		super(ClockSettings.class);
	}

	@Override
	public ClockSettings deserialize(JsonParser jp, DeserializationContext ctxt) throws IOException {
		return deserialize(jp.getCodec().readTree(jp));
	}

	private ClockSettings deserialize(final JsonNode node) throws InvalidClockSettingsException {
		try {
			final ClockSettings result = new ClockSettings(getField(node, INITIAL_TIME, null));
			final int increment = getField(node, INCREMENT, 0);
			if (increment!=0) {
				final int movesNumberBeforeIncrement = getField(node, MOVES_NUMBER_BEFORE_INCREMENT, 1);
				final boolean canAccumulate = getBoolean(node, CAN_ACCUMULATE, true);
				result.withIncrement(increment, movesNumberBeforeIncrement, canAccumulate);
			}
			if (node.hasNonNull(NEXT)) {
				ClockSettings next = deserialize(node.get(NEXT));
				final int movesNumberBeforeNext = getField(node, MOVES_NUMBER_BEFORE_NEXT, null);
				final int maxRemainingKept = getField(node, MAX_REMAINING_KEPT, 0);
				result.withNext(movesNumberBeforeNext, maxRemainingKept, next);
			}
			return result;
		} catch (Exception e) {
			throw new InvalidClockSettingsException(e);
		}
	}
	
	private int getField(JsonNode node, String fieldName, Integer defaultValue) {
		if (node.has(fieldName)) {
			return ((IntNode) node.get(fieldName)).asInt();
		} else if (defaultValue==null) {
			throw new IllegalArgumentException(fieldName+" attribute is missing");
		} else {
			return defaultValue;
		}
	}

	private boolean getBoolean(JsonNode node, String fieldName, boolean defaultValue) {
		return node.has(fieldName) ? ((BooleanNode) node.get(fieldName)).asBoolean() : defaultValue;
	}
}
