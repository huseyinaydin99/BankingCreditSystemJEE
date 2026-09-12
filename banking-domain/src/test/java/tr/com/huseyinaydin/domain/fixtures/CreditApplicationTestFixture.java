package tr.com.huseyinaydin.domain.fixtures;

import tr.com.huseyinaydin.domain.creditapplication.CreditApplication;
import tr.com.huseyinaydin.domain.customer.Customer;
import tr.com.huseyinaydin.domain.credittype.CreditType;

import java.math.BigDecimal;
import java.util.UUID;
import java.util.Random;

public class CreditApplicationTestFixture {
    private Customer customer = IndividualCustomerTestFixture.random();
    private UUID creditTypeId = UUID.randomUUID();
    private BigDecimal requestedAmount = new BigDecimal("10000");
    private int requestedTerm = 12;

    public CreditApplicationTestFixture withCustomer(Customer customer) { this.customer = customer; return this; }
    public CreditApplicationTestFixture withCreditTypeId(UUID creditTypeId) { this.creditTypeId = creditTypeId; return this; }
    public CreditApplicationTestFixture withRequestedAmount(BigDecimal requestedAmount) { this.requestedAmount = requestedAmount; return this; }
    public CreditApplicationTestFixture withRequestedTerm(int requestedTerm) { this.requestedTerm = requestedTerm; return this; }

    public CreditApplication build() {
        return new CreditApplication(customer, creditTypeId, requestedAmount, requestedTerm);
    }

    public static CreditApplication random() {
        Random random = new Random();
        return new CreditApplicationTestFixture()
                .withRequestedAmount(new BigDecimal(1000 + random.nextInt(99000)))
                .withRequestedTerm(6 + random.nextInt(30))
                .build();
    }
}
