package tr.com.huseyinaydin.domain.credittype;

import tr.com.huseyinaydin.domain.common.Entity;
import tr.com.huseyinaydin.domain.enums.CustomerType;
import tr.com.huseyinaydin.domain.valueobjects.Money;
// import jakarta.persistence.Column;       — META-INF/orm/CreditType.xml ile eşleme sağlanmaktadır.
// import jakarta.persistence.Embedded;
// import jakarta.persistence.EnumType;
// import jakarta.persistence.Enumerated;
// import jakarta.persistence.FetchType;
// import jakarta.persistence.JoinColumn;
// import jakarta.persistence.ManyToOne;
// import jakarta.persistence.OneToMany;
// import jakarta.persistence.Table;

import java.math.BigDecimal;
import java.util.HashSet;
import java.util.Set;
import java.util.UUID;

// @jakarta.persistence.Entity
// @Table(name = "CREDIT_TYPES")
public class CreditType extends Entity<UUID> {

    // @Column(name = "NAME", nullable = false, length = 100)
    private String name;

    // @Column(name = "DESCRIPTION", length = 500)
    private String description;

    // @Enumerated(EnumType.STRING)
    // @Column(name = "CUSTOMER_TYPE", nullable = false, length = 20)
    private CustomerType customerType;

    // @Embedded — AMOUNT → MIN_AMOUNT, CURRENCY → MIN_CURRENCY
    private Money minimumAmount;

    // @Embedded — AMOUNT → MAX_AMOUNT, CURRENCY → MAX_CURRENCY
    private Money maximumAmount;

    // @Column(name = "MIN_TERM", nullable = false)
    private int minimumTermMonths;

    // @Column(name = "MAX_TERM", nullable = false)
    private int maximumTermMonths;

    // @Column(name = "BASE_INTEREST_RATE", nullable = false, precision = 5, scale = 2)
    private BigDecimal annualInterestRate;

    // @ManyToOne(fetch = FetchType.LAZY)
    // @JoinColumn(name = "PARENT_CREDIT_TYPE_ID")
    private CreditType parentCreditType;

    // @OneToMany(mappedBy = "parentCreditType", fetch = FetchType.LAZY)
    private Set<CreditType> subCreditTypes = new HashSet<>();

    protected CreditType() {
        super();
    }

    public CreditType(String name, CustomerType customerType,
                      Money minimumAmount, Money maximumAmount,
                      int minimumTermMonths, int maximumTermMonths, BigDecimal annualInterestRate) {
        super();
        this.id = UUID.randomUUID();
        this.name = name;
        this.customerType = customerType;
        this.minimumAmount = minimumAmount;
        this.maximumAmount = maximumAmount;
        this.minimumTermMonths = minimumTermMonths;
        this.maximumTermMonths = maximumTermMonths;
        this.annualInterestRate = annualInterestRate;
    }

    public boolean isSubType() {
        return parentCreditType != null;
    }

    public UUID getParentCreditTypeId() {
        return parentCreditType != null ? parentCreditType.getId() : null;
    }

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }

    public CustomerType getCustomerType() { return customerType; }
    public void setCustomerType(CustomerType customerType) { this.customerType = customerType; }

    public Money getMinimumAmount() { return minimumAmount; }
    public void setMinimumAmount(Money minimumAmount) { this.minimumAmount = minimumAmount; }

    public Money getMaximumAmount() { return maximumAmount; }
    public void setMaximumAmount(Money maximumAmount) { this.maximumAmount = maximumAmount; }

    public int getMinimumTermMonths() { return minimumTermMonths; }
    public void setMinimumTermMonths(int minimumTermMonths) { this.minimumTermMonths = minimumTermMonths; }

    public int getMaximumTermMonths() { return maximumTermMonths; }
    public void setMaximumTermMonths(int maximumTermMonths) { this.maximumTermMonths = maximumTermMonths; }

    public BigDecimal getAnnualInterestRate() { return annualInterestRate; }
    public void setAnnualInterestRate(BigDecimal annualInterestRate) { this.annualInterestRate = annualInterestRate; }

    public CreditType getParentCreditType() { return parentCreditType; }
    public void setParentCreditType(CreditType parentCreditType) { this.parentCreditType = parentCreditType; }

    public Set<CreditType> getSubCreditTypes() { return subCreditTypes; }
    public void setSubCreditTypes(Set<CreditType> subCreditTypes) { this.subCreditTypes = subCreditTypes; }
}
