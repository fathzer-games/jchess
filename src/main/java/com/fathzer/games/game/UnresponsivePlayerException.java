package com.fathzer.games.game;

public class UnresponsivePlayerException extends RuntimeException {
	private static final long serialVersionUID = 1L;
	
	private final transient Player<?,?> player;
	
	public UnresponsivePlayerException(Player<?, ?> player) {
		super();
		this.player = player;
	}

	public Player<?, ?> getPlayer() {
		return player;
	}
}