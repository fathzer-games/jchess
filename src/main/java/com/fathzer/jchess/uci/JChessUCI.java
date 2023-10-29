package com.fathzer.jchess.uci;

import java.io.IOException;
import java.io.InputStream;
import java.io.UncheckedIOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Collection;
import java.util.List;

import com.fathzer.games.ai.evaluation.EvaluatedMove;
import com.fathzer.games.ai.evaluation.Evaluation;
import com.fathzer.games.ai.evaluation.Evaluation.Type;
import com.fathzer.games.perft.PerfTParser;
import com.fathzer.games.perft.PerfTTestData;
import com.fathzer.jchess.Board;
import com.fathzer.jchess.CoordinatesSystem;
import com.fathzer.jchess.Move;
import com.fathzer.jchess.ai.JChessEngine;
import com.fathzer.jchess.ai.evaluator.BasicEvaluator;
import com.fathzer.jchess.fen.FENParser;
import com.fathzer.plugin.loader.jar.JarPluginLoader;
import com.fathzer.plugin.loader.utils.FileUtils;

public class JChessUCI extends UCI {
	public static void main(String[] args) {
		final UCI uci = new JChessUCI();
		final JarPluginLoader loader = new JarPluginLoader();
		final Path pluginsFolder = Paths.get("uci-plugins");
		if (Files.isDirectory(pluginsFolder)) {
			try {
				final List<Path> jars = FileUtils.getJarFiles(pluginsFolder, 1);
				for (Path jar:jars) {
					final List<Engine> plugins = loader.getPlugins(jar, Engine.class);
					plugins.forEach(uci::add);
				}
			} catch (IOException e) {
				//TODO Log failure when loading plugins 
				e.printStackTrace();
			}
		}
		uci.run();
	}
	
	public JChessUCI() {
		super(new JChessUCIEngine());
		addCommand(this::speedTest, "st"); //TODO Strange seems always use JChess engine!
	}
	
	@Override
	protected Collection<PerfTTestData> readTestData() {
		try (InputStream stream = JChessUCI.class.getResourceAsStream("/Perft.txt")) {
			return new PerfTParser().withStartPositionPrefix("position fen").withStartPositionCustomizer(s -> s+" 0 1").read(stream, StandardCharsets.UTF_8);
		} catch (IOException e) {
			throw new UncheckedIOException(e);
		}
	}
	
	private static class MovesAndMore {
		private final JChessEngine engine;
		private CoordinatesSystem cs;
		private String fen;
		private List<EvaluatedMove<Move>> moves;
		
		MovesAndMore(JChessEngine engine, String fen) {
			this.engine = engine;
			fill(fen);
		}
		
		void fill(String fen) {
			Board<Move> board = FENParser.from(fen);
			this.cs = board.getCoordinatesSystem();
			moves = engine.getBestMoves(board);
		}

		private void assertEquals(Object expected, Object actual) {
			if (!expected.equals(actual)) {
				show();
				throw new IllegalArgumentException("Expecting "+expected+" but is "+actual);
			}
		}

		private void assertTrue(boolean value) {
			if (!value) {
				show();
				throw new IllegalArgumentException("Expecting true here");
			}
		}
		
		private void show() {
			System.out.println(fen);
			System.out.println(EvaluatedMove.toString(moves, m -> m.toString(cs)));
		}
	}
	
