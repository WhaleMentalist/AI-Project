@ECHO OFF

SETLOCAL
SET "lock=%temp%\wait%random%.lock"

:: Delete the files used for locking
DEL "%lock%*" 
QUIT