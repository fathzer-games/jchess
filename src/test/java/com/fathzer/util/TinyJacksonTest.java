package com.fathzer.util;

import static org.junit.jupiter.api.Assertions.*;

import org.json.JSONObject;
import org.junit.jupiter.api.Test;

import com.fathzer.games.Color;

import lombok.AllArgsConstructor;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;

class TinyJacksonTest {
	@NoArgsConstructor
	@Getter
	@Setter
	@ToString
	@AllArgsConstructor
	@EqualsAndHashCode
	public static class TestClass {
		private String str;
		private int anInt;
		private long aLong;
		private char aChar;
		private String[] strs;
		private OtherClass other;
	}
	
	@AllArgsConstructor
	@NoArgsConstructor
	@Setter
	@Getter
	@ToString
	@EqualsAndHashCode
	public static class OtherClass {
		private Color color;
		private double value;
	}

	@Test
	void test() {
		final TestClass obj = new TestClass("x",1,2,'a',new String[] {"a","b"}, new OtherClass(Color.WHITE,2.0));
		final JSONObject json = TinyJackson.toJSONObject(obj);
		final String jsonString = json.toString();
		assertEquals(obj, TinyJackson.toObject(json, TestClass.class));
		assertEquals(obj, TinyJackson.toObject(new JSONObject(jsonString), TestClass.class));
	}
	
	@Test
	void testNull() {
		TestClass obj = new TestClass(null,1,2,'a',null, new OtherClass(null,2.0));
		JSONObject json = TinyJackson.toJSONObject(obj);
		assertEquals(obj, TinyJackson.toObject(json, TestClass.class));
		assertEquals(obj, TinyJackson.toObject(new JSONObject(json.toString()), TestClass.class));

		obj = new TestClass(null,1,2,'a',null, null);
		json = TinyJackson.toJSONObject(obj);
		assertEquals(obj, TinyJackson.toObject(json, TestClass.class));
		assertEquals(obj, TinyJackson.toObject(new JSONObject(json.toString()), TestClass.class));
	}
}
