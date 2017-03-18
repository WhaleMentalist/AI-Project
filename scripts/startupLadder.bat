@ECHO OFF
ECHO Starting ladder...
cd ..
CALL ant -buildfile build.xml coopLadder
QUIT