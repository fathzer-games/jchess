package com.fathzer.jchess.internal;

import java.util.Collections;
import java.util.List;

import com.fathzer.jchess.ai.JChessEngine;
import com.fathzer.jchess.bot.Engine;
import com.fathzer.jchess.bot.Move;
import com.fathzer.jchess.bot.Option;
import com.fathzer.jchess.bot.PlayParameters;
import com.fathzer.jchess.bot.Variant;

public class InternalEngine implements Engine {
	private final JChessEngine engine;
	
	public InternalEngine() {
		this.engine = new JChessEngine(null, 0); //TODO
	}

	@Override
	public String getName() {
		return "Internal";
	}

	@Override
	public List<Option<?>> getOptions() {
		// TODO Auto-generated method stub
		return Collections.emptyList();
	}

	@Override
	public boolean newGame(Variant variant) {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public void setPosition(String startpos, List<Move> moves) {
		// TODO Auto-generated method stub

	}

	@Override
	public com.fathzer.jchess.Move play(PlayParameters params) {
		// TODO Auto-generated method stub
		return null;
	}

}
