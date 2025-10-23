#!/bin/bash

# Secure Messenger Build Script for macOS
# This script helps build and install the Android app

echo "Secure Messenger Build Script"
echo "============================="
echo ""

# Check if we're in the right directory
if [ ! -f "build.gradle" ]; then
    echo "Error: build.gradle not found. Please run this script from the project root directory."
    exit 1
fi

# Check if Gradle wrapper exists and is executable
if [ ! -x "gradlew" ]; then
    echo "Making gradlew executable..."
    chmod +x gradlew
fi

echo "Building Secure Messenger..."
echo "This may take a few minutes on the first run."
echo ""

# Clean previous builds
echo "Cleaning previous builds..."
./gradlew clean

# Build the app
echo "Building the application..."
./gradlew build

if [ $? -eq 0 ]; then
    echo ""
    echo "Build successful!"
    echo ""
    echo "To install on a connected device, run:"
    echo "  ./gradlew installDebug"
    echo ""
    echo "To assemble the APK, run:"
    echo "  ./gradlew assembleDebug"
    echo "  The APK will be located at app/build/outputs/apk/debug/app-debug.apk"
else
    echo ""
    echo "Build failed. Please check the error messages above."
    exit 1
fi