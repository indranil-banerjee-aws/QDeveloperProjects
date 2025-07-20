package com.example.kafka.model;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Unit tests for the Person model class.
 */
public class PersonTest {

    private final ObjectMapper objectMapper = new ObjectMapper();

    @Test
    public void testPersonSerialization() throws Exception {
        // Given
        Person person = new Person();
        person.setFirstName("John");
        person.setLastName("Doe");
        person.setStreetAddress("123 Main St");
        person.setCity("Anytown");
        person.setState("CA");
        person.setCounty("Any County");
        person.setZipCode("12345");
        person.setPhoneNumber("555-123-4567");
        person.setEmailAddress("john.doe@example.com");
        person.setEmployerName("ACME Inc");
        person.setEmployerAddress("456 Business Ave");
        person.setEmployerPhoneNumber("555-987-6543");
        person.setEmployerWebsite("www.acme.com");

        // When
        String json = objectMapper.writeValueAsString(person);

        // Then
        assertTrue(json.contains("\"Firstname\":\"John\""), "JSON should contain Firstname field");
        assertTrue(json.contains("\"Lastname\":\"Doe\""), "JSON should contain Lastname field");
        assertTrue(json.contains("\"StreetAddress\":\"123 Main St\""), "JSON should contain StreetAddress field");
        assertTrue(json.contains("\"City\":\"Anytown\""), "JSON should contain City field");
        assertTrue(json.contains("\"State\":\"CA\""), "JSON should contain State field");
        assertTrue(json.contains("\"County\":\"Any County\""), "JSON should contain County field");
        assertTrue(json.contains("\"ZipCode\":\"12345\""), "JSON should contain ZipCode field");
        assertTrue(json.contains("\"PhoneNumber\":\"555-123-4567\""), "JSON should contain PhoneNumber field");
        assertTrue(json.contains("\"EmailAddress\":\"john.doe@example.com\""), "JSON should contain EmailAddress field");
        assertTrue(json.contains("\"EmployerName\":\"ACME Inc\""), "JSON should contain EmployerName field");
        assertTrue(json.contains("\"EmployerAddress\":\"456 Business Ave\""), "JSON should contain EmployerAddress field");
        assertTrue(json.contains("\"EmployerPhoneNumber\":\"555-987-6543\""), "JSON should contain EmployerPhoneNumber field");
        assertTrue(json.contains("\"EmployerWebsite\":\"www.acme.com\""), "JSON should contain EmployerWebsite field");
    }

    @Test
    public void testPersonDeserialization() throws Exception {
        // Given
        String json = "{\"Firstname\":\"Jane\",\"Lastname\":\"Smith\",\"StreetAddress\":\"789 Oak St\",\"City\":\"Othertown\",\"State\":\"NY\",\"County\":\"Other County\",\"ZipCode\":\"67890\",\"PhoneNumber\":\"555-987-6543\",\"EmailAddress\":\"jane.smith@example.com\",\"EmployerName\":\"XYZ Corp\",\"EmployerAddress\":\"321 Corporate Blvd\",\"EmployerPhoneNumber\":\"555-123-4567\",\"EmployerWebsite\":\"www.xyz.com\"}";

        // When
        Person person = objectMapper.readValue(json, Person.class);

        // Then
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
}
