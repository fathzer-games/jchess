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

# TODO
- Remove the empty moves proposal in masters.json.gz resource (and find why they are there)
- Allow engine settings to be defined in engines.json.
- Alert user when two external engines have the same name or add an interface to create engines.
- Implement a way to play again on missclick.
- Implement PGN game loading
- Implement move backward/forward in the game.
- Use com.fathzer.jchess.gui.GameGUI interface or delete it.
- Externalize perfT data (I think it's more than the half of the package size for a very limited usage).