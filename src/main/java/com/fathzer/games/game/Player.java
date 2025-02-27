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
	 * @param drawRequest The method to call process a draw request.
	 */
	default void setDrawRequestMethod(Game<M, B> game, Runnable drawRequest) {}
	
	/** Informs the player that it starts playing in a new game.
	 * <br>The default implementation does nothing
	 * @param game The new game
	 */
	default void onNewGame(Game<M, B> game) {
	}
	
	/** The player is requested to play a move.
	 * <br>It should start thinking in a background thread and then call {@code moveConsumer} with the chosen move.
	 * @param game The game that asks for the move.
	 * @param moveConsumer a consumer where to post the chosen move.
	 * <br>A player can send null to this consumer if it is not able to process the request. Typically a chess engine
	 * must use this mechanism to gracefully inform the game when an exception occurs during its best move search.
	 */
	void requestMove(Game<M, B> game, Consumer<M> moveConsumer);
	
	/** The player is informed that the game is ended.
	 * <br>The default implementation does nothing.
	 * Nevertheless, it is a good practice to override this method and cancel the current search for a move or thinking about a draw proposal.
	 * <br>It is optional because any event sent to a {@link Game} after the end of the game will be ignored.
	 * @param game The game that ends.
	 */
	default void onEndGame(Game<M, B> game) {
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
