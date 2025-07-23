#!/bin/bash

# Run script for Kafka Producer
# This script runs the compiled JAR file

# Set default values
TOPIC=${1:-person-data}
COUNT=${2:-10}
BATCH_ID=${3:-batch-$(date +%s)}

echo "Running Kafka Producer..."
echo "========================"
echo "Topic: $TOPIC"
echo "Message Count: $COUNT"
echo "Batch ID: $BATCH_ID"
echo ""

# Check if JAR file exists
JAR_FILE="target/kafka-producer-1.0-SNAPSHOT-jar-with-dependencies.jar"
if [ ! -f "$JAR_FILE" ]; then
    echo "Error: JAR file not found: $JAR_FILE"
    echo "Please run ./build.sh first to compile the project."
    exit 1
fi

# Check if properties file exists
PROPERTIES_FILE="kafka-client.properties"
if [ ! -f "$PROPERTIES_FILE" ]; then
    echo "Error: Properties file not found: $PROPERTIES_FILE"
    echo "Please ensure kafka-client.properties exists in the current directory."
    exit 1
fi

# Run the producer
java -jar $JAR_FILE \
  --properties $PROPERTIES_FILE \
  --topic $TOPIC \
  --count $COUNT \
  --batch-id $BATCH_ID

# Check if execution was successful
if [ $? -eq 0 ]; then
    echo ""
    echo "Kafka Producer completed successfully!"
else
    echo ""
    echo "Kafka Producer failed! Please check the error messages above."
    exit 1
fi
