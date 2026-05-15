package tr.com.huseyinaydin.domain.creditapplication;

import tr.com.huseyinaydin.domain.common.Entity;
import tr.com.huseyinaydin.domain.enums.CreditApplicationStatus;
import jakarta.persistence.Column;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Table;

import java.math.BigDecimal;
import java.math.MathContext;
import java.math.RoundingMode;
import java.util.UUID;

@jakarta.persistence.Entity
@Table(name = "credit_applications")
public class CreditApplication extends Entity<UUID> {

    @Column(name = "customer_id", nullable = false)
    private UUID customerId;

    @Column(name = "credit_type_id", nullable = false)
    private UUID creditTypeId;

    @Column(name = "requested_amount", nullable = false, precision = 19, scale = 2)
    private BigDecimal requestedAmount;

    @Column(name = "requested_term", nullable = false)
    private int requestedTerm;

    @Column(name = "approved_amount", precision = 19, scale = 2)
    private BigDecimal approvedAmount;

    @Column(name = "approved_term")
    private int approvedTerm;

    @Column(name = "interest_rate", precision = 5, scale = 2)
    private BigDecimal interestRate;

    @Column(name = "monthly_payment", precision = 19, scale = 2)
    private BigDecimal monthlyPayment;

    @Column(name = "total_payment", precision = 19, scale = 2)
    private BigDecimal totalPayment;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false, length = 20)
    private CreditApplicationStatus status;

    @Column(name = "rejection_reason", length = 500)
    private String rejectionReason;

    protected CreditApplication() {
        super();
    }

    public CreditApplication(UUID customerId, UUID creditTypeId,
                             BigDecimal requestedAmount, int requestedTerm) {
        super();
        this.id = UUID.randomUUID();
        this.customerId = customerId;
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
        // r = annualRate / 1200
        // monthly = amount * r * (1+r)^term / ((1+r)^term - 1)
        // total = monthly * term
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

    public UUID getCustomerId() { return customerId; }
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
