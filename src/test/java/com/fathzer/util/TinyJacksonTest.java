package com.fathzer.util;

import static org.junit.jupiter.api.Assertions.*;

import org.json.JSONObject;
import org.junit.jupiter.api.Test;

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
		private String str;
		private double value;
	}

	@Test
	void test() {
		final TestClass obj = new TestClass("x",1,2,'a',new String[] {"a","b"}, new OtherClass("c",2.0));
		final JSONObject json = TinyJackson.toJSONObject(obj);
		final String jsonString = json.toString();
		assertEquals(obj, TinyJackson.toObject(json, TestClass.class));
		assertEquals(obj, TinyJackson.toObject(new JSONObject(jsonString), TestClass.class));
	}
}
