package tr.com.huseyinaydin.domain.fixtures;

import tr.com.huseyinaydin.domain.customer.IndividualCustomer;
import java.time.LocalDate;
import java.util.UUID;
import java.util.Random;

public class IndividualCustomerTestFixture {
    private String firstName = "John";
    private String lastName = "Doe";
    private String nationalId = "12345678901";
    private String email = "john.doe@example.com";
    private LocalDate dateOfBirth = LocalDate.of(1990, 1, 1);
    private String motherName = "Jane";
    private String fatherName = "Jim";
    private String phoneNumber = "5551234567";

    public IndividualCustomerTestFixture withFirstName(String firstName) {
        this.firstName = firstName;
        return this;
    }

    public IndividualCustomerTestFixture withLastName(String lastName) {
        this.lastName = lastName;
        return this;
    }

    public IndividualCustomerTestFixture withNationalId(String nationalId) {
        this.nationalId = nationalId;
        return this;
    }

    public IndividualCustomerTestFixture withEmail(String email) {
        this.email = email;
        return this;
    }

    public IndividualCustomer build() {
        IndividualCustomer customer = new IndividualCustomer(firstName, lastName, nationalId, email);
        customer.setDateOfBirth(dateOfBirth);
        customer.setMotherName(motherName);
        customer.setFatherName(fatherName);
        customer.setPhoneNumber(phoneNumber);
        return customer;
    }

    public static IndividualCustomer random() {
        Random random = new Random();
        long randomNationalId = 10000000000L + (long)(random.nextDouble() * 90000000000L);
        return new IndividualCustomerTestFixture()
                .withFirstName("Name" + random.nextInt(1000))
                .withLastName("LastName" + random.nextInt(1000))
                .withNationalId(String.valueOf(randomNationalId))
                .withEmail("random" + random.nextInt(10000) + "@test.com")
                .build();
    }
}
