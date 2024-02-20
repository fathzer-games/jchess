# jchess
A Swing chess application and a UCI engine based on [JChess-core](https://github.com/fathzer-games/jchess-core).

## Requirements
Java 11+ to run the application and mvn to build it.

## How to compile the project

```mvn package```

## How to launch the swing application
```java ./target/jchess.jar```

## How to launch the UCI engine
```java -Duci=true -jar ./target/jchess.jar```

# Known bugs
- Engine lists in player settings are not updated when variant changes.
- The displayed score is mutiplied per 100.
- The score + resign icon are not initialized at the same time as the board.
- The depth of internal engine is always set to 2

# TODO
- Allow engine settings to be defined in engines.json.
- Alert user when two external engines have the same name or add an interface to create engine.
- Implement a way to play again on missclick.
- Implement game loading and move backward/forward in the game.
- Use com.fathzer.jchess.gui.GameGUI interface or delete it.
- Externalize perfT data (I think it's more than the half of the package size for a very limited usage).