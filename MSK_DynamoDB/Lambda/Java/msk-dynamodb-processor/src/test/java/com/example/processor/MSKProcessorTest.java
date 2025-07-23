package com.example.processor;

import com.amazonaws.services.lambda.runtime.Context;
import com.amazonaws.services.lambda.runtime.LambdaLogger;
import com.amazonaws.services.lambda.runtime.events.KafkaEvent;
import com.example.processor.model.Person;
import com.example.processor.service.DynamoDBService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.MockedStatic;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;
import software.amazon.lambda.powertools.metrics.MetricsUtils;

import java.lang.reflect.Field;
import java.nio.charset.StandardCharsets;
import java.util.Base64;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

/**
 * Unit tests for the MSKProcessor class.
 */
@ExtendWith(MockitoExtension.class)
public class MSKProcessorTest {

    @Mock
    private Context mockContext;

    @Mock
    private LambdaLogger mockLogger;

    @Mock
    private DynamoDBService mockDynamoDBService;

    private MSKProcessor processor;
    private KafkaEvent kafkaEvent;

    @BeforeEach
    public void setUp() throws Exception {
        // Set up environment variable
        System.setProperty("DYNAMODB_TABLE_NAME", "TestTable");

        // Create processor instance
        processor = new MSKProcessor();

        // Use reflection to inject mock DynamoDBService
        Field dynamoDBServiceField = MSKProcessor.class.getDeclaredField("dynamoDBService");
        dynamoDBServiceField.setAccessible(true);
        dynamoDBServiceField.set(processor, mockDynamoDBService);

        // Mock Context and Logger
        when(mockContext.getLogger()).thenReturn(mockLogger);
        doNothing().when(mockLogger).log(anyString());

        // Create a sample Kafka event
        kafkaEvent = createSampleKafkaEvent();
    }

    @Test
    public void testHandleRequest() {
        // Given
        when(mockDynamoDBService.savePerson(any(Person.class))).thenReturn(new Person());

        // When
        try (MockedStatic<MetricsUtils> mockedMetricsUtils = Mockito.mockStatic(MetricsUtils.class)) {
            processor.handleRequest(kafkaEvent, mockContext);

            // Then
            verify(mockLogger, atLeastOnce()).log(anyString());
            verify(mockDynamoDBService, times(2)).savePerson(any(Person.class));
            mockedMetricsUtils.verify(() -> MetricsUtils.publishMetrics(), times(1));
        }
    }

    @Test
    public void testHandleRequest_WithException() {
        // Given
        when(mockDynamoDBService.savePerson(any(Person.class))).thenThrow(new RuntimeException("Test exception"));

        // When
        try (MockedStatic<MetricsUtils> mockedMetricsUtils = Mockito.mockStatic(MetricsUtils.class)) {
            processor.handleRequest(kafkaEvent, mockContext);

            // Then
            verify(mockLogger, atLeastOnce()).log(contains("Error processing record"));
            mockedMetricsUtils.verify(() -> MetricsUtils.putMetric(eq("ProcessingErrors"), eq(1.0), eq("Count")), times(2));
            mockedMetricsUtils.verify(() -> MetricsUtils.publishMetrics(), times(1));
        }
    }

    /**
     * Creates a sample Kafka event for testing.
     *
     * @return A sample KafkaEvent
     */
    private KafkaEvent createSampleKafkaEvent() {
        KafkaEvent event = new KafkaEvent();
        Map<String, List<KafkaEvent.KafkaEventRecord>> records = new HashMap<>();

        // Create two sample records
        KafkaEvent.KafkaEventRecord record1 = new KafkaEvent.KafkaEventRecord();
        record1.setTopic("test-topic");
        record1.setPartition(0);
        record1.setOffset(0L);
        record1.setTimestamp(System.currentTimeMillis());
        record1.setKey(Base64.getEncoder().encodeToString("test-batch-0".getBytes(StandardCharsets.UTF_8)));
        record1.setValue(Base64.getEncoder().encodeToString(
                "{\"Firstname\":\"John\",\"Lastname\":\"Doe\",\"StreetAddress\":\"123 Main St\",\"City\":\"Anytown\",\"State\":\"CA\",\"ZipCode\":\"12345\",\"PhoneNumber\":\"555-123-4567\",\"EmailAddress\":\"john.doe@example.com\",\"EmployerName\":\"ACME Inc\",\"EmployerAddress\":\"456 Business Ave\",\"EmployerPhoneNumber\":\"555-987-6543\",\"EmployerWebsite\":\"www.acme.com\"}"
                        .getBytes(StandardCharsets.UTF_8)));

        KafkaEvent.KafkaEventRecord record2 = new KafkaEvent.KafkaEventRecord();
        record2.setTopic("test-topic");
        record2.setPartition(0);
        record2.setOffset(1L);
        record2.setTimestamp(System.currentTimeMillis());
        record2.setKey(Base64.getEncoder().encodeToString("test-batch-1".getBytes(StandardCharsets.UTF_8)));
        record2.setValue(Base64.getEncoder().encodeToString(
                "{\"Firstname\":\"Jane\",\"Lastname\":\"Smith\",\"StreetAddress\":\"789 Oak St\",\"City\":\"Othertown\",\"State\":\"NY\",\"ZipCode\":\"67890\",\"PhoneNumber\":\"555-987-6543\",\"EmailAddress\":\"jane.smith@example.com\",\"EmployerName\":\"XYZ Corp\",\"EmployerAddress\":\"321 Corporate Blvd\",\"EmployerPhoneNumber\":\"555-123-4567\",\"EmployerWebsite\":\"www.xyz.com\"}"
                        .getBytes(StandardCharsets.UTF_8)));

        // Add records to the event
        records.put("test-topic", List.of(record1, record2));
        event.setRecords(records);

        return event;
    }
}
