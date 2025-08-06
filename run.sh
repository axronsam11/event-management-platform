#!/bin/bash

# Shell script to run the Event Management API locally

# Make the script exit on any error
set -e

# Check if Java is installed
if ! command -v java &> /dev/null; then
    echo "Java is not installed or not in PATH. Please install Java 17 or higher."
    exit 1
fi

# Check if Maven is installed
if ! command -v mvn &> /dev/null; then
    echo "Maven is not installed or not in PATH. Please install Maven."
    exit 1
fi

# Build the project
echo "Building the project..."
mvn clean package -DskipTests

# Run the application
echo "Starting the application..."
java -jar target/event-management-api-0.0.1-SNAPSHOT.jar