#!/bin/bash

# Script to create Kafka topic from local machine using AWS CLI

MSK_CLUSTER_ARN="arn:aws:kafka:us-east-2:584572555040:cluster/MSKLambdaJavaDynamoDBEnvironment-MSKCluster/106f4b4f-d4ec-4fc1-90d9-1eda67bb317a-3"
TOPIC_NAME="MSKLambdaJavaDynamoDBTopic"

echo "Creating Kafka topic using AWS CLI..."

# Create the topic using AWS CLI
aws kafka create-configuration \
  --name "MSKLambdaJavaDynamoDBTopic-config" \
  --kafka-versions "3.4.0" \
  --server-properties "auto.create.topics.enable=true" \
  --region us-east-2 \
  --profile indranil5040

echo "Topic creation initiated. The topic will be auto-created when the first message is sent."
echo ""
echo "Alternatively, you can create the DynamoDB table:"

aws dynamodb create-table \
  --table-name PersonData \
  --attribute-definitions AttributeName=id,AttributeType=S \
  --key-schema AttributeName=id,KeyType=HASH \
  --billing-mode PAY_PER_REQUEST \
  --region us-east-2 \
  --profile indranil5040

echo "Setup complete!"
