# Kafka Producer for MSK

This Java application generates fake person data and sends it as JSON messages to an Amazon MSK Kafka cluster.

## Prerequisites

- Java 11 or higher
- Apache Maven
- Access to an Amazon MSK cluster with IAM authentication enabled

## Building the Application

To build the application, run:

```bash
mvn clean package
```

This will create a JAR file with all dependencies in the `target` directory.

## Configuration

Before running the application, you need to create a properties file with Kafka configuration. A sample file `kafka-client.properties` is provided.

Update the `bootstrap.servers` property with your MSK bootstrap servers. You can get this from the AWS Console or using the AWS CLI:

```bash
aws kafka get-bootstrap-brokers --cluster-arn YOUR_CLUSTER_ARN --region YOUR_REGION
```

For MSK with IAM authentication, use the `BootstrapBrokerStringSaslIam` value for provisioned clusters or `BootstrapBrokerStringVpcConnectivitySaslIam` for serverless clusters.

## Running the Application

Run the application using the following command:

```bash
java -jar target/kafka-producer-1.0-SNAPSHOT-jar-with-dependencies.jar \
  --properties kafka-client.properties \
  --topic YOUR_TOPIC_NAME \
  --batch-id unique-batch-identifier \
  --count 1000
```

### Command Line Arguments

- `--properties` or `-p`: Path to the Kafka properties file (required)
- `--topic` or `-t`: Kafka topic to send messages to (required)
- `--batch-id` or `-b`: Unique identifier for this batch of messages (optional, defaults to a random UUID)
- `--count` or `-c`: Number of messages to send (optional, defaults to 10)

## Message Format

Each message is a JSON object with the following fields:

- Firstname
- Lastname
- StreetAddress
- City
- State
- County
- ZipCode
- PhoneNumber
- EmailAddress
- EmployerName
- EmployerAddress
- EmployerPhoneNumber
- EmployerWebsite

## Example

```bash
# Send 100 messages to the 'customer-data' topic
java -jar target/kafka-producer-1.0-SNAPSHOT-jar-with-dependencies.jar \
  --properties kafka-client.properties \
  --topic customer-data \
  --batch-id batch-2023-07-19 \
  --count 100
```

## Troubleshooting

If you encounter issues with IAM authentication, ensure:

1. The EC2 instance has the correct IAM role with MSK permissions
2. The MSK cluster has IAM authentication enabled
3. The security groups allow traffic between the EC2 instance and MSK cluster
4. The AWS MSK IAM Auth JAR is in the classpath (included in the packaged JAR)
