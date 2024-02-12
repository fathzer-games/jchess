package com.fathzer.jchess.bot.uci;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.fathzer.games.clock.CountDownState;
import com.fathzer.jchess.bot.Engine;
import com.fathzer.jchess.bot.Option;
import com.fathzer.jchess.bot.Variant;
import com.fathzer.jchess.bot.options.SpinOption;
import com.fathzer.jchess.swing.settings.GameSettings;
import com.fathzer.jchess.swing.settings.SettingsDialog;
import com.fathzer.jchess.swing.settings.engine.EngineDialog;

import lombok.extern.slf4j.Slf4j;

@Slf4j
public class EngineLoader {
	private static final String START_FEN = "rnbqkbnr/pppppppp/8/8/8/8/PPPPPPPP/RNBQKBNR w KQkq - 0 1";
	
	public static void main(String[] args) throws IOException {
		final Path path = Paths.get("data/engines.txt");
		final Map<String, Engine> engines = loadEngines(path);
		engines.values().forEach(engine -> {
			final EngineDialog dialog = new EngineDialog(null, engine);
			dialog.setVisible(true);
			try {
				if (dialog.getResult()!=null) {
	//				for (Option<?> option : engine.getOptions()) {
	//					if (option.getName().equals("depth")) {
	//						System.out.println("Depth is currently "+option.getValue()+". Setting it to 2");
	//						((SpinOption)option).setValue(2L);
	//					}
	//				}
					engine.newGame(Variant.STANDARD);
					engine.setPosition(START_FEN, Collections.emptyList());
					engine.play(new CountDownState(180000, 3000, 0));
					engine.setPosition(START_FEN, Collections.singletonList("e2e4"));
					engine.play(new CountDownState(180000, 3000, 0));
				}
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