	private void speedTest(String[] args) {
		final long start = System.currentTimeMillis();
		final JChessEngine engine = new JChessEngine(new BasicEvaluator(), 8);
		engine.getSearchParams().setSize(Integer.MAX_VALUE);
		if (args.length!=0) {
			engine.setParallelism(Integer.parseInt(args[0]));
		}
		
		// 3 possible Mats in 1 with whites
		final MovesAndMore mv = new MovesAndMore(engine, "7k/5p2/5PQN/5PPK/6PP/8/8/8 w - - 6 5");
		mv.assertEquals(6, mv.moves.size());
		{
			final Evaluation max = mv.moves.get(0).getEvaluation();
			mv.assertEquals(Type.WIN, max.getType());
			mv.assertEquals(1, max.getCountToEnd());
			mv.assertTrue(mv.moves.get(3).getEvaluation().compareTo(max)<0);
			mv.moves.stream().limit(3).forEach(m -> mv.assertEquals(max, m.getEvaluation()));
		}

		// Mat in 1 with blacks
		mv.fill("1R6/8/8/7R/k7/ppp1p3/r2bP3/1K6 b - - 6 5");
		mv.assertEquals(7, mv.moves.size());
		Evaluation max = mv.moves.get(0).getEvaluation();
		mv.assertEquals(Type.WIN, max.getType());
		mv.assertEquals(1, max.getCountToEnd());
		Move m = mv.moves.get(0).getContent();
		mv.assertEquals("c3", mv.cs.getAlgebraicNotation(m.getFrom()));
		mv.assertEquals("c2", mv.cs.getAlgebraicNotation(m.getTo()));
		max = mv.moves.get(1).getEvaluation();
		//TODO iterative engine fails to find the second best move in tree, probably because of deepening interruption by first mat
		// Make a test when it will be fixed with a second move that is a MAT in 3 move (see commented code). 
//		mv.assertEquals(Type.WIN, max.getType());
//		mv.assertEquals(3, max.getCountToEnd());
//		mv.assertEquals(Type.EVAL, mv.moves.get(2).getEvaluation().getType());
		
		// Check in 2
		mv.fill("8/8/8/8/1B6/NN6/pk1K4/8 w - - 0 1");
		max = mv.moves.get(0).getEvaluation();
		mv.assertEquals(Type.WIN, max.getType());
		mv.assertEquals(2, max.getCountToEnd());
		mv.assertTrue(mv.moves.get(1).getScore()<max.getScore());
		m = mv.moves.get(0).getContent();
		mv.assertEquals("b3", mv.cs.getAlgebraicNotation(m.getFrom()));
		mv.assertEquals("a1", mv.cs.getAlgebraicNotation(m.getTo()));
		
		// Check in 2 with blacks
		mv.fill("8/4k1KP/6nn/6b1/8/8/8/8 b - - 0 1");
		max = mv.moves.get(0).getEvaluation();
		mv.assertEquals(Type.WIN, max.getType());
		mv.assertEquals(2, max.getCountToEnd());
		mv.assertTrue(mv.moves.get(1).getScore()<max.getScore());
		mv.assertEquals("g6", mv.cs.getAlgebraicNotation(mv.moves.get(0).getContent().getFrom()));
		mv.assertEquals("h8", mv.cs.getAlgebraicNotation(mv.moves.get(0).getContent().getTo()));
		
		// Check in 3
		engine.getSearchParams().setSize(3);
		engine.getSearchParams().setAccuracy(100);
		mv.fill("r2k1r2/pp1b2pp/1b2Pn2/2p5/Q1B2Bq1/2P5/P5PP/3R1RK1 w - - 0 1");
		mv.assertEquals(19, mv.moves.size());
		m = mv.moves.get(0).getContent();
		mv.assertEquals("d1", mv.cs.getAlgebraicNotation(m.getFrom()));
		mv.assertEquals("d7", mv.cs.getAlgebraicNotation(m.getTo()));
		
		// Check in 4
		engine.getSearchParams().setSize(1);
		engine.getSearchParams().setAccuracy(0);
		mv.fill("8/4k3/8/R7/8/8/8/4K2R w K - 0 1");
		mv.assertEquals(2, mv.moves.size());
		mv.assertEquals(Evaluation.Type.WIN, mv.moves.get(0).getEvaluation().getType());
		mv.assertEquals(4, mv.moves.get(0).getEvaluation().getCountToEnd());
		mv.assertEquals(Evaluation.Type.WIN, mv.moves.get(1).getEvaluation().getType());
		mv.assertEquals(4, mv.moves.get(1).getEvaluation().getCountToEnd());

		out("completed in "+(System.currentTimeMillis()-start)+"ms");
	}
}
