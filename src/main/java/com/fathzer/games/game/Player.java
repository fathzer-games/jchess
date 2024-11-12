package com.fathzer.games.game;

import java.util.function.Consumer;

import com.fathzer.games.MoveGenerator;

public interface Player<M,B extends MoveGenerator<M>> {
	/** Sets the method to invoke to resigns.
	 * <br>The default implementation does nothing making resignation impossible.
	 * @param game The game The game to resign
	 * @param resignation The method to call to resign.
	 */
	default void setResignationMethod(Game<M, B> game, Runnable resignation) {}
	
	/** Sets the method to invoke to make a draw proposal.
	 * <br>The default implementation does nothing making draw proposal impossible.
	 * @param game The game The game to resign
	 * @param resignation The method to call to resign.
	 */
	default void setDrawRequestMethod(Game<M, B> game, Runnable drawRequest) {}
	
	/** The player is requested to play a move.
	 * <br>It should start thinking in a background thread and then call {@code moveConsumer} with the chosen move.
	 * @param game The game that asks for the move.
	 * @param moveConsumer a consumer that will post the chosen move to the game.
	 */
	void requestMove(Game<M, B> game, Consumer<M> moveConsumer);
	
	/** The player is requested to cancel the search for a move or thinking about a draw proposal. 
	 * <br>This method is typically called when the game is finished after a call to {@link #requestMove(Game)} and
	 * before the move is posted with {@link Game#addEvent(com.fathzer.games.game.Events.GameEvent)}.
	 * This occurs typically when the other player resigns or a time out occurs.
	 * <br>The default implementation does nothing because even if a call to {@link Game#add(com.fathzer.games.game.Game.IncomingEvent)} is made,
	 * after this method call, it will be ignored by the game.
	 * @param game The game that cancels the request for a move.
	 */
	default void cancel(Game<M, B> game) {
	}
	
	/** The player is requested to accept or reject a draw proposal. 
	 * <br>It should start thinking (in a background thread if the computation requires some time) and then call {@code answerConsumer} with {@code true}
	 * if the draw is accepted and {@code false} if it is not.
	 * <br>The default implementation immediately calls the {@code answerConsumer} with {@code false}.
	 * @param game The game where to post the move event.
	 * @param answerConsumer a consumer that will post the answer to the game.
	 */
	default void requestDrawProposalAnswer(Game<M, B> game, Consumer<Boolean> answerConsumer) {
		answerConsumer.accept(false);
	}
}
