#!/bin/bash

# Get MSK cluster ARN from CloudFormation
echo "Getting MSK cluster ARN from CloudFormation..."
MSK_CLUSTER_ARN=$(aws cloudformation describe-stacks --stack-name msk-lambda-java-dynamodb-stack --region us-east-2 --profile indranil5040 --query "Stacks[0].Outputs[?OutputKey=='MSKClusterArn'].OutputValue" --output text)

if [ -z "$MSK_CLUSTER_ARN" ]; then
  echo "ERROR: Failed to get MSK cluster ARN from CloudFormation"
  exit 1
fi

echo "MSK Cluster ARN: $MSK_CLUSTER_ARN"

# Get bootstrap servers for the MSK cluster
echo "Getting bootstrap servers for the MSK cluster..."
BOOTSTRAP_SERVERS=$(aws kafka get-bootstrap-brokers --cluster-arn "$MSK_CLUSTER_ARN" --region us-east-2 --profile indranil5040 --query "BootstrapBrokerStringSaslIam" --output text)

if [ -z "$BOOTSTRAP_SERVERS" ]; then
  echo "ERROR: Failed to get bootstrap servers for the MSK cluster"
  exit 1
fi

echo "Bootstrap Servers: $BOOTSTRAP_SERVERS"

# Update the Kafka client properties file with the correct bootstrap servers
echo "Updating Kafka client properties file..."
sed -i "s|bootstrap.servers=.*|bootstrap.servers=$BOOTSTRAP_SERVERS|g" /home/ec2-user/project/MSK_DynamoDB/Lambda/Java/kafka-producer/kafka-client.properties

# Create the Kafka topic
echo "Creating Kafka topic: MSKLambdaJavaDynamoDBTopic..."
cd /home/ec2-user/kafka_2.13-2.8.1
export CLASSPATH=/home/ec2-user/msk-iam-auth/aws-msk-iam-auth-1.1.5-all.jar

# Create the client.properties file for IAM authentication
cat > /home/ec2-user/client.properties << EOF
security.protocol=SASL_SSL
sasl.mechanism=AWS_MSK_IAM
sasl.jaas.config=software.amazon.msk.auth.iam.IAMLoginModule required;
sasl.client.callback.handler.class=software.amazon.msk.auth.iam.IAMClientCallbackHandler
EOF

# Create the Kafka topic
bin/kafka-topics.sh --create \
  --bootstrap-server $BOOTSTRAP_SERVERS \
  --command-config /home/ec2-user/client.properties \
  --replication-factor 2 \
  --partitions 3 \
  --topic MSKLambdaJavaDynamoDBTopic

# Create DynamoDB table if it doesn't exist
echo "Creating DynamoDB table: PersonData..."
aws dynamodb create-table \
  --table-name PersonData \
  --attribute-definitions AttributeName=id,AttributeType=S \
  --key-schema AttributeName=id,KeyType=HASH \
  --billing-mode PAY_PER_REQUEST \
  --region us-east-2 \
  --profile indranil5040

echo "Setup complete!"
echo ""
echo "To run the Kafka producer:"
echo "  cd ~/project/MSK_DynamoDB/Lambda/Java/kafka-producer"
echo "  ./run.sh MSKLambdaJavaDynamoDBTopic 100 batch-001"
echo ""
echo "To deploy the Lambda function:"
echo "  cd ~/project/MSK_DynamoDB/Lambda/Java/msk-dynamodb-processor"
echo "  sam deploy --guided"
