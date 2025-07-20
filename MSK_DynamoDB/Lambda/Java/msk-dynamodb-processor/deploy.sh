#!/bin/bash

# Check if MSK Cluster ARN is provided
if [ -z "$1" ]; then
    echo "Usage: $0 <msk-cluster-arn> <kafka-topic-name> [dynamodb-table-name]"
    echo "Example: $0 arn:aws:kafka:us-east-1:123456789012:cluster/my-cluster/abcdef my-topic PersonData"
    exit 1
fi

MSK_CLUSTER_ARN=$1
KAFKA_TOPIC_NAME=$2
DYNAMODB_TABLE_NAME=${3:-"PersonData"}

# Build the SAM application
echo "Building the SAM application..."
sam build

if [ $? -ne 0 ]; then
    echo "Build failed. Please check the errors above."
    exit 1
fi

echo "Build successful!"

# Deploy the SAM application
echo "Deploying the SAM application..."
sam deploy --guided \
    --parameter-overrides \
    MSKClusterArn=$MSK_CLUSTER_ARN \
    KafkaTopicName=$KAFKA_TOPIC_NAME \
    DynamoDBTableName=$DYNAMODB_TABLE_NAME

if [ $? -ne 0 ]; then
    echo "Deployment failed. Please check the errors above."
    exit 1
fi

echo "Deployment successful!"
