# MSK to DynamoDB End-to-End Project

This project demonstrates an end-to-end serverless data processing pipeline using Amazon MSK (Managed Streaming for Apache Kafka) and AWS Lambda to process data and store it in Amazon DynamoDB.

## Architecture Overview

![Architecture Diagram](https://via.placeholder.com/800x400?text=MSK+to+DynamoDB+Architecture)

This solution implements a complete data pipeline with the following components:

1. **Infrastructure Layer**:
   - Amazon VPC with public and private subnets
   - Amazon MSK cluster (Provisioned or Serverless)
   - EC2 instance with development tools and Amazon Kiro Web

2. **Producer Layer**:
   - Java-based Kafka producer application
   - Generates fake person data using JavaFaker
   - Sends JSON messages to MSK

3. **Processing Layer**:
   - AWS Lambda function triggered by MSK events
   - Processes Kafka messages using AWS Lambda Powertools
   - Stores data in DynamoDB

## Quick Start

### 1. Deploy Infrastructure

```bash
aws cloudformation create-stack \
  --stack-name msk-dynamodb-infrastructure \
  --template-body file://Lambda/Java/msk-environment-template.yaml \
  --parameters \
    ParameterKey=EnvironmentName,ParameterValue=MSKEnvironment \
    ParameterKey=KafkaTopicName,ParameterValue=person-data \
    ParameterKey=MSKClusterType,ParameterValue=Provisioned \
    ParameterKey=GitHubRepoURL,ParameterValue=https://github.com/yourusername/msk-dynamodb-project.git \
  --capabilities CAPABILITY_IAM
```

### 2. Connect to EC2 Instance

Connect to the EC2 instance using EC2 Instance Connect from the AWS Console or using the AWS CLI:

```bash
aws ec2-instance-connect ssh --instance-id <instance-id>
```

### 3. Run Setup Script

On the EC2 instance, run:

```bash
./setup-project.sh
```

### 4. Deploy Lambda Function

```bash
cd ~/project/msk-dynamodb-processor
sam deploy --guided
```

### 5. Send Test Messages

```bash
cd ~/project/kafka-producer
./build-and-run.sh person-data 100 batch-001
```

### 6. Verify Data in DynamoDB

```bash
aws dynamodb scan --table-name PersonData --limit 10
```

## Repository Structure

```
/
├── Lambda/
│   └── Java/
│       ├── msk-environment-template.yaml    # CloudFormation template for infrastructure
│       ├── kafka-producer/                  # Kafka producer application
│       │   ├── pom.xml                      # Maven project file
│       │   ├── src/                         # Source code
│       │   └── README.md                    # Producer documentation
│       └── msk-dynamodb-processor/          # Lambda function (SAM project)
│           ├── template.yaml                # SAM template
│           ├── src/                         # Source code
│           └── README.md                    # Lambda documentation
└── README.md                                # This file
```

## Detailed Documentation

For detailed instructions, see:

- [Infrastructure Setup](Lambda/Java/README.md)
- [Kafka Producer Documentation](Lambda/Java/kafka-producer/README.md)
- [Lambda Function Documentation](Lambda/Java/msk-dynamodb-processor/README.md)

## Prerequisites

- AWS Account
- AWS CLI installed and configured
- Git client
- Java 11 or higher (for local development)
- Maven (for local development)

## Features

- **One-Click Deployment**: Deploy the entire infrastructure with a single CloudFormation template
- **Automated Setup**: EC2 instance automatically sets up the development environment
- **Realistic Data Generation**: Generate realistic fake person data using JavaFaker
- **Serverless Processing**: Process data using AWS Lambda triggered by MSK events
- **Observability**: Lambda function includes logging, metrics, and tracing with AWS Lambda Powertools
- **Local Testing**: Test the Lambda function locally using SAM CLI and DynamoDB Local

## Security Considerations

- MSK cluster uses IAM authentication
- All connections to MSK use TLS encryption
- Lambda function has least-privilege permissions
- EC2 instance has appropriate IAM role for deployment

## License

This project is licensed under the MIT License - see the LICENSE file for details.
