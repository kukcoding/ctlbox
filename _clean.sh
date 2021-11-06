#!/usr/bin/env bash

set -e
# ./gradlew :app:assembleProdDebug
./gradlew :app:clean

find . -type -d -name build -exec rm -rf {} \;


