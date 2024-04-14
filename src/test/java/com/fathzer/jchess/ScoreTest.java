package com.fathzer.jchess;

import static org.junit.jupiter.api.Assertions.*;

import org.junit.jupiter.api.Test;

class ScoreTest {

	@Test
	void test() {
		var score = new Score();
		assertThrows(IllegalStateException.class, () -> score.win(true));
		assertThrows(IllegalStateException.class, () -> score.draw());
		assertThrows(IllegalStateException.class, () -> score.win(false));
		
		score.newGame();
		assertEquals(1, score.getGameCount());
		score.win(true);
		assertEquals(1, score.getGameCount());
		assertEquals(1.0, score.getPlayerScore(true), 1E-5);
		assertThrows(IllegalStateException.class, () -> score.win(false));

		score.newGame();
		assertThrows(IllegalStateException.class, () -> score.newGame());
		score.win(false);
		assertEquals(2, score.getGameCount());
		assertEquals(1.0, score.getPlayerScore(true), 1E-5);
		assertEquals(1.0, score.getPlayerScore(false), 1E-5);

		score.newGame();
		score.draw();
		assertEquals(1.5, score.getPlayerScore(true), 1E-5);
		assertEquals(1.5, score.getPlayerScore(false), 1E-5);
	}

}
