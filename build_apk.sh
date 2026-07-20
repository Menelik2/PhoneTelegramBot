#!/bin/bash
# Helper script to build a signed release APK

echo "Building release APK..."
gradle :app:assembleRelease

echo "Build complete. You can find your APK at:"
echo "app/build/outputs/apk/release/app-release.apk"
