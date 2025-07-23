#!/bin/bash -x

# This script sets up the MSK to DynamoDB environment on an EC2 instance
# Parameter values are substituted directly by CloudFormation

# Set parameter values (these will be substituted by CloudFormation)
STACK_NAME="STACK_NAME_PLACEHOLDER"
KAFKA_TOPIC_NAME="KAFKA_TOPIC_NAME_PLACEHOLDER"
GITHUB_REPO_URL="GITHUB_REPO_URL_PLACEHOLDER"
AWS_REGION="AWS_REGION_PLACEHOLDER"
GITHUB_PROJECT_PATH="GITHUB_PROJECT_PATH_PLACEHOLDER"

# Function to log messages with timestamp
log_message() {
    echo "[$(date '+%Y-%m-%d %H:%M:%S')] $1"
}

# Function to handle errors but continue execution
handle_error() {
    local exit_code=$1
    local error_message="$2"
    if [ $exit_code -ne 0 ]; then
        log_message "ERROR: $error_message (Exit code: $exit_code)"
        log_message "Continuing with the rest of the setup..."
        return 1
    fi
    return 0
}

log_message "Starting MSK to DynamoDB environment setup..."

# Set environment variables
log_message "Getting MSK cluster ARN from CloudFormation stack..."
MSK_CLUSTER_ARN=$(aws cloudformation describe-stacks --stack-name $STACK_NAME --query "Stacks[0].Outputs[?OutputKey=='MSKClusterArn'].OutputValue" --output text 2>/dev/null)
if ! handle_error $? "Failed to get MSK cluster ARN from CloudFormation"; then
    log_message "Attempting to continue without MSK cluster ARN..."
fi

# Get bootstrap servers
if [ ! -z "$MSK_CLUSTER_ARN" ]; then
    log_message "Getting bootstrap servers from MSK cluster..."
    BOOTSTRAP_SERVERS=$(aws kafka get-bootstrap-brokers --cluster-arn $MSK_CLUSTER_ARN --query 'BootstrapBrokerStringSaslIam' --output text 2>/dev/null)
    if ! handle_error $? "Failed to get bootstrap servers"; then
        log_message "Will attempt to configure without bootstrap servers for now..."
        BOOTSTRAP_SERVERS="localhost:9092"
    fi
else
    log_message "MSK cluster ARN not available, using placeholder bootstrap servers"
    BOOTSTRAP_SERVERS="localhost:9092"
fi

log_message "Bootstrap servers: $BOOTSTRAP_SERVERS"

# Install Kafka client tools
log_message "Installing Kafka client tools..."
cd /home/ec2-user
KAFKA_VERSION="2.8.1"
SCALA_VERSION="2.13"
KAFKA_DIR="kafka_${SCALA_VERSION}-${KAFKA_VERSION}"
KAFKA_TAR="${KAFKA_DIR}.tgz"

# Download Kafka if not already present
if [ ! -d "$KAFKA_DIR" ]; then
    log_message "Downloading Kafka client tools..."
    wget -q "https://archive.apache.org/dist/kafka/${KAFKA_VERSION}/${KAFKA_TAR}" 2>/dev/null
    if ! handle_error $? "Failed to download Kafka client tools"; then
        log_message "Trying alternative download method..."
        curl -s -O "https://archive.apache.org/dist/kafka/${KAFKA_VERSION}/${KAFKA_TAR}"
        handle_error $? "Failed to download Kafka with curl as well"
    fi
    
    if [ -f "$KAFKA_TAR" ]; then
        log_message "Extracting Kafka client tools..."
        tar -xzf "$KAFKA_TAR" 2>/dev/null
        handle_error $? "Failed to extract Kafka client tools"
        rm -f "$KAFKA_TAR"
    fi
fi

# Download MSK IAM authentication library
log_message "Setting up MSK IAM authentication..."
MSK_IAM_AUTH_DIR="/home/ec2-user/msk-iam-auth"
mkdir -p "$MSK_IAM_AUTH_DIR"
cd "$MSK_IAM_AUTH_DIR"

