package com.fathzer.jchess.bot.uci;

import java.io.BufferedReader;
import java.io.Closeable;
import java.io.EOFException;
import java.io.IOException;
import java.util.concurrent.atomic.AtomicBoolean;

class StdErrReader implements Closeable, Runnable {
	private final BufferedReader errorReader;
	private final Thread spyThread;
	private final AtomicBoolean stopped;

	StdErrReader(Process process) {
		this.errorReader = process.errorReader();
		this.stopped = new AtomicBoolean();
		this.spyThread = new Thread(this);
		this.spyThread.setDaemon(true);
		this.spyThread.start();
	}
	
	@Override
	public void run() {
		while (!stopped.get()) {
			try {
				final String line = errorReader.readLine();
				if (line!=null) {
					System.err.println (line); //TODO
				}
			} catch (EOFException e) {
				if (!stopped.get()) {
					log(e);
				}
			} catch (IOException e) {
				log(e);
			}
		}
	}

	private void log(IOException e) {
		synchronized(System.err) {
			System.err.println("An error occured, stopped is "+ stopped);
			e.printStackTrace(); //TODO
		}
	}

	@Override
	public void close() throws IOException {
		this.stopped.set(true);
		this.errorReader.close();
	}
}
