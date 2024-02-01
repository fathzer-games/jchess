package com.fathzer.jchess.bot.uci;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.Closeable;
import java.io.IOException;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;

import com.fathzer.jchess.bot.Engine;
import com.fathzer.jchess.bot.Move;
import com.fathzer.jchess.bot.Option;
import com.fathzer.jchess.bot.PlayParameters;

import lombok.extern.slf4j.Slf4j;

@Slf4j
public class UCIEngine implements Closeable, Engine {
	private final Process process;
	private final String name;
	private final BufferedReader reader;
	private final BufferedWriter writer;
	private final StdErrReader errorReader;
	private final List<Option<?>> options;

	public UCIEngine(String path) throws IOException {
		log.info ("Launching process {}",path);
		final ProcessBuilder processBuilder = new ProcessBuilder(/*"cmd.exe", "/c",*/ path);
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
				final Option<?> option = parseOption(line.substring(optionPrefix.length()).split(" "));
				options.add(option);
			}
		} while (!"uciok".equals(line));
		if (result==null) {
			throw new IOException("Engine has no name!");
		}
		return result;
	}
	
	private Option<?> parseOption(String[] tokens) throws IOException {
		// TODO Auto-generated method stub
		final String optionName = tokens[0];
		final Option.Type type = null;
		return null;
	}

	private void write(String line) throws IOException {
		this.writer.write(line);
		this.writer.newLine();
		this.writer.flush();
		log.info("> " + line);
	}
	private String read() throws IOException {
		final String line = reader.readLine();
		log.info("< " + line);
		return line;
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
	public void newGame() {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void setPosition(String startpos, List<Move> moves) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public Move play(PlayParameters params) {
		// TODO Auto-generated method stub
		return null;
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
		}
	}
}
