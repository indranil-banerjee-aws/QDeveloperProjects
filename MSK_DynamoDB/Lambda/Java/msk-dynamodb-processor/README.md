# MSK to DynamoDB Processor

This AWS SAM project deploys a Lambda function that processes Kafka messages from an MSK cluster and stores the data in a DynamoDB table.

## Architecture

The solution consists of:

1. A Lambda function triggered by messages from an MSK Kafka cluster
2. A DynamoDB table to store the processed data
3. IAM roles and policies for the Lambda function

## Prerequisites

- AWS CLI installed and configured
- AWS SAM CLI installed
- Java 11 or higher
- Apache Maven
- Docker (for local testing)
- An existing MSK cluster (created by the CloudFormation template)

## Building the Application

To build the application, run:

```bash
sam build
```

## Local Testing

This project includes everything you need to test the Lambda function locally:

1. **Start DynamoDB Local**:
   ```bash
   ./setup-dynamodb-local.sh
   ```
   This script starts DynamoDB Local using Docker Compose and creates the required table.

2. **Run the Lambda function locally**:
   ```bash
   ./test-local.sh
   ```
   This script builds the application and invokes the Lambda function with a sample MSK event.

3. **Query the local DynamoDB table**:
   ```bash
   aws dynamodb scan --table-name PersonData --endpoint-url http://localhost:8000 --region us-east-1
   ```

## Running Unit Tests

To run the unit tests:

```bash
mvn test
```

## Deploying the Application

To deploy the application, run:

```bash
sam deploy --guided
```

You will be prompted for the following parameters:

- **Stack Name**: Name of the CloudFormation stack
- **AWS Region**: Region to deploy to
- **MSKClusterArn**: ARN of the MSK cluster (from the CloudFormation output)
- **KafkaTopicName**: Name of the Kafka topic to consume messages from
- **Environment**: Environment name (dev, test, prod)
- **DynamoDBTableName**: Name of the DynamoDB table to create

## Testing the Application

1. Use the Kafka producer application to send messages to the MSK cluster
2. The Lambda function will automatically process these messages and store them in DynamoDB
3. Check the CloudWatch logs to verify processing
4. Query the DynamoDB table to see the stored data

## Monitoring

The Lambda function uses AWS Lambda Powertools for:

- Structured logging
- Metrics
- Tracing

You can view logs, metrics, and traces in:

- CloudWatch Logs
- CloudWatch Metrics
- AWS X-Ray

## Cleanup

To delete the deployed resources, run:

```bash
sam delete
```

To stop DynamoDB Local:

```bash
docker-compose down
```
