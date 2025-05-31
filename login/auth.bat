@echo off
title LOGIN SERVER
@Color 0E
:start
java -Xmx128m -cp ../libs/*; com.dream.auth.L2AuthServer
if ERRORLEVEL 2 goto restart
if ERRORLEVEL 1 goto error
goto end
:restart
echo.
echo Admin have restarted, please wait.
echo.
goto start
:error
echo.
echo Server have terminated abnormaly.
echo.
:end
echo.
echo Server terminated.
echo.
pause