@echo off
setlocal

set "JAVA_HOME=C:\Program Files\Java\jdk-25.0.2"
if exist "%JAVA_HOME%\bin\java.exe" goto run

set "JAVA_HOME=C:\Program Files\Java\jdk-25"
if exist "%JAVA_HOME%\bin\java.exe" goto run

set "JAVA_HOME=C:\Program Files\Eclipse Adoptium\jdk-25.0.2.10-hotspot"
if exist "%JAVA_HOME%\bin\java.exe" goto run

echo No supported Java 25 installation was found.
echo Update scripts\gradle-jdk25.cmd or install JDK 25.
exit /b 1

:run
call "%~dp0..\gradlew.bat" %*
