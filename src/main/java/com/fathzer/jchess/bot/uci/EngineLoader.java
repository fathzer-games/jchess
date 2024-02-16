package com.fathzer.jchess.bot.uci;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Arrays;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

import org.json.JSONObject;

import com.fathzer.jchess.bot.Engine;
import com.fathzer.jchess.internal.InternalEngine;
import com.fathzer.util.TinyJackson;

import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

public class EngineLoader {
	private static final Path PATH = Paths.get("data/engines.json");
	private static Map<String, EngineData> data;
	
	private EngineLoader() {
		super();
	}
	
	public static void init() throws IOException {
		final EngineData internal = new EngineData("jchess", null);
		final EngineData[] array;
		IOException error = null;
		if (!Files.exists(PATH)) {
			// No external engines
			array = new EngineData[] {internal};
		} else {
			EngineData[] dummy;
			try {
				dummy = TinyJackson.toArray(new JSONObject(Files.readString(PATH)).getJSONArray("engines"), EngineData.class);
			} catch (IOException e) {
				dummy = new EngineData[0];
				error = e;
			}
			array = new EngineData[dummy.length+1];
			System.arraycopy(dummy, 0, array, 0, dummy.length);
			array[dummy.length] = internal;
		}
		data = Arrays.stream(array).collect(Collectors.toMap(EngineData::getName, Function.identity()));
		if (error!=null) {
			throw error;
		}
	}

	public static Map<String, EngineData> getEngines() {
		if (data==null) {
			throw new IllegalStateException("Loader is not inited");
		}
		return data;
	}
	
	public static Engine buildEngine(EngineData data) throws IOException {
		if (data.getCommand()==null) {
			return new InternalEngine();
		} else {
			return new UCIEngine(data);
		}
	}
	
	@Getter
	@Setter
	@NoArgsConstructor
	@AllArgsConstructor(access = AccessLevel.PRIVATE)
	public static class EngineData {
		private String name;
		private String[] command;
	}
}
