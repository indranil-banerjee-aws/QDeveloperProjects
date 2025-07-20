package com.example.processor.model;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Unit tests for the Person model class.
 */
public class PersonTest {

    private final ObjectMapper objectMapper = new ObjectMapper();

    @Test
    public void testPersonDeserialization() throws Exception {
        // Given
        String json = "{\"Firstname\":\"Jane\",\"Lastname\":\"Smith\",\"StreetAddress\":\"789 Oak St\",\"City\":\"Othertown\",\"State\":\"NY\",\"County\":\"Other County\",\"ZipCode\":\"67890\",\"PhoneNumber\":\"555-987-6543\",\"EmailAddress\":\"jane.smith@example.com\",\"EmployerName\":\"XYZ Corp\",\"EmployerAddress\":\"321 Corporate Blvd\",\"EmployerPhoneNumber\":\"555-123-4567\",\"EmployerWebsite\":\"www.xyz.com\"}";

        // When
        Person person = objectMapper.readValue(json, Person.class);

        // Then
        assertNotNull(person.getId(), "ID should be automatically generated");
        assertEquals("Jane", person.getFirstName(), "First name should match");
        assertEquals("Smith", person.getLastName(), "Last name should match");
        assertEquals("789 Oak St", person.getStreetAddress(), "Street address should match");
        assertEquals("Othertown", person.getCity(), "City should match");
        assertEquals("NY", person.getState(), "State should match");
        assertEquals("Other County", person.getCounty(), "County should match");
        assertEquals("67890", person.getZipCode(), "Zip code should match");
        assertEquals("555-987-6543", person.getPhoneNumber(), "Phone number should match");
        assertEquals("jane.smith@example.com", person.getEmailAddress(), "Email address should match");
        assertEquals("XYZ Corp", person.getEmployerName(), "Employer name should match");
        assertEquals("321 Corporate Blvd", person.getEmployerAddress(), "Employer address should match");
        assertEquals("555-123-4567", person.getEmployerPhoneNumber(), "Employer phone number should match");
        assertEquals("www.xyz.com", person.getEmployerWebsite(), "Employer website should match");
    }

    @Test
    public void testPersonIdGeneration() {
        // When
        Person person1 = new Person();
        Person person2 = new Person();

        // Then
        assertNotNull(person1.getId(), "ID should not be null");
        assertNotNull(person2.getId(), "ID should not be null");
        assertNotEquals(person1.getId(), person2.getId(), "IDs should be unique");
    }

    @Test
    public void testPersonSettersAndGetters() {
        // Given
        Person person = new Person();
        String id = "test-id";
        String batchId = "test-batch";
        String timestamp = "2023-07-19T12:00:00Z";

        // When
        person.setId(id);
        person.setBatchId(batchId);
        person.setProcessedTimestamp(timestamp);

        // Then
        assertEquals(id, person.getId(), "ID should match");
        assertEquals(batchId, person.getBatchId(), "Batch ID should match");
        assertEquals(timestamp, person.getProcessedTimestamp(), "Timestamp should match");
    }
}
