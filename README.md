# jchess
A Swing chess application and a UCI engine based on [JChess-core](https://github.com/fathzer-games/jchess-core).

## Requirements
Java 17+ to run the application and mvn to build it.

## How to compile the project

```mvn package```

## How to launch the UCI engine
```java -Duci=true -jar ./target/jchess.jar```

## How to launch the swing application
```java -jar ./target/jchess.jar```

## Where are the pgn of the games played saved?
The application saves the pgn of games in ```./data/pgn```. THis folder will contain one file per day.

## How to add external [UCI](https://gist.github.com/DOBRO/2592c6dad754ba67e6dcaec8c90165bf) engines to the engines list
Add a json file named *engines.json* in the *data* folder using the following example:

```json
{
"engines": [
	{
		"name": "Dragon",
		"command": ["C:/Program Files (x86)/Arena/Engines/Dragon/Dragon_46.exe"]
	},
	{
		"name": "ChesLib",
		"command": ["C:/Program Files/Java/jdk-17/bin/java","-jar","C:/Users/me/git/chesslib-uci-engine/target/chesslib-uci-engine.jar"]
	}
]}
```

Please note that engines should have different names, if not, only the first engine will have its original name, next will have a changed names (the original name followed by a suffix). The name *JChess* is reserved for the internal engine; If it is used for an external engine, this engine name will have a suffix like duplicated ones.

# Tips and hints
Warning, the tips in this section may not be maintained... Use it at your own risk.

## How to make multiple games between two engines?
By default, at the end of a game, the user is asked for making a revenge or ends playing.
If you specify a value for the *gameCount* system property while starting the application. The application will make *gameCount* games without asking the user if it want to continue.


# Known bugs
- When a engine that is used in player settings hangs, it leaves settings in a wrong state with no possibility to fix it; The application should be restarted.
- When a human looses against an engine because its allocated time is elapsed, and it decides to continue the game, the bot never moves again (it receives a negative time counter ... which is strange).

# TODO
- Allow engine settings to be defined in engines.json or save engine settings in preferences.
- PGN should contain 
- Implement a way to play again on missclick.
- Implement PGN game loading
- Implement move backward/forward in the game.
- Use com.fathzer.jchess.gui.GameGUI interface or delete it.
- Externalize perfT data in order to reduce the package size?
