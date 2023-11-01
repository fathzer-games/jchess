package com.fathzer.jchess.lichess;

import java.io.IOException;
import java.io.InputStream;
import java.io.UncheckedIOException;
import java.util.HashMap;
import java.util.Map;
import java.util.Random;
import java.util.function.Function;
import java.util.function.Supplier;
import java.util.zip.GZIPInputStream;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.type.MapType;
import com.fathzer.jchess.Board;
import com.fathzer.jchess.Move;
import com.fathzer.jchess.fen.FENUtils;
import com.fathzer.jchess.movelibrary.LibraryMove;
import com.fathzer.jchess.movelibrary.Proposal;
import com.fathzer.jchess.uci.JChessUCIEngine;
import com.fathzer.jchess.uci.UCIMove;

public class DefaultOpenings implements Function<Board<Move>, Move> {
	private static final Random RND = new Random(); 
	private static final String KNOWN = "/lichess/masters.json.gz";
	private static final ObjectMapper MAPPER = new ObjectMapper();

	public static final DefaultOpenings INSTANCE;
	
	static {
		try {
			INSTANCE = new DefaultOpenings();
		} catch (IOException e) {
			throw new UncheckedIOException(e);
		}
	}

	private final Map<String, Proposal> db;
	
	private DefaultOpenings() throws IOException {
		this(() -> DefaultOpenings.class.getResourceAsStream(KNOWN), true);
	}
	
	public DefaultOpenings(Supplier<InputStream> stream, boolean zipped) throws IOException {
		try (InputStream in = zipped ? new GZIPInputStream(stream.get()) : stream.get()) {
	        MapType mapType = MAPPER.getTypeFactory().constructMapType(HashMap.class, String.class, Proposal.class);
			db = MAPPER.readValue(in, mapType);
		}
	}
	
	private String toFen(Board<Move> board) {
		String fen = FENUtils.to(board);
		int index = fen.lastIndexOf(' ', fen.lastIndexOf(' ')-1);
		return fen.substring(0, index);
	}

	@Override
	public Move apply(Board<Move> board) {
		final Proposal opening = db.get(toFen(board));
		if (opening==null || opening.getMoves().isEmpty()) {
			return null;
		}
		final LibraryMove move = opening.getMoves().get(RND.nextInt(opening.getMoves().size()));
		return JChessUCIEngine.toMove(board.getCoordinatesSystem(), UCIMove.from(move.getCoord()), board.getActiveColor());
	}
}
