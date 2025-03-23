package com.fathzer.jchess.gui;

import java.util.function.Consumer;

import com.fathzer.games.Color;

/** A try to make GameGUI used by GameSession generic to any game.
 * @param <M> The class that represents a move
 */
public interface GameGUI<M> { //TODO Remove or finish?
	/** Player one is the one at the bottom of the screen */
	void setPlayerHuman(boolean playerOne, boolean playerTwo);
	void setPlayerOneColor(Color color);
	void setResignationHandler(Consumer<Color> resignationHandler);
	void setManualMoveEnabled(boolean enabled);
	void doMove(M move);
}
