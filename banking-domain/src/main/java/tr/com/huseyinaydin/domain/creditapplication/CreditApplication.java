package tr.com.huseyinaydin.domain.creditapplication;

import tr.com.huseyinaydin.domain.common.Entity;
import tr.com.huseyinaydin.domain.customer.Customer;
import tr.com.huseyinaydin.domain.enums.CreditApplicationStatus;
import tr.com.huseyinaydin.domain.valueobjects.Money;










import java.math.BigDecimal;
import java.math.MathContext;
import java.math.RoundingMode;
import java.util.UUID;



public class CreditApplication extends Entity<UUID> {

    private Customer customer;

    private UUID creditTypeId;

    private BigDecimal requestedAmount;

    private int requestedTerm;

    private Money approvedAmount;

    private Integer approvedTerm;

    private BigDecimal interestRate;

    private Money monthlyPayment;

    private Money totalPayment;

    private CreditApplicationStatus status;

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


    public void moveToReview() {
        this.status = CreditApplicationStatus.UNDER_REVIEW;
    }

    public void updateRequest(UUID creditTypeId, BigDecimal requestedAmount, int requestedTerm) {
        this.creditTypeId = creditTypeId;
        this.requestedAmount = requestedAmount;
        this.requestedTerm = requestedTerm;
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
