package com.fathzer.jchess.lichess;

import java.io.IOException;
import java.io.InputStream;
import java.io.UncheckedIOException;
import java.util.Random;
import java.util.function.Function;
import java.util.function.Supplier;
import java.util.zip.GZIPInputStream;

import org.json.JSONArray;
import org.json.JSONObject;
import org.json.JSONTokener;

import com.fathzer.jchess.Board;
import com.fathzer.jchess.Move;
import com.fathzer.jchess.fen.FENUtils;
import com.fathzer.jchess.uci.JChessUCIEngine;
import com.fathzer.jchess.uci.UCIMove;

public class DefaultOpenings implements Function<Board<Move>, Move> {
	private static final Random RND = new Random(); 
	private static final String KNOWN = "/lichess/masters.json.gz";

	public static final DefaultOpenings INSTANCE;
	
	static {
		try {
			INSTANCE = new DefaultOpenings();
		} catch (IOException e) {
			throw new UncheckedIOException(e);
		}
	}

	private final JSONObject db;
	
	private DefaultOpenings() throws IOException {
		this(() -> DefaultOpenings.class.getResourceAsStream(KNOWN), true);
	}
	
	public DefaultOpenings(Supplier<InputStream> stream, boolean zipped) throws IOException {
		db = readJSON(stream, zipped);
	}
	
	private JSONObject readJSON(Supplier<InputStream> stream, boolean zipped) throws IOException {
		try (InputStream in = zipped ? new GZIPInputStream(stream.get()) : stream.get()) {
			return new JSONObject(new JSONTokener(in));
		}
	}
	
	private String toFen(Board<Move> board) {
		String fen = FENUtils.to(board);
		int index = fen.lastIndexOf(' ', fen.lastIndexOf(' ')-1);
		return fen.substring(0, index);
	}

	@Override
	public Move apply(Board<Move> board) {
		final String fen = toFen(board);
		final JSONObject opening = db.optJSONObject(fen);
		if (opening==null) {
			return null;
		}
		final JSONArray moves = opening.getJSONArray("moves");
		if (moves.isEmpty()) {
			System.out.println("Strange, moves is empty for fen "+fen); //TODO
			return null;
		}
		final JSONObject move = moves.getJSONObject(RND.nextInt(moves.length()));
		return JChessUCIEngine.toMove(board, UCIMove.from(move.getString("coord")));
	}
}
