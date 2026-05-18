package tr.com.huseyinaydin.domain.creditapplication;

import tr.com.huseyinaydin.domain.common.Entity;
import tr.com.huseyinaydin.domain.customer.Customer;
import tr.com.huseyinaydin.domain.enums.CreditApplicationStatus;
import jakarta.persistence.Column;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.ForeignKey;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;

import java.math.BigDecimal;
import java.math.MathContext;
import java.math.RoundingMode;
import java.util.UUID;

@jakarta.persistence.Entity
@Table(name = "CREDIT_APPLICATIONS")
public class CreditApplication extends Entity<UUID> {

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "CUSTOMER_ID", nullable = false,
                foreignKey = @ForeignKey(name = "FK_CREDIT_APP_CUSTOMER"))
    private Customer customer;

    @Column(name = "CREDIT_TYPE_ID", nullable = false)
    private UUID creditTypeId;

    @Column(name = "REQUESTED_AMOUNT", nullable = false, precision = 18, scale = 2)
    private BigDecimal requestedAmount;

    @Column(name = "REQUESTED_TERM", nullable = false)
    private int requestedTerm;

    @Column(name = "APPROVED_AMOUNT", precision = 18, scale = 2)
    private BigDecimal approvedAmount;

    @Column(name = "APPROVED_TERM")
    private int approvedTerm;

    @Column(name = "INTEREST_RATE", precision = 5, scale = 2)
    private BigDecimal interestRate;

    @Column(name = "MONTHLY_PAYMENT", precision = 18, scale = 2)
    private BigDecimal monthlyPayment;

    @Column(name = "TOTAL_PAYMENT", precision = 18, scale = 2)
    private BigDecimal totalPayment;

    @Enumerated(EnumType.ORDINAL)
    @Column(name = "STATUS_CODE", nullable = false)
    private CreditApplicationStatus status;

    @Column(name = "REJECTION_REASON", length = 500)
    private String rejectionReason;

    protected CreditApplication() {
        super();
    }

    public CreditApplication(Customer customer, UUID creditTypeId,
                             BigDecimal requestedAmount, int requestedTerm) {
        super();
        this.id = UUID.randomUUID();
        this.customer = customer;
        this.creditTypeId = creditTypeId;
        this.requestedAmount = requestedAmount;
        this.requestedTerm = requestedTerm;
        this.status = CreditApplicationStatus.PENDING;
    }

    public void approve(BigDecimal rate) {
        if (status != CreditApplicationStatus.PENDING) {
            throw new IllegalStateException("Yalnızca PENDING durumundaki başvurular onaylanabilir");
        }
        this.interestRate = rate;
        this.approvedAmount = requestedAmount;
        this.approvedTerm = requestedTerm;
        calculatePayments(approvedAmount, approvedTerm, rate);
        this.status = CreditApplicationStatus.APPROVED;
    }

    public void reject(String reason) {
        if (status != CreditApplicationStatus.PENDING) {
            throw new IllegalStateException("Yalnızca PENDING durumundaki başvurular reddedilebilir");
        }
        this.rejectionReason = reason;
        this.status = CreditApplicationStatus.REJECTED;
    }

    public void calculatePayments(BigDecimal amount, int term, BigDecimal annualRate) {
        MathContext mc = new MathContext(15, RoundingMode.HALF_UP);
        BigDecimal r = annualRate.divide(BigDecimal.valueOf(1200), mc);
        BigDecimal onePlusR = BigDecimal.ONE.add(r, mc);
        BigDecimal onePlusRPowTerm = onePlusR.pow(term, mc);
        BigDecimal numerator = amount.multiply(r, mc).multiply(onePlusRPowTerm, mc);
        BigDecimal denominator = onePlusRPowTerm.subtract(BigDecimal.ONE, mc);
        this.monthlyPayment = numerator.divide(denominator, 2, RoundingMode.HALF_UP);
        this.totalPayment = monthlyPayment.multiply(BigDecimal.valueOf(term))
                                          .setScale(2, RoundingMode.HALF_UP);
    }

    public Customer getCustomer() { return customer; }
    public UUID getCustomerId() { return customer != null ? customer.getId() : null; }
    public UUID getCreditTypeId() { return creditTypeId; }
    public BigDecimal getRequestedAmount() { return requestedAmount; }
    public int getRequestedTerm() { return requestedTerm; }
    public BigDecimal getApprovedAmount() { return approvedAmount; }
    public int getApprovedTerm() { return approvedTerm; }
    public BigDecimal getInterestRate() { return interestRate; }
    public BigDecimal getMonthlyPayment() { return monthlyPayment; }
    public BigDecimal getTotalPayment() { return totalPayment; }
    public CreditApplicationStatus getStatus() { return status; }
    public String getRejectionReason() { return rejectionReason; }
}
