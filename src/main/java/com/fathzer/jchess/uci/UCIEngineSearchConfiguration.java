package com.fathzer.jchess.uci;

import com.fathzer.games.Color;
import com.fathzer.jchess.Board;
import com.fathzer.jchess.BoardExplorer;
import com.fathzer.jchess.Move;
import com.fathzer.jchess.Piece;
import com.fathzer.jchess.PieceKind;
import com.fathzer.jchess.ai.JChessEngine;
import com.fathzer.jchess.uci.GoOptions.PlayerClockData;
import com.fathzer.jchess.uci.GoOptions.TimeOptions;

/** A class that configures the engine before executing the go command
 */
public class UCIEngineSearchConfiguration {
	public static class EngineConfiguration {
		private long maxTime;
		
		private EngineConfiguration(JChessEngine engine) {
			maxTime = engine.getMaxTime()*3/2; //TODO Avoid magic number...
		}
	}
	
	public EngineConfiguration configure(JChessEngine engine, GoOptions options, Board<Move> board) {
		final EngineConfiguration result = new EngineConfiguration(engine);
		final TimeOptions timeOptions = options.getTimeOptions();
		if (options.isPonder() || !options.getMoveToSearch().isEmpty() || options.getMate()>0 || options.getNodes()>0 || timeOptions.isInfinite()) {
			//TODO some options are not supported
		}
		if (timeOptions.getMoveTimeMs()>0) {
			engine.setMaxTime(timeOptions.getMoveTimeMs());
		} else {
			final Color c = board.getActiveColor();
			final PlayerClockData engineClock = c==Color.WHITE ? timeOptions.getWhiteClock() : timeOptions.getBlackClock();
			if (engineClock.getRemainingMs()>0) {
				engine.setMaxTime(getMaxTime(board, engineClock.getRemainingMs(), engineClock.getIncrementMs(), timeOptions.getMovesToGo()));
			}
		}
		return result;
	}

	public void set(JChessEngine engine, EngineConfiguration c) {
		engine.setMaxTime(c.maxTime);
	}
	
	public long getMaxTime(Board<Move> board, long remainingMs, long incrementMs, int movesToGo) {
		if (movesToGo==0) {
			// Evaluate number of remaining moves
			movesToGo = getRemainingMoves(board);
		}
		remainingMs += incrementMs*(movesToGo-1);
		return remainingMs/movesToGo;
	}
	
	/** Gets an evaluation of remaining moves.
	 * @param board The current position
	 * @return an prediction of the number of moves to reach end of game
	 */
	public int getRemainingMoves(Board<Move> board) {
		final int points = getPoints(board);
		final int remainingMoves;
		if (points<20) {
			remainingMoves = points+10;
		} else if (points<60) {
			remainingMoves =  3*points/8+22;
		} else {
			remainingMoves =  5*points/4-30;
		}
		return remainingMoves;
	}
	private int getPoints(Board<Move> board) {
		int points = 0;
		final BoardExplorer exp = board.getExplorer();
		do {
			final Piece p = exp.getPiece();
			if (p!=null && !PieceKind.KING.equals(p.getKind())) {
				points += p.getKind().getValue();
			}
			
		} while (exp.next());
		return points;
	}
}
