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
import com.fathzer.jchess.fen.FENParser;
import com.fathzer.jchess.uci.JChessUCIEngine;
import com.fathzer.jchess.uci.UCIMove;

public class DefaultOpenings implements Function<Board<com.fathzer.jchess.Move>, com.fathzer.jchess.Move> {
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

	private final Map<String, Opening> db;
	
	private DefaultOpenings() throws IOException {
		this(() -> DefaultOpenings.class.getResourceAsStream(KNOWN), true);
	}
	
	public DefaultOpenings(Supplier<InputStream> stream, boolean zipped) throws IOException {
		try (InputStream in = zipped ? new GZIPInputStream(stream.get()) : stream.get()) {
	        MapType mapType = MAPPER.getTypeFactory().constructMapType(HashMap.class, String.class, Opening.class);
			db = MAPPER.readValue(in, mapType);
		}
	}
	
	private String toFen(Board<com.fathzer.jchess.Move> board) {
		String fen = FENParser.to(board);
		int index = fen.lastIndexOf(' ', fen.lastIndexOf(' ')-1);
		return fen.substring(0, index);
	}

	@Override
	public com.fathzer.jchess.Move apply(Board<com.fathzer.jchess.Move> board) {
		final Opening opening = db.get(toFen(board));
		if (opening==null || opening.getMoves().isEmpty()) {
			return null;
		}
		System.out.println (opening.getName()+" -> "+opening.getMoves()); //TODO
		final Move move = opening.getMoves().get(RND.nextInt(opening.getMoves().size()));
		return JChessUCIEngine.toMove(UCIMove.from(move.getCoord()), board.getActiveColor());
	}
}
