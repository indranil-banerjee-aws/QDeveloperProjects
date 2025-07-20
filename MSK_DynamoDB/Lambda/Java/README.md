# MSK to DynamoDB End-to-End Project

This project demonstrates an end-to-end serverless data processing pipeline using Amazon MSK (Managed Streaming for Apache Kafka) and AWS Lambda to process data and store it in Amazon DynamoDB.

## Architecture

![Architecture Diagram](https://via.placeholder.com/800x400?text=MSK+to+DynamoDB+Architecture)

The solution consists of:

1. **Infrastructure Layer**:
   - Amazon VPC with public and private subnets
   - Amazon MSK cluster (Provisioned or Serverless)
   - EC2 instance with development tools

2. **Producer Layer**:
   - Java-based Kafka producer application
   - Generates fake person data using JavaFaker
   - Sends JSON messages to MSK

3. **Processing Layer**:
   - AWS Lambda function triggered by MSK events
   - Processes Kafka messages
   - Stores data in DynamoDB

## Repository Structure

```
/
├── msk-environment-template.yaml    # CloudFormation template for infrastructure
├── kafka-producer/                  # Kafka producer application
│   ├── pom.xml                      # Maven project file
│   ├── src/                         # Source code
│   └── README.md                    # Producer documentation
└── msk-dynamodb-processor/          # Lambda function (SAM project)
    ├── template.yaml                # SAM template
    ├── src/                         # Source code
    └── README.md                    # Lambda documentation
```

## Prerequisites

- AWS Account
- AWS CLI installed and configured
- Git client
- Java 11 or higher (for local development)
- Maven (for local development)

## Deployment Instructions

### 1. Deploy Infrastructure

1. Clone this repository:
   ```bash
   git clone https://github.com/yourusername/msk-dynamodb-project.git
   cd msk-dynamodb-project
   ```

2. Deploy the CloudFormation template:
   ```bash
   aws cloudformation create-stack \
     --stack-name msk-dynamodb-infrastructure \
     --template-body file://msk-environment-template.yaml \
     --parameters \
       ParameterKey=EnvironmentName,ParameterValue=MSKEnvironment \
       ParameterKey=KafkaTopicName,ParameterValue=person-data \
       ParameterKey=MSKClusterType,ParameterValue=Provisioned \
       ParameterKey=GitHubRepoURL,ParameterValue=https://github.com/yourusername/msk-dynamodb-project.git \
     --capabilities CAPABILITY_IAM
   ```

3. Wait for the stack to complete:
   ```bash
   aws cloudformation wait stack-create-complete --stack-name msk-dynamodb-infrastructure
   ```

4. Get the EC2 instance public IP:
   ```bash
   aws cloudformation describe-stacks \
     --stack-name msk-dynamodb-infrastructure \
     --query "Stacks[0].Outputs[?OutputKey=='EC2PublicIP'].OutputValue" \
     --output text
   ```

### 2. Connect to EC2 Instance

1. Connect to the EC2 instance using EC2 Instance Connect:
   ```bash
   aws ec2-instance-connect ssh --instance-id $(aws cloudformation describe-stacks \
     --stack-name msk-dynamodb-infrastructure \
     --query "Stacks[0].Outputs[?OutputKey=='EC2InstanceId'].OutputValue" \
     --output text)
   ```

   Or use the AWS Console: EC2 > Instances > Select your instance > Connect > EC2 Instance Connect

2. Run the setup script:
   ```bash
   ./setup-project.sh
   ```

### 3. Deploy the Lambda Function

1. On the EC2 instance, navigate to the SAM project:
   ```bash
   cd ~/project/msk-dynamodb-processor
   ```

2. Deploy the SAM application:
   ```bash
   sam deploy --guided
   ```

   The parameters will be pre-filled from the CloudFormation outputs.

### 4. Generate and Send Kafka Messages

1. On the EC2 instance, navigate to the Kafka producer:
   ```bash
   cd ~/project/kafka-producer
   ```

2. Run the producer to send messages:
   ```bash
   ./build-and-run.sh person-data 100 batch-001
   ```

   This will send 100 messages to the "person-data" topic with batch ID "batch-001".

### 5. Verify Data in DynamoDB

1. Check that the Lambda function processed the messages:
   ```bash
   aws logs describe-log-streams \
     --log-group-name /aws/lambda/msk-dynamodb-processor-MSKProcessorFunction-XXXX
   ```

2. Query the DynamoDB table:
   ```bash
   aws dynamodb scan --table-name PersonData --limit 10
   ```

## Local Development

### Kafka Producer

1. Clone the repository:
   ```bash
   git clone https://github.com/yourusername/msk-dynamodb-project.git
   cd msk-dynamodb-project/kafka-producer
   ```

2. Build the application:
   ```bash
   mvn clean package
   ```

3. Update the `kafka-client.properties` file with your MSK bootstrap servers.

4. Run the producer:
   ```bash
   java -jar target/kafka-producer-1.0-SNAPSHOT-jar-with-dependencies.jar \
     --properties kafka-client.properties \
     --topic your-topic-name \
     --batch-id your-batch-id \
     --count 10
   ```

### Lambda Function

1. Navigate to the SAM project:
   ```bash
   cd msk-dynamodb-project/msk-dynamodb-processor
   ```

2. Run unit tests:
   ```bash
   mvn test
   ```

3. Test locally with DynamoDB Local:
   ```bash
   ./setup-dynamodb-local.sh
   ./test-local.sh
   ```

## Cleanup

1. Delete the SAM application:
   ```bash
   cd ~/project/msk-dynamodb-processor
   sam delete
   ```

2. Delete the CloudFormation stack:
   ```bash
   aws cloudformation delete-stack --stack-name msk-dynamodb-infrastructure
   ```

## Security Considerations

- The EC2 instance has an IAM role with permissions to deploy the SAM application
- MSK is configured with IAM authentication
- The Lambda function has least-privilege permissions to access DynamoDB
- All connections to MSK use TLS encryption

## Troubleshooting

- **Kafka Producer Issues**: Check the Kafka client properties and ensure the bootstrap servers are correct
- **Lambda Function Issues**: Check CloudWatch Logs for error messages
- **MSK Connectivity**: Verify security groups allow traffic between EC2 and MSK
- **DynamoDB Permissions**: Ensure the Lambda execution role has permissions to access DynamoDB

## License

This project is licensed under the MIT License - see the LICENSE file for details.
