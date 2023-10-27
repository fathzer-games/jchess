SET PATH=C:\Program files\Java\jdk-11.0.5\bin
echo %PATH%
rem java -DdebugUCI=false -cp target/classes com.fathzer.jchess.uci.UCI
java -Duci=true -DdebugUCI=false ./target/jchess.jar

pause