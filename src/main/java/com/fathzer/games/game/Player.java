package com.fathzer.games.game;

public interface Player {
	/** The player is requested to play a move.
	 * <br>It should start thinking in a background thread and call {@link Game#addEvent(com.fathzer.games.game.Game.IncomingEvent)} with a {@link Game.MoveEvent}
	 * when the move is chosen.
	 * @param game The game where to post the move event.
	 */
	void requestMove(Game game);
	
	/** The player is requested to cancel the search for a move or thinking about a draw proposal. 
	 * <br>This method is typically called when the game is finished after a call to {@link #requestMove(Game)} and
	 * before the move is posted with {@link Game#addEvent(com.fathzer.games.game.Events.GameEvent)}.
	 * This occurs typically when the other player resigns or a time out occurs.
	 * <br>The default implementation does nothing because even if a call to {@link Game#add(com.fathzer.games.game.Game.IncomingEvent)} is made,
	 * after this method call, it will be ignored by the game.
	 * @param game The game where to post the move event.
	 */
	default void cancel(Game game) {
	}
}
