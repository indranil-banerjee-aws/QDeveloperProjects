#!/bin/bash -xe

# This script sets up the MSK to DynamoDB environment on an EC2 instance
# Parameter values are substituted directly by CloudFormation

# Set parameter values (these will be substituted by CloudFormation)
STACK_NAME="STACK_NAME_PLACEHOLDER"
KAFKA_TOPIC_NAME="KAFKA_TOPIC_NAME_PLACEHOLDER"
GITHUB_REPO_URL="GITHUB_REPO_URL_PLACEHOLDER"
AWS_REGION="AWS_REGION_PLACEHOLDER"
GITHUB_PROJECT_PATH="GITHUB_PROJECT_PATH_PLACEHOLDER"

# Set environment variables
MSK_CLUSTER_ARN=$(aws cloudformation describe-stacks --stack-name $STACK_NAME --query "Stacks[0].Outputs[?OutputKey=='MSKClusterArn'].OutputValue" --output text)

# Get bootstrap servers
BOOTSTRAP_SERVERS=$(aws kafka get-bootstrap-brokers --cluster-arn $MSK_CLUSTER_ARN --query 'BootstrapBrokerStringSaslIam' --output text)

# Create Kafka topic
aws kafka create-topic --cluster-arn $MSK_CLUSTER_ARN --topic-name $KAFKA_TOPIC_NAME --number-of-partitions 3 --replication-factor 2

# Create DynamoDB table
aws dynamodb create-table \
  --table-name PersonData \
  --attribute-definitions AttributeName=id,AttributeType=S \
  --key-schema AttributeName=id,KeyType=HASH \
  --billing-mode PAY_PER_REQUEST

# Create project directory
mkdir -p /home/ec2-user/project
cd /home/ec2-user/project

# Clone repository
git clone $GITHUB_REPO_URL .

# Navigate to the correct project path
cd $GITHUB_PROJECT_PATH

# Create build and run script for Kafka producer
cat > /home/ec2-user/project/$GITHUB_PROJECT_PATH/kafka-producer/build-and-run.sh << 'EOF'
#!/bin/bash

TOPIC=${1:-person-data}
COUNT=${2:-10}
BATCH_ID=${3:-batch-$(date +%s)}

mvn clean package

java -jar target/kafka-producer-1.0-SNAPSHOT-jar-with-dependencies.jar \
  --properties kafka-client.properties \
  --topic $TOPIC \
  --count $COUNT \
  --batch-id $BATCH_ID
EOF

chmod +x /home/ec2-user/project/$GITHUB_PROJECT_PATH/kafka-producer/build-and-run.sh

# Create SAM config file
mkdir -p /home/ec2-user/project/$GITHUB_PROJECT_PATH/msk-dynamodb-processor
cat > /home/ec2-user/project/$GITHUB_PROJECT_PATH/msk-dynamodb-processor/samconfig.toml << EOF
version = 0.1
[default]
[default.deploy]
[default.deploy.parameters]
stack_name = "msk-dynamodb-processor"
s3_bucket = "aws-sam-cli-managed-default-samclisourcebucket-EXAMPLE"
s3_prefix = "msk-dynamodb-processor"
region = "$AWS_REGION"
confirm_changeset = true
capabilities = "CAPABILITY_IAM"
parameter_overrides = "MSKClusterArn=\"$MSK_CLUSTER_ARN\" KafkaTopicName=\"$KAFKA_TOPIC_NAME\" Environment=\"dev\" DynamoDBTableName=\"PersonData\""
image_repositories = []
EOF

# Update Kafka producer properties file
cat > /home/ec2-user/project/$GITHUB_PROJECT_PATH/kafka-producer/kafka-client.properties << EOF
# Kafka Producer Configuration

# Bootstrap servers
bootstrap.servers=$BOOTSTRAP_SERVERS

# MSK IAM Authentication
security.protocol=SASL_SSL
sasl.mechanism=AWS_MSK_IAM
sasl.jaas.config=software.amazon.msk.auth.iam.IAMLoginModule required;
sasl.client.callback.handler.class=software.amazon.msk.auth.iam.IAMClientCallbackHandler

# Producer specific settings
key.serializer=org.apache.kafka.common.serialization.StringSerializer
value.serializer=org.apache.kafka.common.serialization.StringSerializer
EOF

# Create setup script
cat > /home/ec2-user/setup-project.sh << EOF
#!/bin/bash

cd ~/project/$GITHUB_PROJECT_PATH/kafka-producer
mvn clean package

echo "Project setup complete!"
echo "To run the Kafka producer:"
echo "  cd ~/project/$GITHUB_PROJECT_PATH/kafka-producer"
echo "  ./build-and-run.sh $KAFKA_TOPIC_NAME 100 batch-001"
echo ""
echo "To deploy the Lambda function:"
echo "  cd ~/project/$GITHUB_PROJECT_PATH/msk-dynamodb-processor"
echo "  sam deploy --guided"
EOF

chmod +x /home/ec2-user/setup-project.sh

# Set ownership
chown -R ec2-user:ec2-user /home/ec2-user/project
chown ec2-user:ec2-user /home/ec2-user/setup-project.sh

echo "Setup script completed successfully"
