#!/usr/bin/env bash

echo "[ktor-sentry:docs] Deploying documentation!"
rm -rf docs
./gradlew dokkaHtml

echo "[ktor-sentry:docs] Done!"
