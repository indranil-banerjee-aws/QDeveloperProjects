package com.example.kafka;

import com.example.kafka.model.Person;
import com.example.kafka.util.DataGenerator;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.apache.commons.cli.*;
import org.apache.kafka.clients.producer.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.FileInputStream;
import java.io.IOException;
import java.util.Properties;
import java.util.UUID;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * Kafka Producer application that sends fake person data to a Kafka topic.
 */
public class KafkaProducer {
    private static final Logger logger = LoggerFactory.getLogger(KafkaProducer.class);
    private static final ObjectMapper objectMapper = new ObjectMapper();
    private static final DataGenerator dataGenerator = new DataGenerator();

    public static void main(String[] args) {
        // Parse command line arguments
        CommandLine cmd = parseCommandLineArgs(args);
        if (cmd == null) {
            return;
        }

        String propertiesFile = cmd.getOptionValue("properties");
        String topic = cmd.getOptionValue("topic");
        String batchId = cmd.getOptionValue("batch-id", UUID.randomUUID().toString());
        int messageCount = Integer.parseInt(cmd.getOptionValue("count", "10"));

        logger.info("Starting Kafka Producer with the following parameters:");
        logger.info("Properties File: {}", propertiesFile);
        logger.info("Topic: {}", topic);
        logger.info("Batch ID: {}", batchId);
        logger.info("Message Count: {}", messageCount);

        // Load Kafka properties
        Properties kafkaProps = loadProperties(propertiesFile);
        if (kafkaProps == null) {
            return;
        }

        // Create Kafka producer
        try (Producer<String, String> producer = new org.apache.kafka.clients.producer.KafkaProducer<>(kafkaProps)) {
            sendMessages(producer, topic, batchId, messageCount);
        } catch (Exception e) {
            logger.error("Error creating or using Kafka producer", e);
        }
    }

    /**
     * Parse command line arguments.
     *
     * @param args Command line arguments
     * @return CommandLine object or null if parsing fails
     */
    private static CommandLine parseCommandLineArgs(String[] args) {
        Options options = new Options();

        Option propertiesOption = Option.builder("p")
                .longOpt("properties")
                .desc("Properties file with Kafka configuration")
                .hasArg()
                .required(true)
                .build();

        Option topicOption = Option.builder("t")
                .longOpt("topic")
                .desc("Kafka topic to send messages to")
                .hasArg()
                .required(true)
                .build();

        Option batchIdOption = Option.builder("b")
                .longOpt("batch-id")
                .desc("Unique identifier for this batch of messages")
                .hasArg()
                .required(false)
                .build();

        Option countOption = Option.builder("c")
                .longOpt("count")
                .desc("Number of messages to send")
                .hasArg()
                .required(false)
                .build();

        options.addOption(propertiesOption);
        options.addOption(topicOption);
        options.addOption(batchIdOption);
        options.addOption(countOption);

        CommandLineParser parser = new DefaultParser();
        HelpFormatter formatter = new HelpFormatter();

        try {
            return parser.parse(options, args);
        } catch (ParseException e) {
            logger.error("Error parsing command line arguments: {}", e.getMessage());
            formatter.printHelp("KafkaProducer", options);
            return null;
        }
    }

    /**
     * Load Kafka properties from a file.
     *
     * @param propertiesFile Path to properties file
     * @return Properties object or null if loading fails
     */
    private static Properties loadProperties(String propertiesFile) {
        Properties props = new Properties();
        try (FileInputStream fis = new FileInputStream(propertiesFile)) {
            props.load(fis);
            logger.info("Loaded Kafka properties from {}", propertiesFile);
            return props;
        } catch (IOException e) {
            logger.error("Error loading properties file: {}", e.getMessage());
            return null;
        }
    }

    /**
     * Send messages to Kafka topic.
     *
     * @param producer     Kafka producer
     * @param topic        Topic to send messages to
     * @param batchId      Unique identifier for this batch
     * @param messageCount Number of messages to send
     */
    private static void sendMessages(Producer<String, String> producer, String topic, String batchId, int messageCount) {
        logger.info("Sending {} messages to topic {} with batch ID {}", messageCount, topic, batchId);

        final CountDownLatch latch = new CountDownLatch(messageCount);
        final AtomicInteger successCount = new AtomicInteger(0);
        final AtomicInteger failureCount = new AtomicInteger(0);

        for (int i = 0; i < messageCount; i++) {
            try {
                // Generate fake person data
                Person person = dataGenerator.generatePerson();
                
                // Convert to JSON
                String jsonData = objectMapper.writeValueAsString(person);
                
                // Create a unique key for each message
                String key = batchId + "-" + i;
                
                // Create ProducerRecord
                ProducerRecord<String, String> record = new ProducerRecord<>(topic, key, jsonData);
                
                // Send message asynchronously
                producer.send(record, (metadata, exception) -> {
                    if (exception == null) {
                        successCount.incrementAndGet();
                        logger.debug("Message sent successfully: topic={}, partition={}, offset={}",
                                metadata.topic(), metadata.partition(), metadata.offset());
                    } else {
                        failureCount.incrementAndGet();
                        logger.error("Error sending message: {}", exception.getMessage());
                    }
                    latch.countDown();
                });
                
                // Log progress every 100 messages
                if ((i + 1) % 100 == 0 || i == messageCount - 1) {
                    logger.info("Progress: sent {}/{} messages", i + 1, messageCount);
                }
            } catch (Exception e) {
                logger.error("Error preparing message {}: {}", i, e.getMessage());
                latch.countDown();
                failureCount.incrementAndGet();
            }
        }

        try {
            // Wait for all messages to be acknowledged
            latch.await();
            logger.info("Finished sending messages. Success: {}, Failures: {}", 
                    successCount.get(), failureCount.get());
        } catch (InterruptedException e) {
            logger.error("Interrupted while waiting for message acknowledgments", e);
            Thread.currentThread().interrupt();
        }
    }
}
