package com.fathzer.jchess.bot.uci;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.EOFException;
import java.io.IOException;
import java.io.UncheckedIOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.TimeUnit;
import java.util.function.Predicate;

import com.fathzer.games.clock.CountDownState;
import com.fathzer.jchess.bot.Engine;
import com.fathzer.jchess.bot.Option;
import com.fathzer.jchess.bot.Option.Type;
import com.fathzer.jchess.bot.uci.EngineLoader.EngineData;
import com.fathzer.jchess.settings.GameSettings.Variant;
import com.fathzer.util.ProcessExitDetector;

import lombok.extern.slf4j.Slf4j;

@Slf4j
public class UCIEngine implements Engine {
	private static final String CHESS960_OPTION = "UCI_Chess960";
	private static final String PONDER_OPTION = "Ponder";

	private final Process process;
	private final EngineData data;
	private final BufferedReader reader;
	private final BufferedWriter writer;
	private final StdErrReader errorReader;
	private final List<Option<?>> options;
	private boolean is960Supported;
	private boolean whiteToPlay;
	private boolean positionSet;
	private boolean expectedRunning;

	public UCIEngine(EngineData data) throws IOException {
		log.info ("Launching process {}", Arrays.asList(data.getCommand()));
		this.data = data;
		final ProcessBuilder processBuilder = new ProcessBuilder(data.getCommand());
		this.process = processBuilder.start();
		this.expectedRunning = true;
		this.reader = process.inputReader();
		this.writer = process.outputWriter();
		this.errorReader = new StdErrReader(process);
		new ProcessExitDetector(process, p -> {
			if (expectedRunning) {
				expectedRunning = false;
				log.warn("{} UCI engine exited unexpectedly with code {}", data.getName(), p.exitValue());
				try {
					closeStreams();
				} catch (IOException e) {
					log.error("The following error occured while closing streams of "+data.getName()+" UCI engine");
				}
			} else {
				log.info("{} UCI engine exited with code {}", data.getName(), p.exitValue());
			}
		}).start(true);
		this.options = new ArrayList<>();
		init();
	}
	
	private void init() throws IOException {
		this.write("uci");
		String line;
		do {
			line = read();
			if (line==null) {
				throw new EOFException();
			}
			final Optional<Option<?>> ooption = parseOption(line);
			if (ooption.isPresent()) {
				Option<?> option = ooption.get();
				if (CHESS960_OPTION.equals(option.getName())) {
					is960Supported = true;
				} else if (!PONDER_OPTION.equals(option.getName())) {
					// Ponder is not supported yet
					option.addListener((prev, cur) -> {
						try {
							setOption(option, cur);
						} catch (IOException e) {
							throw new UncheckedIOException(e);
						}
					});
					options.add(option);
				}
			}
		} while (!"uciok".equals(line));
	}
	
	private Optional<Option<?>> parseOption(String line) throws IOException {
		try {
			return OptionParser.parse(line);
		} catch (IllegalArgumentException e) {
			throw new IOException(e);
		}
	}
	
	private void setOption(Option<?> option, Object value) throws IOException {
		final StringBuilder buf = new StringBuilder("setoption name ");
		buf.append(option.getName());
		if (Type.BUTTON!=option.getType()) {
			buf.append(" value ");
			buf.append(value);
		}
		write(buf.toString());
	}
	
	private void write(String line) throws IOException {
		this.writer.write(line);
		this.writer.newLine();
		this.writer.flush();
		log.info(">{}: {}", data.getName(), line);
	}
	private String read() throws IOException {
		final String line = reader.readLine();
		log.info("<{} : {}", data.getName(), line);
		return line;
	}

	@Override
	public List<Option<?>> getOptions() {
		return options;
	}
	
	@Override
	public boolean isSupported(Variant variant) {
		return variant==Variant.STANDARD || (variant==Variant.CHESS960 && is960Supported);
	}

	@Override
	public boolean newGame(Variant variant) throws IOException {
		positionSet = false;
		if (variant==Variant.CHESS960 && !is960Supported) {
			return false;
		}
		write("ucinewgame");
		if (is960Supported) {
			write("setoption name "+CHESS960_OPTION + " value "+(variant==Variant.CHESS960));
		}
		write("isready");
		return waitAnswer("readyok"::equals)!=null;
	}

	/** Reads the engine standard output until a valid answer is returned.
	 * @param answerValidator a predicate that checks the lines returned by engine. 
	 * @return The line that is considered valid, null if no valid line is returned
	 * and the engine closed its output.
	 * @throws IOException If communication with engine fails
	 */
	private String waitAnswer(Predicate<String> answerValidator) throws IOException {
		for (String line = read(); line!=null; line=read()) {
			if (answerValidator.test(line)) {
				return line;
			}
		}
		throw new EOFException();
	}

	@Override
	public void setPosition(String fen, List<String> moves) throws IOException {
		whiteToPlay = "w".equals(fen.split(" ")[1]);
		if (moves.size()%2!=0) {
			whiteToPlay = !whiteToPlay;
		}
		final StringBuilder builder = new StringBuilder("position fen "+fen);
		if (!moves.isEmpty()) {
			builder.append(" moves");
			for (String move : moves) {
				builder.append(" ");
				builder.append(move);
			}
		}
		write(builder.toString());
		positionSet = true;
	}

	@Override
	public String getMove(CountDownState params) throws IOException {
		if (!positionSet) {
			throw new IllegalStateException("No position defined");
		}
		final StringBuilder command = new StringBuilder("go");
		if (params!=null) {
			command.append(' ');
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
		}
		write (command.toString());
		var bestMovePrefix = "bestmove ";
		final String answer = waitAnswer(s -> s.startsWith(bestMovePrefix));
		return answer.substring(bestMovePrefix.length());
	}

	@Override
	public void close() throws IOException {
		if (!expectedRunning) {
			return;
		}
		expectedRunning = false;
		this.write("quit");
		closeStreams();
		try {
			this.process.waitFor(5, TimeUnit.SECONDS);
		} catch (InterruptedException e) {
			log.warn("Fail to gracefully close UCI engine {}, trying to destroy it", data.getName());
			this.process.destroy();
			Thread.currentThread().interrupt();
		}
	}

	private void closeStreams() throws IOException {
		this.reader.close();
		this.writer.close();
		this.errorReader.close();
	}

	@Override
	public String getName() {
		return data.getName();
	}
}
