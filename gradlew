#!/usr/bin/env bash
set -euo pipefail

GRADLE_VERSION="8.7"
BASE_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"
CACHE_DIR="${GRADLE_USER_HOME:-$HOME/.gradle}/wrapper-distros"
DIST_ZIP="$CACHE_DIR/gradle-${GRADLE_VERSION}-bin.zip"
DIST_DIR="$CACHE_DIR/gradle-${GRADLE_VERSION}"
GRADLE_BIN="$DIST_DIR/bin/gradle"

if command -v gradle >/dev/null 2>&1; then
  exec gradle "$@"
fi

mkdir -p "$CACHE_DIR"

if [ ! -x "$GRADLE_BIN" ]; then
  if [ ! -f "$DIST_ZIP" ]; then
    echo "Downloading Gradle $GRADLE_VERSION..."
    curl -L --fail --retry 3 --retry-delay 2 \
      "https://services.gradle.org/distributions/gradle-${GRADLE_VERSION}-bin.zip" \
      -o "$DIST_ZIP"
  fi
  rm -rf "$DIST_DIR"
  unzip -q "$DIST_ZIP" -d "$CACHE_DIR"
fi

exec "$GRADLE_BIN" "$@"
