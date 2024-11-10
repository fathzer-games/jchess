package com.fathzer.games.game;

import java.util.LinkedList;
import java.util.List;

import com.fathzer.games.MoveGenerator.MoveConfidence;
import com.fathzer.games.Status;
import com.fathzer.jchess.Board;
import com.fathzer.jchess.Move;
import com.fathzer.jchess.pgn.PGNHeaders.TerminationCause;

public class GameHistory {
	private final Board<Move> startBoard;
	private final Board<Move> board;
	private final List<Move> moves;
	private volatile Status extraStatus;
	private volatile TerminationCause terminationCause;

	public GameHistory(Board<Move> board) {
		this.startBoard = (Board<Move>) board.fork();
		this.board = (Board<Move>) board.fork();
		this.moves = new LinkedList<>();
		this.terminationCause = TerminationCause.NORMAL;
	}

	/** Adds a move to this history.
	 * @param move The move to add.
	 * @return true if the move is legal. False if it is not. In such a case the move is also added to the list of moves.
	 * @throws IllegalStateException if game is already ended.
	 */
	public synchronized boolean add(Move move) {
		if (Status.PLAYING!=getStatus()) {
			throw new IllegalStateException();
		}
		this.moves.add(move);
		return board.makeMove(move, MoveConfidence.UNSAFE);
	}

	public Board<Move> getStartBoard() {
		return startBoard;
	}
	
	public Board<Move> getBoard() {
		return board;
	}

	public List<Move> getMoves() {
		return moves;
	}
	
	/** Declares an early termination for the game.
	 * <br>For instance, a player resigns or players agree to a draw.
	 * <br>Any subsequent call to this method or {@link #add(Move)} will result in an IllegalStateException  
	 * @param status The end status
	 * @param terminationCause The termination cause
	 * @throws IllegalStateException if game is already ended.
	 * @throws IllegalArgumentException if termination is null or if status is {@link Status#PLAYING} or null.
	 */
	public synchronized void earlyEnd(Status status, TerminationCause terminationCause) {
		if (Status.PLAYING==status || status==null) {
			throw new IllegalStateException();
		}
		if (Status.PLAYING!=getStatus()) {
			throw new IllegalStateException("Status is "+getStatus());
		}
		this.extraStatus = status;
		this.terminationCause = terminationCause;
	}
	
	public synchronized Status getStatus() {
		if (extraStatus!=null) {
			return extraStatus;
		}
		return board.getStatus();
	}
	
	public synchronized TerminationCause getTerminationCause() {
		return this.terminationCause;
	}
}