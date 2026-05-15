package tr.com.huseyinaydin.domain.credittype;

import tr.com.huseyinaydin.domain.common.Entity;
import tr.com.huseyinaydin.domain.enums.CustomerType;
import jakarta.persistence.Column;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Table;
import jakarta.persistence.Transient;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@jakarta.persistence.Entity
@Table(name = "credit_types")
public class CreditType extends Entity<UUID> {

    @Column(name = "name", nullable = false, length = 100)
    private String name;

    @Column(name = "description", length = 500)
    private String description;

    @Enumerated(EnumType.STRING)
    @Column(name = "customer_type", nullable = false, length = 20)
    private CustomerType customerType;

    @Column(name = "min_amount", nullable = false, precision = 19, scale = 2)
    private BigDecimal minAmount;

    @Column(name = "max_amount", nullable = false, precision = 19, scale = 2)
    private BigDecimal maxAmount;

    @Column(name = "min_term", nullable = false)
    private int minTerm;

    @Column(name = "max_term", nullable = false)
    private int maxTerm;

    @Column(name = "base_interest_rate", nullable = false, precision = 5, scale = 2)
    private BigDecimal baseInterestRate;

    @Column(name = "parent_credit_type_id")
    private UUID parentCreditTypeId;

    @Transient
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
        return parentCreditTypeId != null;
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

    public UUID getParentCreditTypeId() { return parentCreditTypeId; }
    public void setParentCreditTypeId(UUID parentCreditTypeId) { this.parentCreditTypeId = parentCreditTypeId; }

    public List<CreditType> getSubCreditTypes() { return subCreditTypes; }
    public void setSubCreditTypes(List<CreditType> subCreditTypes) { this.subCreditTypes = subCreditTypes; }
}
