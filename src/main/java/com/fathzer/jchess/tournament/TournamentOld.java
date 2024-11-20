package com.fathzer.jchess.tournament;

import java.io.IOException;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.fathzer.games.clock.ClockSettings;
import com.fathzer.games.clock.PGNTimeControlTagParser;
import com.fathzer.jchess.bot.uci.EngineLoader;
import com.fathzer.jchess.bot.uci.EngineLoader.EngineData;
import com.fathzer.jchess.settings.BasicClockSettings;
import com.fathzer.jchess.settings.Settings;
import com.fathzer.jchess.settings.Settings.EngineSettings;

public class Tournament {
	private static final Logger LOGGER = LoggerFactory.getLogger(Tournament.class);
	
	public void launch(String[] args) {
		if (!init(args)) {
			System.exit(-1);
		}
		final Settings settings = new Settings();
		final ClockSettings clock = new PGNTimeControlTagParser().toClockSettings(args[0]);
		settings.setClock(BasicClockSettings.fromClockSettings(clock));
		settings.getPlayer1().setName(args[1]);
		settings.getPlayer1().setEngine(new EngineSettings());
		settings.getPlayer1().getEngine().setName(args[1]);
		settings.getPlayer2().setName(args[2]);
		settings.getPlayer2().setEngine(new EngineSettings());
		settings.getPlayer2().getEngine().setName(args[2]);
		final TournamentGameSession session = new TournamentGameSession(settings);
		final Thread thread = new Thread(session);
		thread.setName("Game session");
		thread.start();
	}

	private boolean init(String[] args) {
		if (args.length!=3) {
			LOGGER.error("Expecting exactly 3 arguments but found {}", args.length);
			return false;
		} else {
			try {
				EngineLoader.init();
			} catch (IOException e) {
				LOGGER.error("An error occured while reading the external engine configuration file (data/engines.json)", e);
				return false;
			}
		}
		return Arrays.stream(args).skip(1).map(this::startEngine).allMatch(r -> r);
	}

	private boolean startEngine(String name) {
		final List<EngineData> availableEngines = EngineLoader.getEngines();
		final Optional<EngineData> engine = availableEngines.stream().filter(e -> name.equals(e.getName())).findFirst();
		if (!engine.isPresent()) {
			LOGGER.error("Can't find {} engine", name);
			return false;
		}
		// Ensure engine is started
		if (engine.get().getEngine()==null) {
			try {
				engine.get().start();
				LOGGER.info("{} engine started", name);
			} catch (IOException e) {
				LOGGER.error("Can't start "+name+" engine", e);
				return false;
			}
		}
		return true;
	}
}
