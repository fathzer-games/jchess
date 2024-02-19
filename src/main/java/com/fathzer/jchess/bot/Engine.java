package com.fathzer.jchess.bot;

import java.io.Closeable;
import java.util.List;

import com.fathzer.games.clock.CountDownState;
import com.fathzer.jchess.settings.GameSettings.Variant;

public interface Engine extends Closeable {
	String getName();
	
	List<Option<?>> getOptions();
	
	/** Tests whether a variant is supported or not by this engine.
	 * @param variant A variant
	 * @return true if the variant is supported
	 */
	boolean isSupported(Variant variant);
	
	/** Starts a new game.
	 * @param variant The game variant
	 * @return false if the variant is not supported
	 */
	boolean newGame(Variant variant);
	
	void setPosition(String fen, List<String> moves);
	
	String play(CountDownState params);
}
