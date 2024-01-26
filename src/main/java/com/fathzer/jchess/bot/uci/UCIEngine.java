package com.fathzer.jchess.bot.uci;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.Closeable;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

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
	private final List<Option<?>> options;

	public UCIEngine(String path) throws IOException {
		log.info ("Launch process {}",path);
		this.process = new ProcessBuilder("cmd", "/c", path).start();
		this.reader = process.inputReader();
		this.writer = process.outputWriter();
		this.options = new ArrayList<>();
		this.name = init();
	}
	
	private String init() throws IOException {
		this.writer.write("uci");
		String line;
		String result = null;
		do {
			line = read();
		} while (!"uciok".equals(line) && line!=null);
		if (result==null) {
			throw new IOException("Engine has no name!");
		}
		return result;
	}
	
	private String read() throws IOException {
		String line = reader.readLine();
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
		this.reader.close();
		this.writer.close();
		this.process.destroy();
	}
}
