#!/usr/bin/env bash

echo "[ktor-sentry:publish] Now publishing to Maven repository..."
./gradlew publish -Dorg.gradle.s3.endpoint=https://s3.wasabisys.com
