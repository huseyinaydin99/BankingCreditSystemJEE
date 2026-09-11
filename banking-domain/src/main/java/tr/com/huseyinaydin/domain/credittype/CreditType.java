package tr.com.huseyinaydin.domain.credittype;

import tr.com.huseyinaydin.domain.common.Entity;
import tr.com.huseyinaydin.domain.enums.CustomerType;
import tr.com.huseyinaydin.domain.valueobjects.Money;










import java.math.BigDecimal;
import java.util.HashSet;
import java.util.Set;
import java.util.UUID;



public class CreditType extends Entity<UUID> {

    private String name;

    private String description;

    private CustomerType customerType;

    private Money minimumAmount;

    private Money maximumAmount;

    private int minimumTermMonths;

    private int maximumTermMonths;

    private BigDecimal annualInterestRate;

    private CreditType parentCreditType;

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
