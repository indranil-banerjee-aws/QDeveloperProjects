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
            "--count", "5",
            "--batch-id", "test-batch"
        };

        // When
        Options options = KafkaProducer.createOptions();
        CommandLineParser parser = new DefaultParser();
        CommandLine cmd = parser.parse(options, args);

        // Then
        assertNotNull(cmd, "Command line should be parsed successfully");
        assertEquals("kafka.properties", cmd.getOptionValue("properties"), "Properties file should match");
        assertEquals("test-topic", cmd.getOptionValue("topic"), "Topic should match");
        assertEquals("5", cmd.getOptionValue("count"), "Count should match");
        assertEquals("test-batch", cmd.getOptionValue("batch-id"), "Batch ID should match");
    }

    @Test
    public void testLoadProperties() throws IOException {
        // Given
        File propertiesFile = tempDir.resolve("test.properties").toFile();
        try (FileWriter writer = new FileWriter(propertiesFile)) {
            writer.write("bootstrap.servers=localhost:9092\n");
            writer.write("key.serializer=org.apache.kafka.common.serialization.StringSerializer\n");
        }

        // When
        Properties properties = KafkaProducer.loadProperties(propertiesFile.getAbsolutePath());

        // Then
        assertNotNull(properties, "Properties should not be null");
        assertEquals("localhost:9092", properties.getProperty("bootstrap.servers"), "Bootstrap servers should match");
    }

    @Test
    public void testGenerateMessages() throws Exception {
        // Given
        String topic = "test-topic";
        String batchId = "test-batch";
        int count = 3;

        // When
        List<ProducerRecord<String, String>> records = KafkaProducer.generateMessages(topic, count, batchId);

        // Then
        assertEquals(count, records.size(), "Should generate correct number of records");
        
        for (ProducerRecord<String, String> record : records) {
            assertEquals(topic, record.topic(), "Topic should match");
            assertNotNull(record.key(), "Key should not be null");
            assertNotNull(record.value(), "Value should not be null");
            assertTrue(record.key().startsWith(batchId), "Key should start with batch ID");
            assertTrue(record.value().contains("firstName"), "Value should contain firstName field");
            assertTrue(record.value().contains("lastName"), "Value should contain lastName field");
        }
    }
}
