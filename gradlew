#!/usr/bin/env bash
# Gradle Wrapper 8.7 compatible Java 21 LTS (macOS / Linux)
set -e
DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"
WRAPPER_DIR="$DIR/gradle/wrapper"
WRAPPER_JAR="$WRAPPER_DIR/gradle-wrapper.jar"

if [ ! -s "$WRAPPER_JAR" ]; then
    echo "==> Téléchargement du Gradle Wrapper 8.7 compatible Java 21..."
    mkdir -p "$WRAPPER_DIR"
    curl -sSL -o "$WRAPPER_JAR" "https://raw.githubusercontent.com/gradle/gradle/v8.7.0/gradle/wrapper/gradle-wrapper.jar" || true
fi

if [ -n "$JAVA_HOME" ] && [ -x "$JAVA_HOME/bin/java" ]; then
    JAVACMD="$JAVA_HOME/bin/java"
else
    JAVACMD="java"
fi

if [ -s "$WRAPPER_JAR" ]; then
    exec "$JAVACMD" -Xmx4096m -XX:MaxMetaspaceSize=1024m -XX:+UseG1GC -Dfile.encoding=UTF-8 -Dorg.gradle.appname=gradlew -classpath "$WRAPPER_JAR" org.gradle.wrapper.GradleWrapperMain --no-daemon "$@"
else
    exec gradle --no-daemon "$@"
fi
