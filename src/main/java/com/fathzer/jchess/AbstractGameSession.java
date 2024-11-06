package com.fathzer.jchess;

import java.util.Optional;
import java.util.function.BiConsumer;

import com.fathzer.games.Color;
import com.fathzer.games.Status;
import com.fathzer.jchess.bot.Engine;
import com.fathzer.jchess.bot.uci.EngineLoader;
import com.fathzer.jchess.bot.uci.EngineLoader.EngineData;
import com.fathzer.games.clock.Clock;
import com.fathzer.games.clock.ClockSettings;
import com.fathzer.jchess.settings.Settings;
import com.fathzer.jchess.settings.Settings.ColorSetting;
import com.fathzer.jchess.settings.Settings.EngineSettings;
import com.fathzer.util.Observable;

import lombok.extern.slf4j.Slf4j;

@Slf4j
public abstract class AbstractGameSession<T> {
	public enum State {
		CREATED, PAUSED, RUNNING, ENDED
	}
	
	protected final T gui;
	private Settings settings;
	private Engine whiteEngine;
	private Engine blackEngine;
	private Score score;
	protected Color player1Color;
	private Observable<State> state;
	protected Game game;

	public Score getScore() {
		return score;
	}
	
	public Settings getSettings() {
		return settings;
	}
	
	protected AbstractGameSession(T gui, Settings settings) {
		this.gui = gui;
		this.state = new Observable<>(State.CREATED);
		state.addListener(this::onStateChanged);
		score = new Score();
		setSettings(settings);
	}
	
	protected void onStateChanged(State old, State current) {
		log.debug("State changed from {} to {}", old,current);
		if (State.RUNNING.equals(current)) {
			game.start();
		} else {
			game.pause();
		}
	}

	private void doRevenge() {
		final Color previous1 = player1Color;
		if (ColorSetting.RANDOM.equals(settings.getPlayer1Color()) && score.getGameCount()%2==0) {
			player1Color = settings.getPlayer1Color().getColor();
		} else {
			player1Color = player1Color.opposite();
		}
		if (!previous1.equals(player1Color)) {
			// Switch engines color
			Engine dummy = whiteEngine;
			whiteEngine = blackEngine;
			blackEngine = dummy;
			onPlayerColorsChanged();
		}
		newGame();
		setState(State.PAUSED);
		start();
	}
	
	protected void onPlayerColorsChanged() {
		// Allows subclasses to perform extra initialization when players color changes
	}
	
	protected void newGame() {
		this.game = new Game(settings.getVariant().getRules().apply(settings.getFen()), buildClock());
		this.game.setStartClockAfterFirstMove(settings.isStartClockAfterFirstMove());
		score.newGame();
	}

	private Clock buildClock() {
		if (settings.getClock()!=null) {
			final ClockSettings common = settings.getClock().toClockSettings();
			final int extraTime = Integer.getInteger("player2ExtraTimeS",0);
			final Clock clock;
			if (extraTime==0) {
				clock = new Clock(common);
			} else {
				final ClockSettings other = new ClockSettings(common.getInitialTime()+extraTime).withIncrement(common.getIncrement(), common.getMovesNumberBeforeIncrement(), common.isCanAccumulate());
				clock = player1Color==Color.WHITE ? new Clock(common, other) : new Clock(other, common);
			}
			clock.addStatusListener(this::doTimeUp);
			clock.addClockListener(e -> log.debug("Clock {} state changes from {} to {}",e.getClock(), e.getPreviousState(), e.getNewState()));
			if (settings.isStartClockAfterFirstMove()) {
				clock.withStartingColor(Color.BLACK);
			}
			return clock;
		} else {
			return null;
		}
	}
	
	public Engine getEngine(EngineSettings settings) {
		if (settings==null) {
			return null;
		}
		final Optional<EngineData> found = EngineLoader.getEngines().stream().filter(e -> e.getEngine()!=null && e.getName().equals(settings.getName())).findAny();
		return found.orElseThrow().getEngine();
	}

