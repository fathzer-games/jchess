package com.fathzer.jchess.bot.uci;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Arrays;
import java.util.List;

import org.json.JSONObject;

import com.fathzer.jchess.bot.Engine;
import com.fathzer.jchess.internal.InternalEngine;
import com.fathzer.util.TinyJackson;
import com.fathzer.util.TinyJackson.JsonIgnore;

import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class EngineLoader {
	private static final Path PATH = Paths.get("data/engines.json");
	private static List<EngineData> data;
	
	private EngineLoader() {
		super();
	}
	
	public static void init() throws IOException {
		if (data!=null) {
			return;
		}
		final EngineData internal = new EngineData("JChess", null, new InternalEngine());
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
			array[0] = internal;
			System.arraycopy(dummy, 0, array, 1, dummy.length);
		}
		data = Arrays.asList(array);
		if (error!=null) {
			throw error;
		}
		Runtime.getRuntime().addShutdownHook(new Thread(()-> shutdown()));
	}

	private static void shutdown() {
		data.forEach(e -> {
			if (e.command!=null) {
				// If not the internal engine
				try {
					e.stop();
				} catch (IOException e1) {
					log.error("An error occured while stopping "+e.getName()+" engine",e1);
				}
			}
		});
	}

	public static List<EngineData> getEngines() {
		if (data==null) {
			throw new IllegalStateException("Loader is not inited");
		}
		return data;
	}
	
	@NoArgsConstructor
	@AllArgsConstructor(access = AccessLevel.PRIVATE)
	public static class EngineData {
		@Getter
		@Setter
		private String name;
		@Getter
		@Setter
		private String[] command;
		@JsonIgnore
		private Engine engine;
		
		/** Gets the engine.
		 * @return The engine or null if server is not started
		 */
		public Engine getEngine() {
			return engine;
		}

		/** Starts the engine.
		 * @return true if the engine was not started before calling this method
		 * @throws IOException If something went wrong during server start
		 */
		public boolean start() throws IOException {
			if (engine==null) {
				if (command!=null) {
					engine = new UCIEngine(this);
				} else {
					engine = new InternalEngine();
				}
				return true;
			}
			return false;
		}
		
		/** Stops the engine.
		 * @return true if the engine was not stopped before calling this method
		 * @throws IOException If something went wrong during server stop
		 */
		public boolean stop() throws IOException {
			if (engine!=null) {
				try {
					engine.close();
					return true;
				} finally {
					engine = null;
				}
			}
			return false;
		}
	}
}
