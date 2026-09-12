package tr.com.huseyinaydin.domain.fixtures;

import tr.com.huseyinaydin.domain.customer.CorporateCustomer;
import java.time.LocalDate;
import java.util.Random;

public class CorporateCustomerTestFixture {
    private String companyName = "Acme Corp";
    private String taxNumber = "1234567890";
    private String email = "contact@acmecorp.com";
    private String tradeRegistrationNumber = "123456";
    private String taxOffice = "Central";
    private String phoneNumber = "5559876543";

    public CorporateCustomerTestFixture withCompanyName(String companyName) {
        this.companyName = companyName;
        return this;
    }

    public CorporateCustomerTestFixture withTaxNumber(String taxNumber) {
        this.taxNumber = taxNumber;
        return this;
    }

    public CorporateCustomerTestFixture withEmail(String email) {
        this.email = email;
        return this;
    }

    public CorporateCustomer build() {
        CorporateCustomer customer = new CorporateCustomer(companyName, taxNumber, email);
        customer.setTradeRegistrationNumber(tradeRegistrationNumber);
        customer.setTaxOffice(taxOffice);
        customer.setPhoneNumber(phoneNumber);
        return customer;
    }

    public static CorporateCustomer random() {
        Random random = new Random();
        long randomTaxNumber = 1000000000L + (long)(random.nextDouble() * 9000000000L);
        return new CorporateCustomerTestFixture()
                .withCompanyName("Company " + random.nextInt(1000))
                .withTaxNumber(String.valueOf(randomTaxNumber))
                .withEmail("company" + random.nextInt(10000) + "@test.com")
                .build();
    }
}
