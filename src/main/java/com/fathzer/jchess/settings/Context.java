package com.fathzer.jchess.settings;

import java.util.List;

import com.fathzer.jchess.bot.uci.EngineLoader.EngineData;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class Context {
	private GameSettings settings;
	private List<EngineData> engines;
}
