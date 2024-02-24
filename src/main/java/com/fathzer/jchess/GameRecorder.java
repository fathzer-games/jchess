package com.fathzer.jchess;

import java.io.IOException;
import java.io.PrintWriter;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;
import java.time.LocalDate;

import com.fathzer.games.Color;
import com.fathzer.jchess.pgn.PGNHeaders;
import com.fathzer.jchess.pgn.PGNHeaders.Builder;
import com.fathzer.jchess.settings.GameSettings;
import com.fathzer.jchess.settings.GameSettings.PlayerSettings;
import com.fathzer.jchess.settings.GameSettings.Variant;
import com.fathzer.jchess.pgn.PGNWriter;

import lombok.experimental.UtilityClass;

@UtilityClass
public class GameRecorder {
	public static void record(GameSettings settings, Color player1Color, GameHistory history) throws IOException {
		try (PrintWriter out=out()) {
			final Builder builder = new PGNHeaders.Builder();
			builder.setWhiteName(who(settings, player1Color, Color.WHITE));
			builder.setBlackName(who(settings, player1Color, Color.BLACK));
			if (Variant.STANDARD!=settings.getVariant()) {
				builder.setVariant(settings.getVariant().name());
			}
			// Add white and black names
			new PGNWriter().getPGN(builder.build(), history).forEach(out::println);
			out.flush();
		}
	}

	private static PrintWriter out() throws IOException {
		final Path file = Path.of("./data/pgn", PGNWriter.DATE_FORMAT.format(LocalDate.now())+".txt");
		Files.createDirectories(file.getParent());
		final PrintWriter printer = new PrintWriter(Files.newBufferedWriter(file, StandardOpenOption.CREATE, StandardOpenOption.APPEND));
		if (Files.size(file)!=0) {
			printer.println();
			printer.println("===========");
			printer.println();
		}
		return printer;
	}
	
	private static String who(GameSettings settings, Color player1Color, Color color) {
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
