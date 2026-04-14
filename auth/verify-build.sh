#!/bin/bash
# EventMate Auth Service - Build and Verification Script (Linux/Mac)
# This script verifies the project structure and builds the application

echo "===================================="
echo "EventMate Auth Service Verification"
echo "===================================="
echo ""

echo "[1/5] Checking Java version..."
java -version
if [ $? -ne 0 ]; then
    echo "ERROR: Java not found or not in PATH"
    exit 1
fi
echo ""

echo "[2/5] Checking project structure..."
if [ ! -f "src/main/java/com/eventmate/auth/AuthApplication.java" ]; then
    echo "ERROR: AuthApplication.java not found"
    exit 1
fi
if [ ! -f "build.gradle" ]; then
    echo "ERROR: build.gradle not found"
    exit 1
fi
if [ ! -f "src/main/resources/application.yml" ]; then
    echo "ERROR: application.yml not found"
    exit 1
fi
echo "Project structure: OK"
echo ""

echo "[3/5] Listing all Java source files..."
find src/main/java -name "*.java" | wc -l
echo "source files found."
echo ""

echo "[4/5] Building the project (without tests)..."
./gradlew clean build -x test --no-daemon
if [ $? -ne 0 ]; then
    echo "ERROR: Build failed"
    exit 1
fi
echo "Build: SUCCESS"
echo ""

echo "[5/5] Checking build artifacts..."
if ls build/libs/auth-*.jar 1> /dev/null 2>&1; then
    echo "JAR file created successfully:"
    ls -lh build/libs/*.jar
else
    echo "WARNING: JAR file not found in build/libs"
fi
echo ""

echo "===================================="
echo "Verification Complete!"
echo "===================================="
echo ""
echo "Next steps:"
echo "1. Setup PostgreSQL database"
echo "2. Run: psql -U postgres -d eventmate-auth -f database-setup.sql"
echo "3. Set environment variables (optional):"
echo "   export DB_PASSWORD=your_password"
echo "   export JWT_SECRET=your_secret"
echo "4. Run the application:"
echo "   ./gradlew bootRun"
echo ""
echo "For more information, see:"
echo "- README.md (complete documentation)"
echo "- QUICKSTART.md (getting started guide)"
echo "- IMPLEMENTATION-SUMMARY.md (what was built)"
echo ""

