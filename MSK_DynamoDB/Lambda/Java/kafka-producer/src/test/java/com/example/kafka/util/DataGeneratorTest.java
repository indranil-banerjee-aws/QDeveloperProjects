package com.example.kafka.util;

import com.example.kafka.model.Person;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Unit tests for the DataGenerator class.
 */
public class DataGeneratorTest {

    private DataGenerator dataGenerator;

    @BeforeEach
    public void setUp() {
        dataGenerator = new DataGenerator();
    }

    @Test
    public void testGeneratePerson_ShouldReturnValidPerson() {
        // When
        Person person = dataGenerator.generatePerson();

        // Then
        assertNotNull(person, "Generated person should not be null");
        assertNotNull(person.getFirstName(), "First name should not be null");
        assertNotNull(person.getLastName(), "Last name should not be null");
        assertNotNull(person.getStreetAddress(), "Street address should not be null");
        assertNotNull(person.getCity(), "City should not be null");
        assertNotNull(person.getState(), "State should not be null");
        assertNotNull(person.getCounty(), "County should not be null");
        assertNotNull(person.getZipCode(), "Zip code should not be null");
        assertNotNull(person.getPhoneNumber(), "Phone number should not be null");
        assertNotNull(person.getEmailAddress(), "Email address should not be null");
        assertNotNull(person.getEmployerName(), "Employer name should not be null");
        assertNotNull(person.getEmployerAddress(), "Employer address should not be null");
        assertNotNull(person.getEmployerPhoneNumber(), "Employer phone number should not be null");
        assertNotNull(person.getEmployerWebsite(), "Employer website should not be null");
    }

    @Test
    public void testGeneratePerson_ShouldGenerateUniquePersons() {
        // When
        Person person1 = dataGenerator.generatePerson();
        Person person2 = dataGenerator.generatePerson();

        // Then
        assertNotEquals(person1.getFirstName(), person2.getFirstName(), "First names should be different");
        assertNotEquals(person1.getLastName(), person2.getLastName(), "Last names should be different");
        assertNotEquals(person1.getEmailAddress(), person2.getEmailAddress(), "Email addresses should be different");
    }

    @Test
    public void testGeneratePerson_EmailShouldContainFirstAndLastName() {
        // When
        Person person = dataGenerator.generatePerson();

        // Then
        String firstName = person.getFirstName().toLowerCase();
        String lastName = person.getLastName().toLowerCase();
        String email = person.getEmailAddress().toLowerCase();

        assertTrue(email.contains(firstName) || email.contains(firstName.substring(0, 1)), 
                "Email should contain first name or its initial");
        assertTrue(email.contains(lastName) || email.contains(lastName.substring(0, 1)), 
                "Email should contain last name or its initial");
    }
}
