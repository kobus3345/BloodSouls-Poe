@rem Gradle wrapper stub for Windows.
@rem Open the project in Android Studio to auto-download the full Gradle wrapper,
@rem or run: gradle wrapper --gradle-version 8.7

@echo off
setlocal

set SCRIPT_DIR=%~dp0
set WRAPPER_JAR=%SCRIPT_DIR%gradle\wrapper\gradle-wrapper.jar

if not exist "%WRAPPER_JAR%" (
    echo ERROR: gradle-wrapper.jar is missing.
    echo Open the project in Android Studio to generate the wrapper, or run:
    echo   gradle wrapper --gradle-version 8.7
    exit /b 1
)

java -jar "%WRAPPER_JAR%" %*
