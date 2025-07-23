package com.example.kafka.util;

import com.example.kafka.model.Person;
import com.github.javafaker.Faker;

import java.util.Locale;

/**
 * Utility class to generate fake data using JavaFaker library.
 */
public class DataGenerator {
    private final Faker faker;

    public DataGenerator() {
        this.faker = new Faker(Locale.US);
    }

    /**
     * Generates a Person object with random fake data.
     *
     * @return Person object with fake data
     */
    public Person generatePerson() {
        // Generate personal information
        String firstName = faker.name().firstName();
        String lastName = faker.name().lastName();
        String streetAddress = faker.address().streetAddress();
        String city = faker.address().city();
        String state = faker.address().state();
        String zipCode = faker.address().zipCode();
        String phoneNumber = faker.phoneNumber().phoneNumber();
        String emailAddress = firstName.toLowerCase() + "." + lastName.toLowerCase() + "@" + faker.internet().domainName();

        // Generate employer information
        String employerName = faker.company().name();
        String employerAddress = faker.address().fullAddress();
        String employerPhoneNumber = faker.phoneNumber().phoneNumber();
        String employerWebsite = "www." + employerName.toLowerCase().replaceAll("[^a-zA-Z0-9]", "") + "." + faker.internet().domainSuffix();

        return new Person(
                firstName,
                lastName,
                streetAddress,
                city,
                state,
                zipCode,
                phoneNumber,
                emailAddress,
                employerName,
                employerAddress,
                employerPhoneNumber,
                employerWebsite
        );
    }
}