if [ ! -f "aws-msk-iam-auth-1.1.5-all.jar" ]; then
    log_message "Downloading MSK IAM authentication JAR..."
    wget -q "https://github.com/aws/aws-msk-iam-auth/releases/download/v1.1.5/aws-msk-iam-auth-1.1.5-all.jar" 2>/dev/null
    if ! handle_error $? "Failed to download MSK IAM auth JAR with wget"; then
        curl -s -L -O "https://github.com/aws/aws-msk-iam-auth/releases/download/v1.1.5/aws-msk-iam-auth-1.1.5-all.jar"
        handle_error $? "Failed to download MSK IAM auth JAR with curl"
    fi
fi

# Set up Kafka client configuration for IAM authentication
log_message "Configuring Kafka client for IAM authentication..."
cd /home/ec2-user
if [ -d "$KAFKA_DIR" ]; then
    # Add MSK IAM auth JAR to Kafka classpath
    export CLASSPATH="$MSK_IAM_AUTH_DIR/aws-msk-iam-auth-1.1.5-all.jar"
    
    # Create Kafka topic using Kafka client tools
    if [ ! -z "$MSK_CLUSTER_ARN" ] && [ "$BOOTSTRAP_SERVERS" != "localhost:9092" ]; then
        log_message "Creating Kafka topic: $KAFKA_TOPIC_NAME"
        
        # Create a temporary client properties file for topic creation
        TEMP_CLIENT_PROPS="/tmp/kafka-client-temp.properties"
        cat > "$TEMP_CLIENT_PROPS" << EOF
bootstrap.servers=$BOOTSTRAP_SERVERS
security.protocol=SASL_SSL
sasl.mechanism=AWS_MSK_IAM
sasl.jaas.config=software.amazon.msk.auth.iam.IAMLoginModule required;
sasl.client.callback.handler.class=software.amazon.msk.auth.iam.IAMClientCallbackHandler
EOF
        
        # Create the topic
        ./${KAFKA_DIR}/bin/kafka-topics.sh --create \
            --bootstrap-server "$BOOTSTRAP_SERVERS" \
            --command-config "$TEMP_CLIENT_PROPS" \
            --topic "$KAFKA_TOPIC_NAME" \
            --partitions 3 \
            --replication-factor 2 2>/dev/null
        
        if handle_error $? "Failed to create Kafka topic"; then
            log_message "Successfully created Kafka topic: $KAFKA_TOPIC_NAME"
            
            # Verify topic creation
            log_message "Verifying topic creation..."
            ./${KAFKA_DIR}/bin/kafka-topics.sh --list \
                --bootstrap-server "$BOOTSTRAP_SERVERS" \
                --command-config "$TEMP_CLIENT_PROPS" 2>/dev/null | grep -q "$KAFKA_TOPIC_NAME"
            
            if handle_error $? "Failed to verify topic creation"; then
                log_message "Topic verification successful"
            fi
        fi
        
        # Clean up temporary file
        rm -f "$TEMP_CLIENT_PROPS"
    else
        log_message "Skipping Kafka topic creation - MSK cluster not ready or bootstrap servers not available"
    fi
else
    log_message "Kafka client tools not available - skipping topic creation"
fi

# Create DynamoDB table
log_message "Creating DynamoDB table: PersonData"
aws dynamodb create-table \
  --table-name PersonData \
  --attribute-definitions AttributeName=id,AttributeType=S \
  --key-schema AttributeName=id,KeyType=HASH \
  --billing-mode PAY_PER_REQUEST \
  --region "$AWS_REGION" 2>/dev/null

if handle_error $? "Failed to create DynamoDB table"; then
    log_message "Successfully created DynamoDB table: PersonData"
else
    # Check if table already exists
    aws dynamodb describe-table --table-name PersonData --region "$AWS_REGION" >/dev/null 2>&1
    if [ $? -eq 0 ]; then
        log_message "DynamoDB table PersonData already exists"
    fi
fi

# Create project directory
log_message "Setting up project directory..."
mkdir -p /home/ec2-user/project
cd /home/ec2-user/project

# Clone repository
log_message "Cloning repository: $GITHUB_REPO_URL"
if [ -d ".git" ]; then
    log_message "Repository already cloned, pulling latest changes..."
    git pull 2>/dev/null
    handle_error $? "Failed to pull latest changes"
