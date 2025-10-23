#!/bin/bash

# Secure Messenger Server Runner
# This script starts the Python server for the Secure Messenger

echo "Starting Secure Messenger Server..."
echo "Make sure you have Python 3 installed."
echo ""

# Check if Python 3 is available
if command -v python3 &> /dev/null; then
    echo "Found Python 3. Starting server..."
    echo "Server will listen on all interfaces, port 8080"
    echo "Press Ctrl+C to stop the server"
    echo ""
    python3 server.py
elif command -v python &> /dev/null; then
    echo "Found Python. Checking version..."
    PYTHON_VERSION=$(python --version 2>&1 | cut -d' ' -f2 | cut -d'.' -f1)
    if [ "$PYTHON_VERSION" -ge "3" ]; then
        echo "Python 3+ found. Starting server..."
        echo "Server will listen on all interfaces, port 8080"
        echo "Press Ctrl+C to stop the server"
        echo ""
        python server.py
    else
        echo "Python version is less than 3. Please install Python 3."
        exit 1
    fi
else
    echo "Python not found. Please install Python 3."
    exit 1
fi