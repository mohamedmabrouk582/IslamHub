#!/usr/bin/env bash
set -euo pipefail

ROOT_DIR="$(cd "$(dirname "$0")/.." && pwd)"
GITHUB_USER="${GITHUB_USER:-mohamedmabrouk582}"
USER_SITE_REPO="https://github.com/${GITHUB_USER}/${GITHUB_USER}.github.io.git"
WORK_DIR="$(mktemp -d)"

cleanup() {
  rm -rf "$WORK_DIR"
}
trap cleanup EXIT

echo "Deep link files live in: ${ROOT_DIR}/docs"
echo ""
echo "1) Push IslamHub to GitHub if you have not already:"
echo "   git add docs app/build.gradle.kts app/src/main/AndroidManifest.xml app/src/main/java/com/example/navigation/QuranDeepLink.kt"
echo "   git commit -m \"Add GitHub Pages deep link hosting\""
echo "   git push origin main"
echo ""
echo "2) Enable GitHub Pages on IslamHub:"
echo "   https://github.com/${GITHUB_USER}/IslamHub/settings/pages"
echo "   Source: Deploy from branch -> main -> /docs -> Save"
echo ""
echo "3) After 1-2 minutes, test:"
echo "   https://${GITHUB_USER}.github.io/IslamHub/.well-known/assetlinks.json"
echo ""
echo "Optional (opens app directly without browser):"
echo "Create repo ${GITHUB_USER}.github.io and publish assetlinks at the domain root."

if ! git clone "$USER_SITE_REPO" "$WORK_DIR" 2>/dev/null; then
  echo ""
  echo "User-site repo not found yet. Browser redirect from IslamHub Pages still works."
  exit 0
fi

mkdir -p "$WORK_DIR/.well-known"
cp "$ROOT_DIR/docs/.well-known/assetlinks-root.json" "$WORK_DIR/.well-known/assetlinks.json"
cd "$WORK_DIR"

if git diff --quiet && git diff --cached --quiet; then
  echo "User-site assetlinks already up to date."
  exit 0
fi

git add -A
git commit -m "Publish Android App Links verification file"
git push origin main

echo ""
echo "User-site assetlinks deployed:"
echo "https://${GITHUB_USER}.github.io/.well-known/assetlinks.json"
