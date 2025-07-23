package com.example.kafka.util;

import com.example.kafka.model.Person;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Unit tests for the DataGenerator utility class.
 */
public class DataGeneratorTest {

    private DataGenerator dataGenerator;

    @BeforeEach
    public void setUp() {
        dataGenerator = new DataGenerator();
    }

    @Test
    public void testGeneratePerson() {
        Person person = dataGenerator.generatePerson();

        assertNotNull(person, "Generated person should not be null");
        assertNotNull(person.getFirstName(), "First name should not be null");
        assertNotNull(person.getLastName(), "Last name should not be null");
        assertNotNull(person.getStreetAddress(), "Street address should not be null");
        assertNotNull(person.getCity(), "City should not be null");
        assertNotNull(person.getState(), "State should not be null");
        assertNotNull(person.getZipCode(), "Zip code should not be null");
        assertNotNull(person.getPhoneNumber(), "Phone number should not be null");
        assertNotNull(person.getEmailAddress(), "Email address should not be null");
        assertNotNull(person.getEmployerName(), "Employer name should not be null");
        assertNotNull(person.getEmployerAddress(), "Employer address should not be null");
        assertNotNull(person.getEmployerPhoneNumber(), "Employer phone number should not be null");
        assertNotNull(person.getEmployerWebsite(), "Employer website should not be null");
    }

    @Test
    public void testGenerateMultiplePersons() {
        Person person1 = dataGenerator.generatePerson();
        Person person2 = dataGenerator.generatePerson();

        assertNotEquals(person1.getFirstName(), person2.getFirstName(), "First names should be different");
        assertNotEquals(person1.getLastName(), person2.getLastName(), "Last names should be different");
        assertNotEquals(person1.getEmailAddress(), person2.getEmailAddress(), "Email addresses should be different");
    }

    @Test
    public void testEmailAddressFormat() {
        Person person = dataGenerator.generatePerson();
        String email = person.getEmailAddress();

        assertTrue(email.contains("@"), "Email should contain @ symbol");
        assertTrue(email.contains("."), "Email should contain domain extension");
        assertTrue(email.indexOf("@") < email.lastIndexOf("."), "@ should come before the last dot");
    }
}
