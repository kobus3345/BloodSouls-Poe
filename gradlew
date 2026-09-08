#!/bin/sh
# Gradle wrapper stub — run `gradle wrapper` inside Android Studio or via the CLI
# to regenerate the full wrapper with gradle-wrapper.jar.
#
# Alternatively, open the project in Android Studio and it will download
# and configure the Gradle wrapper automatically.

SCRIPT_DIR="$(cd "$(dirname "$0")" && pwd)"
WRAPPER_JAR="$SCRIPT_DIR/gradle/wrapper/gradle-wrapper.jar"

if [ ! -f "$WRAPPER_JAR" ]; then
  echo "ERROR: gradle-wrapper.jar is missing."
  echo "Open the project in Android Studio to let it generate the wrapper, or run:"
  echo "  gradle wrapper --gradle-version 8.7"
  exit 1
fi

exec java -jar "$WRAPPER_JAR" "$@"
