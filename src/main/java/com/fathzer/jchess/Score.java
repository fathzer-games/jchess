package com.fathzer.jchess;

//TODO Should move to games-core with ability to get more than 2 players score
public class Score {
	private int gameCount;
	private int player1Score;
	private int player2Score;

	public Score() {
		gameCount = 0;
		reset();
	}
	
	public void reset() {
		gameCount = 0;
		player1Score = 0;
		player2Score = 0;
	}
	
	public void newGame() {
		check(player1Score+player2Score==gameCount*2);
		gameCount++;
	}
	
	public void draw() {
		check(player1Score+player2Score==(gameCount-1)*2);
		player1Score++;
		player2Score++;
	}
	
	public void win(boolean player1) {
		check(player1Score+player2Score==(gameCount-1)*2);
		if (player1) {
			player1Score += 2;
		} else {
			player2Score += 2;
		}
	}
	
	public double getPlayerScore(boolean player1) {
		return 0.5 * (player1 ? player1Score : player2Score);
	}
	
	/** Gets the number of started games. 
	 * @return a positive or null integer
	 */
	public int getGameCount() {
		return gameCount;
	}

	private void check(boolean condition) {
		if (!condition) {
			throw new IllegalStateException();
		}
	}
}
