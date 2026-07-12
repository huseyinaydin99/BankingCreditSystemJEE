package tr.com.huseyinaydin.domain.creditapplication;

import tr.com.huseyinaydin.domain.common.Entity;
import tr.com.huseyinaydin.domain.customer.Customer;
import tr.com.huseyinaydin.domain.enums.CreditApplicationStatus;
import tr.com.huseyinaydin.domain.valueobjects.Money;
// import jakarta.persistence.Column;       — META-INF/orm/CreditApplication.xml ile eşleme sağlanmaktadır.
// import jakarta.persistence.Embedded;
// import jakarta.persistence.EnumType;
// import jakarta.persistence.Enumerated;
// import jakarta.persistence.FetchType;
// import jakarta.persistence.ForeignKey;
// import jakarta.persistence.JoinColumn;
// import jakarta.persistence.ManyToOne;
// import jakarta.persistence.Table;

import java.math.BigDecimal;
import java.math.MathContext;
import java.math.RoundingMode;
import java.util.UUID;

// @jakarta.persistence.Entity
// @Table(name = "CREDIT_APPLICATIONS")
public class CreditApplication extends Entity<UUID> {

    // @ManyToOne(fetch = FetchType.LAZY)
    // @JoinColumn(name = "CUSTOMER_ID", nullable = false,
    //             foreignKey = @ForeignKey(name = "FK_CREDIT_APP_CUSTOMER"))
    private Customer customer;

    // @Column(name = "CREDIT_TYPE_ID", nullable = false)
    private UUID creditTypeId;

    // @Column(name = "REQUESTED_AMOUNT", nullable = false, precision = 18, scale = 2)
    private BigDecimal requestedAmount;

    // @Column(name = "REQUESTED_TERM", nullable = false)
    private int requestedTerm;

    // @Embedded — AMOUNT → APPROVED_AMOUNT, CURRENCY → APPROVED_CURRENCY (onayda dolar)
    private Money approvedAmount;

    // @Column(name = "APPROVED_TERM")  — nullable olduğu için Integer
    private Integer approvedTerm;

    // @Column(name = "INTEREST_RATE", precision = 5, scale = 2)
    private BigDecimal interestRate;

    // @Embedded — AMOUNT → MONTHLY_PAYMENT, CURRENCY → MONTHLY_PAYMENT_CURRENCY
    private Money monthlyPayment;

    // @Embedded — AMOUNT → TOTAL_PAYMENT, CURRENCY → TOTAL_PAYMENT_CURRENCY
    private Money totalPayment;

    // @Enumerated(EnumType.ORDINAL)
    // @Column(name = "STATUS_CODE", nullable = false)
    private CreditApplicationStatus status;

    // @Column(name = "REJECTION_REASON", length = 500)
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

    /*
     * Durum geçişleri. Geçiş meşruiyeti (hangi durumdan hangisine izinli olduğu)
     * uygulama katmanındaki CreditApplicationBusinessRules.statusTransitionMustBeValid
     * tarafından doğrulanır; bu nedenle entity metotları guard içermez, yalnızca
     * durumu ve ilgili alanları günceller.
     */

    public void moveToReview() {
        this.status = CreditApplicationStatus.UNDER_REVIEW;
    }

    public void approve(Money approvedAmount, Integer approvedTerm, BigDecimal annualInterestRate) {
        this.approvedAmount = approvedAmount;
        this.approvedTerm = approvedTerm;
        this.interestRate = annualInterestRate;
        calculatePayments(approvedAmount.getAmount(), approvedTerm, annualInterestRate,
                approvedAmount.getCurrency());
        this.status = CreditApplicationStatus.APPROVED;
    }

    public void reject(String reason) {
        this.rejectionReason = reason;
        this.status = CreditApplicationStatus.REJECTED;
    }

    public void cancel() {
        this.status = CreditApplicationStatus.CANCELLED;
    }

    /**
     * Anüite (eşit taksitli) ödeme planı:
     * M = P · [r(1+r)^n] / [(1+r)^n − 1], burada r = yıllık faiz / 12 (aylık, ondalık).
     * annualRate yüzde olarak verilir (ör. 12 → %12); r = annualRate / 1200.
     * Sonuçlar verilen para birimiyle Money olarak saklanır.
     */
    public void calculatePayments(BigDecimal amount, int term, BigDecimal annualRate, String currency) {
        MathContext mc = new MathContext(15, RoundingMode.HALF_UP);
        BigDecimal r = annualRate.divide(BigDecimal.valueOf(1200), mc);
        BigDecimal onePlusR = BigDecimal.ONE.add(r, mc);
        BigDecimal onePlusRPowTerm = onePlusR.pow(term, mc);
        BigDecimal numerator = amount.multiply(r, mc).multiply(onePlusRPowTerm, mc);
        BigDecimal denominator = onePlusRPowTerm.subtract(BigDecimal.ONE, mc);
        BigDecimal monthly = numerator.divide(denominator, 2, RoundingMode.HALF_UP);
        BigDecimal total = monthly.multiply(BigDecimal.valueOf(term))
                                  .setScale(2, RoundingMode.HALF_UP);
        this.monthlyPayment = Money.of(monthly, currency);
        this.totalPayment = Money.of(total, currency);
    }

    public Customer getCustomer() { return customer; }
    public UUID getCustomerId() { return customer != null ? customer.getId() : null; }
    public UUID getCreditTypeId() { return creditTypeId; }
    public BigDecimal getRequestedAmount() { return requestedAmount; }
    public int getRequestedTerm() { return requestedTerm; }
    public Money getApprovedAmount() { return approvedAmount; }
    public Integer getApprovedTerm() { return approvedTerm; }
    public BigDecimal getInterestRate() { return interestRate; }
    public Money getMonthlyPayment() { return monthlyPayment; }
    public Money getTotalPayment() { return totalPayment; }
    public CreditApplicationStatus getStatus() { return status; }
    public String getRejectionReason() { return rejectionReason; }
}
