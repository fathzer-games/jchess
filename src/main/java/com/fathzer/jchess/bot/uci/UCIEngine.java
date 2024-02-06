package com.fathzer.jchess.bot.uci;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.Closeable;
import java.io.IOException;
import java.io.UncheckedIOException;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;
import java.util.function.Predicate;

import com.fathzer.games.clock.CountDownState;
import com.fathzer.jchess.bot.Engine;
import com.fathzer.jchess.bot.Option;
import com.fathzer.jchess.bot.Variant;

import lombok.extern.slf4j.Slf4j;

@Slf4j
public class UCIEngine implements Closeable, Engine {
	private static final String CHESS960_OPTION = "UCI_Chess960";
	private final Process process;
	private final String name;
	private final BufferedReader reader;
	private final BufferedWriter writer;
	private final StdErrReader errorReader;
	private final List<Option<?>> options;
	private boolean is960Supported;
	private boolean whiteToPlay;
	private boolean positionSet;

	public UCIEngine(String path) throws IOException {
		log.info ("Launching process {}",path);
		final ProcessBuilder processBuilder = new ProcessBuilder(path);
		processBuilder.directory(Paths.get(path).toFile().getParentFile());
		this.process = processBuilder.start();
		this.reader = process.inputReader();
		this.writer = process.outputWriter();
		this.errorReader = new StdErrReader(process);
		this.options = new ArrayList<>();
		this.name = init();
	}
	
	private String init() throws IOException {
		this.write("uci");
		String line;
		String result = null;
		do {
			line = read();
			if (line==null) {
				break;
			}
			final String namePrefix = "id name ";
			final String optionPrefix = "option name ";
			if (line.startsWith(namePrefix)) {
				result = line.substring(namePrefix.length());
			} else if (line.startsWith(optionPrefix)) {
				final Option<?> option = OptionParser.get(line.substring(optionPrefix.length()).split(" "));
				options.add(option);
			}
		} while (!"uciok".equals(line));
		if (result==null) {
			throw new IOException("Engine has no name!");
		}
		return result;
	}
	
	private void write(String line) {
		try {
			this.writer.write(line);
			this.writer.newLine();
			this.writer.flush();
			log.info("> " + line);
		} catch (IOException e) {
			throw new UncheckedIOException(e);
		}
	}
	private String read() {
		try {
			final String line = reader.readLine();
			log.info("< " + line);
			return line;
		} catch (IOException e) {
			throw new UncheckedIOException(e);
		}
	}
	
	@Override
	public String getName() {
		return this.name;
	}

	@Override
	public List<Option<?>> getOptions() {
		return options;
	}

	@Override
	public boolean newGame(Variant variant) {
		positionSet = false;
		if (variant==Variant.CHESS_960 && !is960Supported) {
			return false;
		}
		write("ucinewgame");
		if (is960Supported) {
			write("setoption name "+CHESS960_OPTION + " value "+(variant==Variant.CHESS_960));
		}
		write("isready");
		return waitAnswer("readyok"::equals)!=null;
	}

	/** Reads the engine standard output until a valid answer is returned.
	 * @param answerValidator a predicate that checks the lines returned by engine. 
	 * @return The line that is considered valid, null if no valid line is returned
	 * and the engine closed its output.
	 */
	private String waitAnswer(Predicate<String> answerValidator) {
		for (String line = read(); line!=null; line=read()) {
			if (answerValidator.test(line)) {
				return line;
			}
		}
		return null;
	}

	@Override
	public void setPosition(String fen, List<String> moves) {
		whiteToPlay = "w".equals(fen.split(" ")[1]);
		final StringBuilder builder = new StringBuilder("position fen "+fen);
		if (!moves.isEmpty()) {
			builder.append(" moves");
			for (String move : moves) {
				builder.append(" ");
				builder.append(move);
			}
			whiteToPlay = !whiteToPlay;
		}
		write(builder.toString());
		positionSet = true;
	}

	@Override
	public String play(CountDownState params) {
		if (!positionSet) {
			throw new IllegalStateException("No position defined");
		}
		final StringBuilder command = new StringBuilder("go ");
		final char prefix = whiteToPlay ? 'w' : 'b';
		command.append(prefix);
		command.append("time ");
		command.append(params.getRemainingMs());
		if (params.getIncrementMs()>0) {
			command.append(" ");
			command.append(prefix);
			command.append("inc ");
			command.append(params.getIncrementMs());
		}
		if (params.getMovesToGo()>0) {
			command.append(" ");
			command.append("movestogo ");
			command.append(params.getMovesToGo());
		}
		write (command.toString());
		var bestMovePrefix = "bestmove ";
		final String answer = waitAnswer(s -> s.startsWith(bestMovePrefix));
		if (answer!=null) {
			return answer.substring(bestMovePrefix.length());
		} else {
			return null;
		}
	}

	@Override
	public void close() throws IOException {
		this.write("quit");
		this.reader.close();
		this.writer.close();
		this.errorReader.close();
		try {
			this.process.waitFor(5, TimeUnit.SECONDS);
		} catch (InterruptedException e) {
			this.process.destroy();
			Thread.currentThread().interrupt();
		}
	}
}
