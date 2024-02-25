# jchess
A Swing chess application and a UCI engine based on [JChess-core](https://github.com/fathzer-games/jchess-core).

## Requirements
Java 11+ to run the application and mvn to build it.

## How to compile the project

```mvn package```

## How to launch the UCI engine
```java -Duci=true -jar ./target/jchess.jar```

## How to launch the swing application
```java ./target/jchess.jar```

## How to add external [UCI](https://gist.github.com/DOBRO/2592c6dad754ba67e6dcaec8c90165bf) engines to the engines list
Add a json file in the *data* folder using the following example:

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

# Known bugs

# TODO
- Allow engine settings to be defined in engines.json or save engine settings in preferences.
- Implement a way to play again on missclick.
- Implement PGN game loading
- Implement move backward/forward in the game.
- Use com.fathzer.jchess.gui.GameGUI interface or delete it.
- Externalize perfT data in order to reduce the package size?