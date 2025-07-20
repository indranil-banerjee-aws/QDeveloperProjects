package com.example.processor.service;

import com.example.processor.model.Person;
import software.amazon.awssdk.enhanced.dynamodb.DynamoDbEnhancedClient;
import software.amazon.awssdk.enhanced.dynamodb.DynamoDbTable;
import software.amazon.awssdk.enhanced.dynamodb.TableSchema;
import software.amazon.awssdk.regions.Region;
import software.amazon.awssdk.services.dynamodb.DynamoDbClient;
import software.amazon.lambda.powertools.logging.Logging;
import software.amazon.lambda.powertools.metrics.Metrics;
import software.amazon.lambda.powertools.tracing.Tracing;

/**
 * Service class for DynamoDB operations.
 */
public class DynamoDBService {
    private final DynamoDbEnhancedClient enhancedClient;
    private final DynamoDbTable<Person> personTable;

    /**
     * Constructor that initializes the DynamoDB client and table.
     *
     * @param tableName The name of the DynamoDB table
     */
    public DynamoDBService(String tableName) {
        // Create DynamoDB client
        DynamoDbClient ddbClient = DynamoDbClient.builder()
                .region(Region.of(System.getenv("AWS_REGION")))
                .build();

        // Create enhanced client
        this.enhancedClient = DynamoDbEnhancedClient.builder()
                .dynamoDbClient(ddbClient)
                .build();

        // Get a reference to the person table
        this.personTable = enhancedClient.table(tableName, TableSchema.fromBean(Person.class));
    }

    /**
     * Saves a person to DynamoDB.
     *
     * @param person The person to save
     * @return The saved person
     */
    @Tracing
    @Logging(logEvent = true)
    @Metrics(namespace = "MSKDynamoDBProcessor", service = "DynamoDBService")
    public Person savePerson(Person person) {
        personTable.putItem(person);
        return person;
    }
}
