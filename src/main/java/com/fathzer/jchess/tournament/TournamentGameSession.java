package com.fathzer.jchess.tournament;

import java.io.PrintWriter;

import com.fathzer.games.game.GameManager;
import com.fathzer.games.game.Player;
import com.fathzer.jchess.Board;
import com.fathzer.jchess.GameRecorder;
import com.fathzer.jchess.Move;
import com.fathzer.jchess.settings.Settings;

import lombok.extern.slf4j.Slf4j;

@Slf4j
class TournamentGameSession extends GameManager<Move, Board<Move>, Settings> {
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
			GameRecorder.print(this.game.getHistory(), this.getSettings(), this.getPlayer1Color(), (long) this.getScore().getGameCount(), writer);
		} catch (Exception e) {
			log.error("An error occurred while writing pgn",e);
		}
	}
	
	@Override
	protected void onStateChanged(State old, State current) {
		log.debug("Game session's state changes from {} to {}!", old, current);
		super.onStateChanged(old, current);
		if (State.ENDED.equals(current)) {
			log.info("Game session ended");
			log.info("Final score: {} {} {}", this.getSettings().getPlayer1().getName(), this.getScore(), this.getSettings().getPlayer2().getName());
		}
	}

	@Override
	protected Board<Move> getStartPosition() {
		return getSettings().getVariant().getRules().apply(getSettings().getFen());
	}
}
