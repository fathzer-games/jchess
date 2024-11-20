package com.fathzer.jchess.tournament;

import java.io.PrintWriter;

import com.fathzer.games.Color;
import com.fathzer.games.Status;
import com.fathzer.games.game.EnginePlayer;
import com.fathzer.games.game.GameManager;
import com.fathzer.games.game.Player;
import com.fathzer.jchess.Board;
import com.fathzer.jchess.Move;
import com.fathzer.jchess.pgn.MoveAlgebraicNotationBuilder;
import com.fathzer.games.GameHistory.TerminationCause;
import com.fathzer.jchess.settings.Settings;

import lombok.extern.slf4j.Slf4j;

@Slf4j
class TournamentGameSession extends GameManager<Move, Board<Move>, Settings> implements Runnable {
	private static final MoveAlgebraicNotationBuilder ANB = new MoveAlgebraicNotationBuilder();

	TournamentGameSession(Settings settings, Player<Move, Board<Move>> player1, Player<Move, Board<Move>> player2) {
		super(settings, player1, player2);
	}

	@Override
	protected void onGameEnded() {
		PrintWriter writer = new PrintWriter(System.out);
		try {
			if (this.getScore().getGameCount()>1) {
				writer.println();
				writer.println("===========");
				writer.println();
			}
			var h = this.game.getHistory();
			writer.println(result()+(h.getTerminationCause()==TerminationCause.NORMAL?"":" ("+h.getTerminationCause().toString()+")")+" after "+h.getMoves().size()+" moves. Score: "+getScore());
			writer.flush();
//TODO			GameRecorder.print(this.game.getHistory(), this.getSettings(), this.player1Color, (long) this.getScore().getGameCount(), writer);
		} catch (Exception e) {
			log.error("An error occured while writing pgn",e);
		}
	}
	
	private CharSequence result() {
		final StringBuilder buf = new StringBuilder();
		final Status status = game.getHistory().getStatus();
		buf.append(status);
		final Color winner = status.winner();  
		if (winner!=null) {
			buf.append("-");
			buf.append(((EnginePlayer) this.getPlayer(winner)).getName());
		}
		return buf;
	}

	@Override
	protected void onStateChanged(State old, State current) {
		log.debug("Game session's state changes from {} to {}!", old, current);
		super.onStateChanged(old, current);
		if (State.ENDED.equals(current)) {
			log.info("Game session ended");
		}
	}

	@Override
	protected Board<Move> getStartPosition() {
		return getSettings().getVariant().getRules().apply(getSettings().getFen());
	}
}
