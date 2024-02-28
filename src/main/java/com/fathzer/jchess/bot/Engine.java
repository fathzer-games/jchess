package com.fathzer.jchess.bot;

import java.io.Closeable;
import java.io.IOException;
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
	 * @throws IOException If communication with engine fails
	 */
	boolean newGame(Variant variant) throws IOException;
	
	void setPosition(String fen, List<String> moves) throws IOException;
	
	/** Gets the engine move choice.
	 * @param params The current engine's clock count down (null if no clock is set)
	 * @return a Move in <a href="https://gist.github.com/DOBRO/2592c6dad754ba67e6dcaec8c90165bf">UCI</a> format
	 * @throws IOException If communication with engine fails
	 */
	String getMove(CountDownState params) throws IOException;
}
