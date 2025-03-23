package com.fathzer.jchess.bot.uci;

import static org.junit.jupiter.api.Assertions.*;

import java.util.Queue;
import java.util.concurrent.ConcurrentLinkedQueue;

import org.junit.jupiter.api.Test;

import com.fathzer.jchess.uci.JChessUCIEngine;
import com.fathzer.jchess.uci.UCI;

class UCITest {
	
	@Test
	void test() throws InterruptedException {
		//FIXME
		// This is clearly not a test (no assertion)!
		// The worse is it does not respect the uci protocol (should wait for go return before sending a new position)
		MyUCI uci = new MyUCI();
		Thread thread = new Thread(uci);
		thread.setDaemon(true);
		thread.start();
		uci.add("uci");
		uci.add("isready");
		uci.add("ucinewgame");
		uci.add("position startpos");
		uci.add("isready");
		uci.add("go");
		uci.add("position fen 4k1r1/1P6/5p2/p1Np1P2/5B1p/5Q1P/1q3PPK/8 w - - 4 42 moves b7b8q");
		uci.add("go");
		uci.add("quit");
		thread.join();
	}
	

	private static class MyUCI extends UCI {
		public MyUCI() {
			super(new JChessUCIEngine());
		}

		private Queue<String> commands = new ConcurrentLinkedQueue<>();
		private Queue<String> replies = new ConcurrentLinkedQueue<>();
		
		public MyUCI add(String command) {
			commands.add(command);
			return this;
		}
		
		@Override
		protected String getNextCommand() {
			waitSomething(commands);
			String next = commands.poll();
			System.out.println("receiving: "+next);
			return next;
		}

		protected void waitSomething(Queue<?> queue) {
			while (queue.peek()==null) {
				try {
					Thread.sleep(100);
				} catch (InterruptedException e) {
					Thread.currentThread().interrupt();
				}
			}
		}

		@Override
		protected void out(CharSequence message) {
			replies.add(message.toString());
			super.out(message);
		}
	}
}
