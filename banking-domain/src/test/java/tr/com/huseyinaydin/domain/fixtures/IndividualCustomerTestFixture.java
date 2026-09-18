package tr.com.huseyinaydin.domain.fixtures;

import tr.com.huseyinaydin.domain.customer.IndividualCustomer;

import java.time.LocalDate;
import java.util.UUID;

public class IndividualCustomerTestFixture {

    public static IndividualCustomer createValidCustomer() {
        return createValidCustomer(UUID.randomUUID(), "11111111110");
    }

    public static IndividualCustomer createValidCustomer(UUID id, String nationalId) {
        IndividualCustomer customer = new IndividualCustomer(
                "Ahmet",
                "Yılmaz",
                nationalId,
                "ahmet.yilmaz@example.com"
        );

        customer.updatePersonalInfo(
                "Ahmet",
                "Yılmaz",
                LocalDate.of(1990, 1, 1),
                "Ayşe",
                "Mehmet"
        );

        customer.updateContactInfo(
                "5551234567",
                "ahmet.yilmaz@example.com",
                "Örnek Mahallesi, Test Sokak No:1"
        );

        return customer;
    }
}
