package com.fathzer.jchess.bot;

import java.util.List;

public interface Engine {
	String getName();
	List<Option<?>> getOptions();
	/** Starts a new game.
	 * @param variant The game variant
	 * @return false if the variant is not supported
	 */
	boolean newGame(Variant variant);
	void setPosition(String startpos, List<Move> moves);
	
	//TODO Switch to another move representation (for instance UCI)?
	com.fathzer.jchess.Move play(PlayParameters params);
}
