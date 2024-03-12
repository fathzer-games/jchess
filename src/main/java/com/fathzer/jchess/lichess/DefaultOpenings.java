package com.fathzer.jchess.lichess;

import java.io.IOException;
import java.io.InputStream;
import java.io.UncheckedIOException;
import java.util.function.Supplier;

import com.fathzer.jchess.Board;
import com.fathzer.jchess.Move;
import com.fathzer.jchess.fen.FENUtils;
import com.fathzer.jchess.uci.JChessUCIEngine;
import com.fathzer.jchess.uci.UCIMove;

public class DefaultOpenings extends AbstractDefaultOpenings<Move, Board<Move>> {
	private static final String KNOWN = "/lichess/masters-shrink.json.gz";

	public static final DefaultOpenings INSTANCE;
	
	static {
		try {
			INSTANCE = new DefaultOpenings();
		} catch (IOException e) {
			throw new UncheckedIOException(e);
		}
	}

	private DefaultOpenings() throws IOException {
		this(() -> DefaultOpenings.class.getResourceAsStream(KNOWN), true);
	}
	
	public DefaultOpenings(Supplier<InputStream> stream, boolean zipped) throws IOException {
		super(stream, zipped);
	}
	
	@Override
	protected Move fromUCI(Board<Move> board, String move) {
		return JChessUCIEngine.toMove(board, UCIMove.from(move));
	}

	@Override
	protected String toXFEN(Board<Move> board) {
		return FENUtils.to(board);
	}
}
