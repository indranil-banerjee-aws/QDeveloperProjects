package com.example.processor;

import com.amazonaws.services.lambda.runtime.Context;
import com.amazonaws.services.lambda.runtime.RequestHandler;
import com.amazonaws.services.lambda.runtime.events.KafkaEvent;
import com.amazonaws.services.lambda.runtime.events.KafkaEvent.KafkaEventRecord;
import com.example.processor.model.Person;
import com.example.processor.service.DynamoDBService;
import com.fasterxml.jackson.databind.ObjectMapper;
import software.amazon.lambda.powertools.logging.Logging;
import software.amazon.lambda.powertools.metrics.Metrics;
import software.amazon.lambda.powertools.metrics.MetricsUtils;
import software.amazon.lambda.powertools.tracing.Tracing;

import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.time.format.DateTimeFormatter;
import java.util.Base64;
import java.util.Map;

/**
 * Lambda function handler for processing Kafka messages from MSK and storing them in DynamoDB.
 */
public class MSKProcessor implements RequestHandler<KafkaEvent, Void> {
    private static final ObjectMapper objectMapper = new ObjectMapper();
    private static final String TABLE_NAME = System.getenv("DYNAMODB_TABLE_NAME");
    private final DynamoDBService dynamoDBService;

    public MSKProcessor() {
        this.dynamoDBService = new DynamoDBService(TABLE_NAME);
    }

    /**
     * Lambda function handler method.
     *
     * @param event   The Kafka event from MSK
     * @param context The Lambda context
     * @return void
     */
    @Override
    @Tracing
    @Logging(logEvent = true)
    @Metrics(namespace = "MSKDynamoDBProcessor", service = "MSKProcessor")
    public Void handleRequest(KafkaEvent event, Context context) {
        // Log the event
        context.getLogger().log("Received Kafka event with " + event.getRecords().size() + " record(s)");
        
        // Process each record
        for (Map.Entry<String, java.util.List<KafkaEventRecord>> entry : event.getRecords().entrySet()) {
            String topic = entry.getKey();
            context.getLogger().log("Processing records from topic: " + topic);
            
            for (KafkaEventRecord record : entry.getValue()) {
                processRecord(record, context);
            }
        }
        
        // Publish metrics
        MetricsUtils.publishMetrics();
        return null;
    }

    /**
     * Process a single Kafka record.
     *
     * @param record  The Kafka record
     * @param context The Lambda context
     */
    @Tracing
    @Logging(logEvent = true)
    private void processRecord(KafkaEventRecord record, Context context) {
        try {
            // Extract key and value
            String key = record.getKey() != null ? 
                    new String(Base64.getDecoder().decode(record.getKey()), StandardCharsets.UTF_8) : null;
            String value = new String(Base64.getDecoder().decode(record.getValue()), StandardCharsets.UTF_8);
            
            context.getLogger().log("Processing record with key: " + key);
            
            // Parse JSON into Person object
            Person person = objectMapper.readValue(value, Person.class);
            
            // Extract batch ID from key if available
            if (key != null && key.contains("-")) {
                String batchId = key.substring(0, key.lastIndexOf("-"));
                person.setBatchId(batchId);
            }
            
            // Add processing timestamp
            person.setProcessedTimestamp(DateTimeFormatter.ISO_INSTANT.format(Instant.now()));
            
            // Save to DynamoDB
            dynamoDBService.savePerson(person);
            
            // Log success
            context.getLogger().log("Successfully processed and saved record: " + person.getId());
            
            // Record metric
            MetricsUtils.putMetric("RecordsProcessed", 1, "Count");
            
        } catch (Exception e) {
            // Log error
            context.getLogger().log("Error processing record: " + e.getMessage());
            
            // Record error metric
            MetricsUtils.putMetric("ProcessingErrors", 1, "Count");
        }
    }
}