else
    git clone "$GITHUB_REPO_URL" . 2>/dev/null
    if ! handle_error $? "Failed to clone repository"; then
        log_message "Successfully cloned repository"
    fi
fi

# Navigate to the correct project path
if [ -d "$GITHUB_PROJECT_PATH" ]; then
    cd "$GITHUB_PROJECT_PATH"
    log_message "Navigated to project path: $GITHUB_PROJECT_PATH"
else
    log_message "WARNING: Project path $GITHUB_PROJECT_PATH not found, staying in current directory"
fi

# Create separate build script for Kafka producer
log_message "Creating build script for Kafka producer..."
mkdir -p /home/ec2-user/project/$GITHUB_PROJECT_PATH/kafka-producer
cat > /home/ec2-user/project/$GITHUB_PROJECT_PATH/kafka-producer/build.sh << 'EOF'
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
EOF

chmod +x /home/ec2-user/project/$GITHUB_PROJECT_PATH/kafka-producer/build.sh

# Create separate run script for Kafka producer
log_message "Creating run script for Kafka producer..."
cat > /home/ec2-user/project/$GITHUB_PROJECT_PATH/kafka-producer/run.sh << 'EOF'
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
EOF

chmod +x /home/ec2-user/project/$GITHUB_PROJECT_PATH/kafka-producer/run.sh

# Create SAM config file
log_message "Creating SAM configuration file..."
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
log_message "Creating Kafka producer properties file..."
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
log_message "Creating user setup script..."
cat > /home/ec2-user/setup-project.sh << EOF
#!/bin/bash

cd ~/project/$GITHUB_PROJECT_PATH/kafka-producer

echo "Building Kafka Producer..."
./build.sh

echo ""
echo "Project setup complete!"
echo ""
echo "Available commands:"
echo "  To build the Kafka producer:"
echo "    cd ~/project/$GITHUB_PROJECT_PATH/kafka-producer"
echo "    ./build.sh"
echo ""
echo "  To run the Kafka producer:"
echo "    cd ~/project/$GITHUB_PROJECT_PATH/kafka-producer"
echo "    ./run.sh [$KAFKA_TOPIC_NAME] [message-count] [batch-id]"
echo ""
echo "  Example:"
echo "    ./run.sh $KAFKA_TOPIC_NAME 100 batch-001"
echo ""
echo "  To deploy the Lambda function:"
echo "    cd ~/project/$GITHUB_PROJECT_PATH/msk-dynamodb-processor"
echo "    sam deploy --guided"
echo ""
echo "Kafka client tools are installed in: /home/ec2-user/$KAFKA_DIR"
echo "MSK IAM auth JAR is available in: /home/ec2-user/msk-iam-auth/"
EOF

chmod +x /home/ec2-user/setup-project.sh

# Set ownership
log_message "Setting file ownership..."
chown -R ec2-user:ec2-user /home/ec2-user/project 2>/dev/null
handle_error $? "Failed to set ownership for project directory"

chown -R ec2-user:ec2-user /home/ec2-user/$KAFKA_DIR 2>/dev/null
handle_error $? "Failed to set ownership for Kafka directory"

chown -R ec2-user:ec2-user /home/ec2-user/msk-iam-auth 2>/dev/null
handle_error $? "Failed to set ownership for MSK IAM auth directory"

chown ec2-user:ec2-user /home/ec2-user/setup-project.sh 2>/dev/null
handle_error $? "Failed to set ownership for setup script"

log_message "Setup script completed successfully!"
log_message "Summary:"
log_message "- MSK Cluster ARN: $MSK_CLUSTER_ARN"
log_message "- Kafka Topic: $KAFKA_TOPIC_NAME"
log_message "- Bootstrap Servers: $BOOTSTRAP_SERVERS"
log_message "- DynamoDB Table: PersonData"
log_message "- Kafka Client Tools: /home/ec2-user/$KAFKA_DIR"
log_message "- Project Path: /home/ec2-user/project/$GITHUB_PROJECT_PATH"
