#!/usr/bin/env bash

set -e
# ./gradlew :app:assembleProdDebug
./gradlew :app:assembleDevDebug

scp app/build/outputs/apk/dev/debug/*.apk app@192.168.114.3:/home/www/html/p/kuk/apks/