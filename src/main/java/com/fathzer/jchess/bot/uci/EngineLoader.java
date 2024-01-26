package com.fathzer.jchess.bot.uci;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.List;

import lombok.extern.slf4j.Slf4j;

@Slf4j
public class EngineLoader {

	public static void main(String[] args) throws IOException {
		final List<String> commands = Files.readAllLines(Paths.get("data/engines.txt"));
		for (String command : commands) {
			if (!command.isBlank() && !command.startsWith("#")) {
				try (UCIEngine engine = new UCIEngine(command)) {
					System.out.println ("engine "+engine.getName()+" is loaded");
				} catch (IOException e) {
					log.error("Error while launching "+command, e);
				}
			}
		}
	}

}
