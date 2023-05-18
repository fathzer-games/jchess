package com.fathzer.jchess.swing;

import com.fathzer.jchess.pgn.GameHistory;

public class GameRecorder {

	public static void record(GameHistory history) {
		//TODO
		history.getPGN().forEach(System.out::println);
	}
	
}
