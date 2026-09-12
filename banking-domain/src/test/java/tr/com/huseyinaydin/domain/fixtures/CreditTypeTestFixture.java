package tr.com.huseyinaydin.domain.fixtures;

import tr.com.huseyinaydin.domain.credittype.CreditType;
import tr.com.huseyinaydin.domain.enums.CustomerType;
import tr.com.huseyinaydin.domain.valueobjects.Money;

import java.math.BigDecimal;
import java.util.Random;

public class CreditTypeTestFixture {
    private String name = "Standard Personal Loan";
    private CustomerType customerType = CustomerType.INDIVIDUAL;
    private Money minimumAmount = Money.of(new BigDecimal("1000"), "TRY");
    private Money maximumAmount = Money.of(new BigDecimal("100000"), "TRY");
    private int minimumTermMonths = 6;
    private int maximumTermMonths = 36;
    private BigDecimal annualInterestRate = new BigDecimal("1.50");

    public CreditTypeTestFixture withName(String name) { this.name = name; return this; }
    public CreditTypeTestFixture withCustomerType(CustomerType customerType) { this.customerType = customerType; return this; }

    public CreditType build() {
        return new CreditType(name, customerType, minimumAmount, maximumAmount, minimumTermMonths, maximumTermMonths, annualInterestRate);
    }

    public static CreditType random() {
        Random random = new Random();
        return new CreditTypeTestFixture()
                .withName("Loan " + random.nextInt(1000))
                .withCustomerType(random.nextBoolean() ? CustomerType.INDIVIDUAL : CustomerType.CORPORATE)
                .build();
    }
}