	protected boolean onlyHumans() {
		return this.settings.getPlayer1().getEngine()==null && this.settings.getPlayer2().getEngine()==null;
	}
	
	public void addListener(BiConsumer<State,State> listener) {
		this.state.addListener(listener);
	}
	
	public void setEngine(Color color, Engine engine) {
		if (Color.WHITE.equals(color)) {
			this.whiteEngine = engine;
		} else if (Color.BLACK.equals(color)) {
			this.blackEngine = engine;
		}
	}
	
	protected Engine getEngine(Color color) {
		if (Color.WHITE.equals(color)) {
			return this.whiteEngine;
		} else if (Color.BLACK.equals(color)) {
			return this.blackEngine;
		} else {
			throw new NullPointerException();
		}
	}
	
	public void start() {
		if (State.ENDED.equals(getState())) {
			this.score.reset();
			newGame();
		}
		setEngine(player1Color, getEngine(settings.getPlayer1().getEngine()));
		setEngine(player1Color.opposite(), getEngine(settings.getPlayer2().getEngine()));
		setState(State.RUNNING);
		//TODO With the nextMove here, TournamentGameSession goes (sometime) crazy !!!
		//TODO The alternative is having the subclass trap the state change to call nextMove itself ... wich should do exactly the same !!!
		nextMove();
	}

	protected abstract void nextMove();
	
	protected abstract void play(Game game, Move move);

	public State getState() {
		return this.state.getValue();
	}
	
	private void setState(State state) {
		this.state.setValue(state);
	}

	/** This method is called when clock emits a time up event.
	 * <br>Please note that this method could be invoked on a thread that is not the Swing event thread.
	 * <br>One can override this method to ensure the correct thread is used.
	 * @param status The game status.
	 */
	protected void doTimeUp(Status status) {
		if (getTournamentGamesCount()==0) {
			// If we are not in tournament mode, propose to continue without clock
			this.setState(State.PAUSED);
			if (continueOnTimeup(status)) {
				this.setState(State.RUNNING);
				nextMove();
				return;
			}
		}
		endOfGame(status);
	}
	
	protected boolean continueOnTimeup(Status status) {
		return false;
	}
	
	protected void resign(Color color) {
		if (!getState().equals(State.RUNNING)) {
			return;
		}
		setState(State.PAUSED);
		if (game.getBoard().getActiveColor().equals(color) && isResignationConfirmed()) {
			final Status status = Color.WHITE.equals(color) ? Status.BLACK_WON : Status.WHITE_WON;
			endOfGame(status);
		} else {
			setState(State.RUNNING);
			nextMove();
		}
	}
	
	protected boolean isResignationConfirmed() {
		return true;
	}
	
	public int getTournamentGamesCount() {
		return Integer.getInteger("gameCount",0);
	}

	protected void endOfGame(final Status status) {
		setState(State.PAUSED);
		log.debug("End of game,  state: {}", getState());
		updateScores(status);
		onGameEnded();
		if (isMakeRevenge(status)) {
			doRevenge();
		} else {
			setState(State.ENDED);
		}
	}
	
	/** This method is called when game just finished.
	 *  <br>It does nothing by default but allows subclasses to perform some specific actions (for instance, output a summary of the game in a file). 
	 */
	protected void onGameEnded() {
		// Does nothing by default
	}
	
	protected boolean isMakeRevenge(final Status status) {
		final int toPlay = getTournamentGamesCount();
		return toPlay!=0 && score.getGameCount()<toPlay;
	}
	
	private void updateScores(Status status) {
		if (Status.DRAW.equals(status)) {
			score.draw();
		} else {
			score.win(status.winner()==player1Color);
		}
	}
	
	public void stop() {
		setState(State.PAUSED);
	}

	public void setSettings(Settings settings) {
		if (State.RUNNING.equals(getState()) || State.PAUSED.equals(getState())) {
			throw new IllegalStateException("Can't change the game settings during the game");
		}
		this.settings = settings;
		player1Color = settings.getPlayer1Color().getColor();
		score.reset();
		newGame();
	}
}
