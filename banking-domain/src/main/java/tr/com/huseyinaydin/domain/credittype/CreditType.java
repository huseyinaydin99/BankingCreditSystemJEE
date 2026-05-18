package tr.com.huseyinaydin.domain.credittype;

import tr.com.huseyinaydin.domain.common.Entity;
import tr.com.huseyinaydin.domain.enums.CustomerType;
import jakarta.persistence.Column;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.OneToMany;
import jakarta.persistence.Table;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@jakarta.persistence.Entity
@Table(name = "CREDIT_TYPES")
public class CreditType extends Entity<UUID> {

    @Column(name = "NAME", nullable = false, length = 100)
    private String name;

    @Column(name = "DESCRIPTION", length = 500)
    private String description;

    @Enumerated(EnumType.STRING)
    @Column(name = "CUSTOMER_TYPE", nullable = false, length = 20)
    private CustomerType customerType;

    @Column(name = "MIN_AMOUNT", nullable = false, precision = 18, scale = 2)
    private BigDecimal minAmount;

    @Column(name = "MAX_AMOUNT", nullable = false, precision = 18, scale = 2)
    private BigDecimal maxAmount;

    @Column(name = "MIN_TERM", nullable = false)
    private int minTerm;

    @Column(name = "MAX_TERM", nullable = false)
    private int maxTerm;

    @Column(name = "BASE_INTEREST_RATE", nullable = false, precision = 5, scale = 2)
    private BigDecimal baseInterestRate;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "PARENT_CREDIT_TYPE_ID")
    private CreditType parentCreditType;

    @OneToMany(mappedBy = "parentCreditType", fetch = FetchType.LAZY)
    private List<CreditType> subCreditTypes = new ArrayList<>();

    protected CreditType() {
        super();
    }

    public CreditType(String name, CustomerType customerType,
                      BigDecimal minAmount, BigDecimal maxAmount,
                      int minTerm, int maxTerm, BigDecimal baseInterestRate) {
        super();
        this.id = UUID.randomUUID();
        this.name = name;
        this.customerType = customerType;
        this.minAmount = minAmount;
        this.maxAmount = maxAmount;
        this.minTerm = minTerm;
        this.maxTerm = maxTerm;
        this.baseInterestRate = baseInterestRate;
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

    public BigDecimal getMinAmount() { return minAmount; }
    public void setMinAmount(BigDecimal minAmount) { this.minAmount = minAmount; }

    public BigDecimal getMaxAmount() { return maxAmount; }
    public void setMaxAmount(BigDecimal maxAmount) { this.maxAmount = maxAmount; }

    public int getMinTerm() { return minTerm; }
    public void setMinTerm(int minTerm) { this.minTerm = minTerm; }

    public int getMaxTerm() { return maxTerm; }
    public void setMaxTerm(int maxTerm) { this.maxTerm = maxTerm; }

    public BigDecimal getBaseInterestRate() { return baseInterestRate; }
    public void setBaseInterestRate(BigDecimal baseInterestRate) { this.baseInterestRate = baseInterestRate; }

    public CreditType getParentCreditType() { return parentCreditType; }
    public void setParentCreditType(CreditType parentCreditType) { this.parentCreditType = parentCreditType; }

    public List<CreditType> getSubCreditTypes() { return subCreditTypes; }
    public void setSubCreditTypes(List<CreditType> subCreditTypes) { this.subCreditTypes = subCreditTypes; }
}
