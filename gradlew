#!/bin/sh
if [ ! -d "back-end" ]; then
  echo "Error: back-end directory not found"
  exit 1
fi
cd back-end && exec ./gradlew "$@"
