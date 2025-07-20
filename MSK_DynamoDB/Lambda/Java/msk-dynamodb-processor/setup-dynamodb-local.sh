#!/bin/bash

# Start DynamoDB Local using Docker Compose
echo "Starting DynamoDB Local..."
docker-compose up -d

# Wait for DynamoDB Local to start
echo "Waiting for DynamoDB Local to start..."
sleep 5

# Create the table
echo "Creating PersonData table in DynamoDB Local..."
aws dynamodb create-table \
    --table-name PersonData \
    --attribute-definitions AttributeName=id,AttributeType=S \
    --key-schema AttributeName=id,KeyType=HASH \
    --billing-mode PAY_PER_REQUEST \
    --endpoint-url http://localhost:8000 \
    --region us-east-1

if [ $? -ne 0 ]; then
    echo "Failed to create table. Please check if DynamoDB Local is running."
    exit 1
fi

echo "Table created successfully!"
echo "You can now run 'test-local.sh' to test the Lambda function with DynamoDB Local."
