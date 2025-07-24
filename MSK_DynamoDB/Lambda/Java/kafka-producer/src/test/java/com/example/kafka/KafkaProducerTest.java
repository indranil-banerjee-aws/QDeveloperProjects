package com.example.kafka;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.file.Path;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Unit tests for the KafkaProducer class.
 */
public class KafkaProducerTest {

    @TempDir
    Path tempDir;

    @Test
    public void testMainWithInvalidArgs() {
        // Test that main method handles invalid arguments gracefully
        String[] args = {"--invalid-arg"};
        
        // This should not throw an exception
        assertDoesNotThrow(() -> {
            KafkaProducer.main(args);
        });
    }

    @Test
    public void testMainWithHelpArg() {
        // Test that main method handles help argument
        String[] args = {"--help"};
        
        // This should not throw an exception
        assertDoesNotThrow(() -> {
            KafkaProducer.main(args);
        });
    }

    @Test
    public void testMainWithMissingRequiredArgs() {
        // Test that main method handles missing required arguments
        String[] args = {"--topic", "test-topic"};
        
        // This should not throw an exception (should exit gracefully)
        assertDoesNotThrow(() -> {
            KafkaProducer.main(args);
        });
    }

    @Test
    public void testMainWithValidArgsButInvalidPropertiesFile() throws IOException {
        // Create a temporary properties file
        File propertiesFile = tempDir.resolve("test.properties").toFile();
        try (FileWriter writer = new FileWriter(propertiesFile)) {
            writer.write("bootstrap.servers=localhost:9092\n");
            writer.write("key.serializer=org.apache.kafka.common.serialization.StringSerializer\n");
            writer.write("value.serializer=org.apache.kafka.common.serialization.StringSerializer\n");
        }

        String[] args = {
            "--properties", propertiesFile.getAbsolutePath(),
            "--topic", "test-topic",
            "--count", "1",
            "--batch-id", "test-batch"
        };

        // This should not throw an exception (may fail to connect to Kafka but should handle gracefully)
        assertDoesNotThrow(() -> {
            KafkaProducer.main(args);
        });
    }

    @Test
    public void testMainWithNonExistentPropertiesFile() {
        String[] args = {
            "--properties", "/non/existent/file.properties",
            "--topic", "test-topic",
            "--count", "1",
            "--batch-id", "test-batch"
        };

        // This should not throw an exception (should handle file not found gracefully)
        assertDoesNotThrow(() -> {
            KafkaProducer.main(args);
        });
    }
}
