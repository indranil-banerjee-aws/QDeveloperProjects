#!/bin/bash

# Build script for Kafka Producer
# This script compiles the Java source code and creates the JAR file

echo "Building Kafka Producer..."
echo "=========================="

# Clean and compile the project
mvn clean package

# Check if build was successful
if [ $? -eq 0 ]; then
    echo ""
    echo "Build completed successfully!"
    echo "JAR file created: target/kafka-producer-1.0-SNAPSHOT-jar-with-dependencies.jar"
    echo ""
    echo "To run the producer, use: ./run.sh [topic] [count] [batch-id]"
else
    echo ""
    echo "Build failed! Please check the error messages above."
    exit 1
fi
