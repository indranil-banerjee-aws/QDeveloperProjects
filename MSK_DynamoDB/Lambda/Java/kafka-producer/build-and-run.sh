#!/bin/bash

# Build the application
echo "Building the Kafka Producer application..."
mvn clean package

if [ $? -ne 0 ]; then
    echo "Build failed. Please check the errors above."
    exit 1
fi

echo "Build successful!"

# Check if we have the required arguments
if [ "$#" -lt 2 ]; then
    echo "Usage: $0 <topic-name> <message-count> [batch-id]"
    echo "Example: $0 my-topic 100 batch-001"
    exit 1
fi

TOPIC=$1
COUNT=$2
BATCH_ID=${3:-$(uuidgen)}

# Check if properties file exists
PROPERTIES_FILE="kafka-client.properties"
if [ ! -f "$PROPERTIES_FILE" ]; then
    echo "Error: Properties file $PROPERTIES_FILE not found."
    echo "Please create this file with your Kafka configuration."
    exit 1
fi

# Run the application
echo "Running Kafka Producer with:"
echo "  Topic: $TOPIC"
echo "  Count: $COUNT"
echo "  Batch ID: $BATCH_ID"
echo ""

java -jar target/kafka-producer-1.0-SNAPSHOT-jar-with-dependencies.jar \
  --properties "$PROPERTIES_FILE" \
  --topic "$TOPIC" \
  --batch-id "$BATCH_ID" \
  --count "$COUNT"
