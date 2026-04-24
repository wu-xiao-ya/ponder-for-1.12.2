@echo off
setlocal

set "JAVA_HOME=C:\Program Files\Java\jdk-25.0.2"
set "PATH=%JAVA_HOME%\bin;%PATH%"

call "%~dp0..\gradlew.bat" %*
exit /b %ERRORLEVEL%
