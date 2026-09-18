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

    public void updateDetails(String name, String description, Money minimumAmount, Money maximumAmount,
                              int minimumTermMonths, int maximumTermMonths, BigDecimal annualInterestRate) {
        this.name = name;
        this.description = description;
        this.minimumAmount = minimumAmount;
        this.maximumAmount = maximumAmount;
        this.minimumTermMonths = minimumTermMonths;
        this.maximumTermMonths = maximumTermMonths;
        this.annualInterestRate = annualInterestRate;
    }

    public void assignParent(CreditType parent) {
        this.parentCreditType = parent;
    }

    public void removeParent() {
        this.parentCreditType = null;
    }

    public void addSubCreditType(CreditType child) {
        this.subCreditTypes.add(child);
        child.assignParent(this);
    }

    public void updateDescription(String description) {
        this.description = description;
    }

    public boolean isSubType() {
        return parentCreditType != null;
    }

    public UUID getParentCreditTypeId() {
        return parentCreditType != null ? parentCreditType.getId() : null;
    }

    public String getName() { return name; }
    public String getDescription() { return description; }
    public CustomerType getCustomerType() { return customerType; }
    public Money getMinimumAmount() { return minimumAmount; }
    public Money getMaximumAmount() { return maximumAmount; }
    public int getMinimumTermMonths() { return minimumTermMonths; }
    public int getMaximumTermMonths() { return maximumTermMonths; }
    public BigDecimal getAnnualInterestRate() { return annualInterestRate; }
    public CreditType getParentCreditType() { return parentCreditType; }
    public Set<CreditType> getSubCreditTypes() { return subCreditTypes; }
}
