package com.fathzer.jchess.bot.uci;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.fathzer.jchess.bot.Engine;

import lombok.extern.slf4j.Slf4j;

@Slf4j
public class EngineLoader {

	
	public static void main(String[] args) throws IOException {
		final Path path = Paths.get("data/engines.txt");
		final Map<String, Engine> engines = loadEngines(path);
		engines.values().forEach(engine -> {
			try {
				((UCIEngine)engine).close();
			} catch (IOException e) {
				log.warn("Error while closing engine " +((UCIEngine)engine).getName(), e);
			}
		});
	}

	public static Map<String, Engine> loadEngines(final Path path) throws IOException {
		final Map<String, Engine> engines = new HashMap<>(); 
		final List<String> commands = Files.readAllLines(path);
		for (String command : commands) {
			if (!command.isBlank() && !command.startsWith("#")) {
				try {
					UCIEngine engine = new UCIEngine(command);
					engines.put(engine.getName(), engine);
					log.info("engine {} is loaded",engine.getName());
				} catch (IOException e) {
					log.error("Error while launching "+command, e);
				}
			}
		}
		return engines;
	}

	
}
