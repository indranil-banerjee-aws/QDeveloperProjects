package com.example.kafka.model;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Unit tests for the Person model class.
 */
public class PersonTest {

    @Test
    public void testPersonCreation() {
        Person person = new Person();
        person.setFirstName("John");
        person.setLastName("Doe");
        person.setStreetAddress("123 Main St");
        person.setCity("Anytown");
        person.setState("CA");
        person.setZipCode("12345");
        person.setPhoneNumber("555-1234");
        person.setEmailAddress("john.doe@example.com");
        person.setEmployerName("ACME Corp");
        person.setEmployerAddress("456 Business Ave");
        person.setEmployerPhoneNumber("555-5678");
        person.setEmployerWebsite("www.acme.com");

        // Verify all fields are set correctly
        assertTrue(person.getFirstName() != null && !person.getFirstName().isEmpty(), "First name should not be null or empty");
        assertTrue(person.getLastName() != null && !person.getLastName().isEmpty(), "Last name should not be null or empty");
        assertTrue(person.getStreetAddress() != null && !person.getStreetAddress().isEmpty(), "Street address should not be null or empty");
        assertTrue(person.getCity() != null && !person.getCity().isEmpty(), "City should not be null or empty");
        assertTrue(person.getState() != null && !person.getState().isEmpty(), "State should not be null or empty");
        assertTrue(person.getZipCode() != null && !person.getZipCode().isEmpty(), "Zip code should not be null or empty");
        assertTrue(person.getPhoneNumber() != null && !person.getPhoneNumber().isEmpty(), "Phone number should not be null or empty");
        assertTrue(person.getEmailAddress() != null && !person.getEmailAddress().isEmpty(), "Email address should not be null or empty");
        assertTrue(person.getEmployerName() != null && !person.getEmployerName().isEmpty(), "Employer name should not be null or empty");
        assertTrue(person.getEmployerAddress() != null && !person.getEmployerAddress().isEmpty(), "Employer address should not be null or empty");
        assertTrue(person.getEmployerPhoneNumber() != null && !person.getEmployerPhoneNumber().isEmpty(), "Employer phone number should not be null or empty");
        assertTrue(person.getEmployerWebsite() != null && !person.getEmployerWebsite().isEmpty(), "Employer website should not be null or empty");
    }

    @Test
    public void testPersonConstructor() {
        Person person = new Person("Jane", "Smith", "789 Oak St", "Springfield", "IL", 
                                 "67890", "555-9876", "jane.smith@example.com",
                                 "Tech Solutions", "321 Tech Blvd", "555-4321", "www.techsolutions.com");

        assertEquals("Jane", person.getFirstName(), "First name should match constructor parameter");
        assertEquals("Smith", person.getLastName(), "Last name should match constructor parameter");
        assertEquals("789 Oak St", person.getStreetAddress(), "Street address should match constructor parameter");
        assertEquals("Springfield", person.getCity(), "City should match constructor parameter");
        assertEquals("IL", person.getState(), "State should match constructor parameter");
        assertEquals("67890", person.getZipCode(), "Zip code should match constructor parameter");
        assertEquals("555-9876", person.getPhoneNumber(), "Phone number should match constructor parameter");
        assertEquals("jane.smith@example.com", person.getEmailAddress(), "Email address should match constructor parameter");
        assertEquals("Tech Solutions", person.getEmployerName(), "Employer name should match constructor parameter");
        assertEquals("321 Tech Blvd", person.getEmployerAddress(), "Employer address should match constructor parameter");
        assertEquals("555-4321", person.getEmployerPhoneNumber(), "Employer phone number should match constructor parameter");
        assertEquals("www.techsolutions.com", person.getEmployerWebsite(), "Employer website should match constructor parameter");
    }
}
