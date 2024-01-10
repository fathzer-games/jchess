package com.fathzer.jchess.uci;

import java.io.IOException;
import java.io.InputStream;
import java.io.UncheckedIOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Collection;
import java.util.Deque;
import java.util.List;

import com.fathzer.games.perft.PerfTParser;
import com.fathzer.games.perft.PerfTTestData;
import com.fathzer.jchess.uci.extended.ExtendedUCI;
import com.fathzer.jchess.uci.extended.SpeedTest;
import com.fathzer.plugin.loader.jar.JarPluginLoader;
import com.fathzer.plugin.loader.utils.FileUtils;

public class JChessUCI extends ExtendedUCI {
	public static void main(String[] args) {
		try (UCI uci = new JChessUCI()) {
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
	}
	
	public JChessUCI() {
		super(new JChessUCIEngine());
		addCommand(this::speedTest, "st");
	}
	
	@Override
	protected Collection<PerfTTestData> readTestData() {
		try (InputStream stream = JChessUCI.class.getResourceAsStream("/Perft.txt")) {
			return new PerfTParser().withStartPositionPrefix("position fen").withStartPositionCustomizer(s -> s+" 0 1").read(stream, StandardCharsets.UTF_8);
		} catch (IOException e) {
			throw new UncheckedIOException(e);
		}
	}

	private void speedTest(Deque<String> args) {
		if (engine instanceof JChessUCIEngine) {
			out("completed in "+new SpeedTest<>((JChessUCIEngine)engine).run()+"ms");
		} else {
			debug("This engine does not support this command");
		}
	}
}
