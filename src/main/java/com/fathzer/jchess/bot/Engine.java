package com.fathzer.jchess.bot;

import java.util.List;

public interface Engine {
	String getName();
	List<Option<?>> getOptions();
	void newGame();
	void setPosition(String startpos, List<Move> moves);
	Move play(PlayParameters params);
}
