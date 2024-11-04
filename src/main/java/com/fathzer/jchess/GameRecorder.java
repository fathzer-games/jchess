package com.fathzer.jchess;

import java.io.IOException;
import java.io.PrintWriter;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;
import java.time.LocalDate;

import com.fathzer.games.Color;
import com.fathzer.games.clock.PGNTimeControlTagParser;
import com.fathzer.jchess.pgn.PGNHeaders;
import com.fathzer.jchess.pgn.PGNHeaders.Builder;
import com.fathzer.jchess.settings.Settings;
import com.fathzer.jchess.settings.Settings.PlayerSettings;
import com.fathzer.jchess.settings.Settings.Variant;
import com.fathzer.jchess.pgn.PGNWriter;

import lombok.experimental.UtilityClass;

@UtilityClass
public class GameRecorder {
	public static void print(GameHistory history, Settings settings, Color player1Color, Long round) throws IOException {
		try (PrintWriter out=out()) {
			print(history, settings, player1Color, round, out);
		}
	}

	public static void print(GameHistory history, Settings settings, Color player1Color, Long round, PrintWriter out) {
		final Builder builder = new PGNHeaders.Builder();
		builder.setWhiteName(who(settings, player1Color, Color.WHITE));
		builder.setBlackName(who(settings, player1Color, Color.BLACK));
		builder.setRound(round);
		builder.setTimeControl(new PGNTimeControlTagParser().toTag(settings.getClock().toClockSettings()));
		if (Variant.STANDARD!=settings.getVariant()) {
			builder.setVariant(settings.getVariant().name());
		}
		// Add white and black names
		new PGNWriter().getPGN(builder.build(), history).forEach(out::println);
		out.flush();
	}

	private static PrintWriter out() throws IOException {
		final Path file = Path.of("./data/pgn", PGNWriter.DATE_FORMAT.format(LocalDate.now())+".pgn");
		Files.createDirectories(file.getParent());
		final PrintWriter printer = new PrintWriter(Files.newBufferedWriter(file, StandardOpenOption.CREATE, StandardOpenOption.APPEND));
		if (Files.size(file)!=0) {
			printer.println();
			printer.println("===========");
			printer.println();
		}
		return printer;
	}
	
	private static String who(Settings settings, Color player1Color, Color color) {
		final PlayerSettings player = player1Color==color ? settings.getPlayer1() : settings.getPlayer2();
		if (player.getName()!=null) {
			return player.getName();
		} else if (player.getEngine()!=null) {
			return player.getEngine().getName();
		} else {
			return "?";
		}
	}
}
