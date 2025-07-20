package com.example.kafka;

import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.CommandLineParser;
import org.apache.commons.cli.DefaultParser;
import org.apache.commons.cli.Options;
import org.apache.kafka.clients.producer.MockProducer;
import org.apache.kafka.clients.producer.ProducerRecord;
import org.apache.kafka.common.serialization.StringSerializer;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.nio.file.Path;
import java.util.List;
import java.util.Properties;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Unit tests for the KafkaProducer class.
 */
public class KafkaProducerTest {

    private MockProducer<String, String> mockProducer;
    private Properties testProperties;
    
    @TempDir
    Path tempDir;

    @BeforeEach
    public void setUp() {
        mockProducer = new MockProducer<>(true, new StringSerializer(), new StringSerializer());
        
        testProperties = new Properties();
        testProperties.setProperty("bootstrap.servers", "localhost:9092");
        testProperties.setProperty("key.serializer", "org.apache.kafka.common.serialization.StringSerializer");
        testProperties.setProperty("value.serializer", "org.apache.kafka.common.serialization.StringSerializer");
    }

    @Test
    public void testParseCommandLineArgs() throws Exception {
        // Given
        String[] args = {
            "--properties", "kafka.properties",
            "--topic", "test-topic",
            "--batch-id", "test-batch",
            "--count", "5"
        };

        // Use reflection to access private method
        Method parseCommandLineArgsMethod = KafkaProducer.class.getDeclaredMethod("parseCommandLineArgs", String[].class);
        parseCommandLineArgsMethod.setAccessible(true);

        // When
        CommandLine cmd = (CommandLine) parseCommandLineArgsMethod.invoke(null, (Object) args);

        // Then
        assertNotNull(cmd, "CommandLine should not be null");
        assertEquals("kafka.properties", cmd.getOptionValue("properties"), "Properties file path should match");
        assertEquals("test-topic", cmd.getOptionValue("topic"), "Topic should match");
        assertEquals("test-batch", cmd.getOptionValue("batch-id"), "Batch ID should match");
        assertEquals("5", cmd.getOptionValue("count"), "Count should match");
    }

    @Test
    public void testLoadProperties() throws Exception {
        // Given
        File propsFile = tempDir.resolve("test.properties").toFile();
        try (FileWriter writer = new FileWriter(propsFile)) {
            testProperties.store(writer, "Test properties");
        }

        // Use reflection to access private method
        Method loadPropertiesMethod = KafkaProducer.class.getDeclaredMethod("loadProperties", String.class);
        loadPropertiesMethod.setAccessible(true);

        // When
        Properties loadedProps = (Properties) loadPropertiesMethod.invoke(null, propsFile.getAbsolutePath());

        // Then
        assertNotNull(loadedProps, "Loaded properties should not be null");
        assertEquals("localhost:9092", loadedProps.getProperty("bootstrap.servers"), "Bootstrap servers should match");
    }

    @Test
    public void testSendMessages() throws Exception {
        // Given
        String topic = "test-topic";
        String batchId = "test-batch";
        int messageCount = 5;

        // Use reflection to access private method
        Method sendMessagesMethod = KafkaProducer.class.getDeclaredMethod("sendMessages", 
                org.apache.kafka.clients.producer.Producer.class, String.class, String.class, int.class);
        sendMessagesMethod.setAccessible(true);

        // When
        sendMessagesMethod.invoke(null, mockProducer, topic, batchId, messageCount);

        // Then
        List<ProducerRecord<String, String>> history = mockProducer.history();
        assertEquals(messageCount, history.size(), "Should have sent " + messageCount + " messages");
        
        for (int i = 0; i < messageCount; i++) {
            ProducerRecord<String, String> record = history.get(i);
            assertEquals(topic, record.topic(), "Topic should match");
            assertEquals(batchId + "-" + i, record.key(), "Key should match pattern");
            assertNotNull(record.value(), "Value should not be null");
            assertTrue(record.value().contains("Firstname"), "Value should contain Firstname field");
            assertTrue(record.value().contains("Lastname"), "Value should contain Lastname field");
        }
    }
}
