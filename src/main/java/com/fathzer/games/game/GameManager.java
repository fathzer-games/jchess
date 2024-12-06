package com.fathzer.games.game;

import java.util.Optional;
import java.util.function.BiConsumer;

import com.fathzer.games.Color;
import com.fathzer.games.GameHistory;
import com.fathzer.games.MoveGenerator;
import com.fathzer.games.Status;
import com.fathzer.jchess.Score;
import com.fathzer.games.clock.Clock;
import com.fathzer.games.clock.ClockSettings;
import com.fathzer.games.game.AbstractGameSettings.ColorSetting;
import com.fathzer.util.Observable;

import lombok.Getter;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public abstract class GameManager<M,B extends MoveGenerator<M>,S extends AbstractGameSettings> implements Runnable {
	public enum State {
		CREATED, PAUSED, RUNNING, ENDED
	}
	
	private final Player<M, B> player1;
	private final Player<M, B> player2;
	private final Score score;
	@Getter
	private S settings;
	@Getter
	private Color player1Color;
	private Observable<State> state;
	protected Game<M, B> game;

	public Score getScore() {
		return score;
	}
	
	protected GameManager(S settings, Player<M, B> player1, Player<M, B> player2) {
		this.player1 = player1;
		this.player2 = player2;
		this.state = new Observable<>(State.CREATED);
		state.addListener(this::onStateChanged);
		score = new Score();
		setSettings(settings);
	}
	
	@Override
	public void run() {
		while (state.getValue()!=State.ENDED) {
			state.setValue(State.RUNNING);
			this.game.run();
			endOfGame(this.game.getHistory().getStatus());
		}
	}
	
	protected void onStateChanged(State old, State current) {
	}
	
	private void doRevenge() {
		final Color previous1 = player1Color;
		if (ColorSetting.RANDOM.equals(settings.getPlayer1Color()) && score.getGameCount()%2==0) {
			player1Color = settings.getPlayer1Color().getColor();
		} else {
			player1Color = player1Color.opposite();
		}
		if (!previous1.equals(player1Color)) {
			onPlayerColorsChanged();
		}
		newGame();
		setState(State.PAUSED);
		start();
	}
	
	protected void onPlayerColorsChanged() {
		// Allows subclasses to perform extra initialization when players color changes
	}
	
	public Player<M,B> getPlayer(Color color) {
		if (color==null) {
			throw new IllegalArgumentException();
		}
		return color==player1Color ? player1 : player2;
	}
	
	/**
	 * @return the player1
	 */
	public Player<M, B> getPlayer1() {
		return player1;
	}

	/**
	 * @return the player2
	 */
	public Player<M, B> getPlayer2() {
		return player2;
	}

	/**
	 * 	final Board<Move> board = settings.getVariant().getRules().apply(settings.getFen());
	 * @return //TODO
	 */
	protected abstract B getStartPosition();
	
	private void newGame() {
		this.game = new Game<>(new GameHistory<>(getStartPosition()), buildClock(), getPlayer(Color.WHITE), getPlayer(Color.BLACK));
		log.debug("New game created: {}", this.game.getId());
		this.game.setStartClockAfterFirstMove(settings.isStartClockAfterFirstMove());
		score.newGame();
		onNewGame(this.game);
	}
	
	/** A new Game was created.
	 * <br>This method does nothing but subclasses can override it to perform extra actions when new game is created.
	 * @param game The new game
	 */
	protected void onNewGame(Game<M,B> game) {
		// Does nothing, can be used by subclass
	}

	protected Clock buildClock() {
		if (settings.getClockSettings()!=null) {
			final ClockSettings common = settings.getClockSettings();
//TODO integrate player 2 extra time to ClockSettings? 
//			final int extraTime = Integer.getInteger("player2ExtraTimeS",0);
			final Clock clock;
//			if (extraTime==0) {
				clock = new Clock(common);
//			} else {
//				final ClockSettings other = new ClockSettings(common.getInitialTime()+extraTime).withIncrement(common.getIncrement(), common.getMovesNumberBeforeIncrement(), common.isCanAccumulate());
//				clock = player1Color==Color.WHITE ? new Clock(common, other) : new Clock(other, common);
//			}
			if (settings.isStartClockAfterFirstMove()) {
				clock.withStartingColor(Color.BLACK);
			}
			return clock;
		} else {
			return null;
		}
	}

	public void addListener(BiConsumer<State,State> listener) {
		this.state.addListener(listener);
	}
	
	private void start() {
		if (State.ENDED.equals(getState())) {
			this.score.reset();
			newGame();
		}
		setState(State.RUNNING);
	}

	public State getState() {
		return this.state.getValue();
	}
	
	private void setState(State state) {
		this.state.setValue(state);
	}
	
	public int getTournamentGamesCount() {
		return Integer.getInteger("gameCount",0);
	}

	private void endOfGame(final Status status) {
		setState(State.PAUSED);
		final Optional<Game<M,B>> resumed = getResumedGame();
		if (resumed.isPresent()) {
			// Replace game by the same one returned (for instance, the same game without clock)
			this.game = resumed.get();
			log.debug("Continue ended with another game at same position, state: {}", getState());
			onNewGame(this.game);
			setState(State.RUNNING);
		} else {
			log.debug("End of game,  state: {}", getState());
			updateScores(status);
			onGameEnded();
			if (isMakeRevenge(status)) {
				doRevenge();
			} else {
				setState(State.ENDED);
			}
		}
	}
	
	/** Gets a new game to start just after a game just ended.
	 * <br>A typical use of this method is to allow a game lost by time forfeit to be resumed without a clock.
	 * @return an empty option if the game should not be resumed or the game to resume. 
	 */
	protected Optional<Game<M,B>> getResumedGame() {
		return Optional.empty();
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

	public void setSettings(S settings) {
		if (State.RUNNING.equals(getState()) || State.PAUSED.equals(getState())) {
			throw new IllegalStateException("Can't change the game settings during the game");
		}
		this.settings = settings;
		player1Color = settings.getPlayer1Color().getColor();
		score.reset();
		newGame();
	}
}
