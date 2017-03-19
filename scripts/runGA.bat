@ECHO OFF

SETLOCAL
SET "lock=%temp%\wait%random%.lock"

:LOOP

START "" CMD /C 9>"%lock%1" startupLadder.bat
START "" CMD /C 9>"%lock%2" startupLadder.bat
START "" CMD /C 9>"%lock%3" startupLadder.bat

:: Basically, wait a set time before cheking if files are there
PING -N 2 -W 1000 127.1 >NUL

:: Limit number of programs by checking the lock on the file... 
:WAIT
FOR %%N in (1 1 3) DO (
	( rem
	) 9>"%lock%%%N" || GOTO :WAIT
) 2>NUL

:: Loop back to start
GOTO LOOP

:END

PAUSE