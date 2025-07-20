package com.example.processor.service;

import com.example.processor.model.Person;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.MockedStatic;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;
import software.amazon.awssdk.enhanced.dynamodb.DynamoDbEnhancedClient;
import software.amazon.awssdk.enhanced.dynamodb.DynamoDbTable;
import software.amazon.awssdk.enhanced.dynamodb.TableSchema;
import software.amazon.awssdk.regions.Region;
import software.amazon.awssdk.services.dynamodb.DynamoDbClient;

import java.lang.reflect.Field;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

/**
 * Unit tests for the DynamoDBService class.
 */
@ExtendWith(MockitoExtension.class)
public class DynamoDBServiceTest {

    @Mock
    private DynamoDbClient mockDynamoDbClient;

    @Mock
    private DynamoDbEnhancedClient mockEnhancedClient;

    @Mock
    private DynamoDbTable<Person> mockPersonTable;

    private DynamoDBService dynamoDBService;
    private final String tableName = "TestTable";

    @BeforeEach
    public void setUp() throws Exception {
        // Mock the static builder methods
        try (MockedStatic<DynamoDbClient> mockedDynamoDbClient = Mockito.mockStatic(DynamoDbClient.class);
             MockedStatic<DynamoDbEnhancedClient> mockedEnhancedClient = Mockito.mockStatic(DynamoDbEnhancedClient.class)) {
            
            // Mock DynamoDbClient builder
            DynamoDbClient.Builder mockDynamoDbClientBuilder = mock(DynamoDbClient.Builder.class);
            mockedDynamoDbClient.when(DynamoDbClient::builder).thenReturn(mockDynamoDbClientBuilder);
            when(mockDynamoDbClientBuilder.region(any(Region.class))).thenReturn(mockDynamoDbClientBuilder);
            when(mockDynamoDbClientBuilder.build()).thenReturn(mockDynamoDbClient);
            
            // Mock DynamoDbEnhancedClient builder
            DynamoDbEnhancedClient.Builder mockEnhancedClientBuilder = mock(DynamoDbEnhancedClient.Builder.class);
            mockedEnhancedClient.when(DynamoDbEnhancedClient::builder).thenReturn(mockEnhancedClientBuilder);
            when(mockEnhancedClientBuilder.dynamoDbClient(any(DynamoDbClient.class))).thenReturn(mockEnhancedClientBuilder);
            when(mockEnhancedClientBuilder.build()).thenReturn(mockEnhancedClient);
            
            // Mock table method
            when(mockEnhancedClient.table(eq(tableName), any(TableSchema.class))).thenReturn(mockPersonTable);
            
            // Set environment variable for AWS_REGION
            System.setProperty("AWS_REGION", "us-east-1");
            
            // Create the service
            dynamoDBService = new DynamoDBService(tableName);
            
            // Use reflection to set the mocked fields
            Field enhancedClientField = DynamoDBService.class.getDeclaredField("enhancedClient");
            enhancedClientField.setAccessible(true);
            enhancedClientField.set(dynamoDBService, mockEnhancedClient);
            
            Field personTableField = DynamoDBService.class.getDeclaredField("personTable");
            personTableField.setAccessible(true);
            personTableField.set(dynamoDBService, mockPersonTable);
        }
    }

    @Test
    public void testSavePerson() {
        // Given
        Person person = new Person();
        person.setFirstName("John");
        person.setLastName("Doe");

        // When
        dynamoDBService.savePerson(person);

        // Then
        verify(mockPersonTable, times(1)).putItem(person);
    }
}
