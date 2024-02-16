package com.fathzer.jchess.bot;

import java.io.Closeable;
import java.util.List;

import com.fathzer.games.clock.CountDownState;

public interface Engine extends Closeable {
	String getName();
	
	List<Option<?>> getOptions();
	
	/** Starts a new game.
	 * @param variant The game variant
	 * @return false if the variant is not supported
	 */
	boolean newGame(Variant variant);
	
	void setPosition(String fen, List<String> moves);
	
	String play(CountDownState params);
}
