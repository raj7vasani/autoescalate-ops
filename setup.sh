#!/bin/bash

# AutoEscalate Ops Setup Script
# This script sets up and runs the application

set -e  # Exit on error

echo "=================================="
echo "AutoEscalate Ops - Setup Script"
echo "=================================="
echo ""

# Check Java version
echo "Checking Java version..."
if ! command -v java &> /dev/null; then
    echo "❌ Java is not installed. Please install Java 21 or higher."
    exit 1
fi

JAVA_VERSION=$(java -version 2>&1 | head -n 1 | cut -d'"' -f2 | cut -d'.' -f1)
if [ "$JAVA_VERSION" -lt 21 ]; then
    echo "❌ Java version is $JAVA_VERSION. Please install Java 21 or higher."
    exit 1
fi
echo "✅ Java $JAVA_VERSION detected"
echo ""

# Check Maven
echo "Checking Maven..."
if ! command -v mvn &> /dev/null; then
    echo "❌ Maven is not installed. Please install Maven 3.9 or higher."
    exit 1
fi
echo "✅ Maven detected"
echo ""

# Check Node.js (optional, for CAP tools)
echo "Checking Node.js..."
if ! command -v node &> /dev/null; then
    echo "⚠️  Node.js is not installed. Some features may not work."
else
    echo "✅ Node.js detected"
fi
echo ""

# Install npm dependencies (if needed)
if [ -f "package.json" ]; then
    echo "Installing npm dependencies..."
    npm install
    echo "✅ npm dependencies installed"
    echo ""
fi

# Build the backend
echo "Building backend application..."
cd srv
mvn clean install -DskipTests
echo "✅ Backend built successfully"
echo ""

# Start the application
echo "=================================="
echo "Starting AutoEscalate Ops..."
echo "=================================="
echo ""
echo "Frontend will be available at:"
echo "  http://localhost:8080/issuetracker/webapp/index.html"
echo ""
echo "API will be available at:"
echo "  http://localhost:8080/api/issues"
echo ""
echo "Login credentials:"
echo "  Username: authenticated"
echo "  Password: (leave blank)"
echo ""
echo "Press Ctrl+C to stop the application"
echo ""

# Run the application
mvn spring-boot:run
