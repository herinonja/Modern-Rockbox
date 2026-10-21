@rem Gradle wrapper batch script for Windows with Java 21 support
@echo off
set DIRNAME=%~dp0
if "%DIRNAME%" == "" set DIRNAME=.
set WRAPPER_JAR=%DIRNAME%gradle\wrapper\gradle-wrapper.jar

if not exist "%WRAPPER_JAR%" (
    echo Downloading Gradle Wrapper 8.7...
    powershell -Command "[Net.ServicePointManager]::SecurityProtocol = [Net.SecurityProtocolType]::Tls12; (New-Object Net.WebClient).DownloadFile('https://raw.githubusercontent.com/gradle/gradle/v8.7.0/gradle/wrapper/gradle-wrapper.jar', '%WRAPPER_JAR%')"
)

if defined JAVA_HOME (
    set JAVACMD="%JAVA_HOME%\bin\java.exe"
) else (
    set JAVACMD=java
)

if exist "%WRAPPER_JAR%" (
    %JAVACMD% -Xmx4096m -XX:MaxMetaspaceSize=1024m -XX:+UseG1GC -Dfile.encoding=UTF-8 -classpath "%WRAPPER_JAR%" org.gradle.wrapper.GradleWrapperMain --no-daemon %*
) else (
    gradle --no-daemon %*
)
