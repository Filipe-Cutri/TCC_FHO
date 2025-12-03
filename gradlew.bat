@echo off
if not exist "back-end" (
  echo Error: back-end directory not found
  exit /b 1
)
cd /d back-end && call gradlew.bat %*
