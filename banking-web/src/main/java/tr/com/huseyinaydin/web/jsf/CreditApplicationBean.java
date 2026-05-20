package tr.com.huseyinaydin.web.jsf;

import jakarta.annotation.PostConstruct;
import jakarta.faces.view.ViewScoped;
import jakarta.inject.Named;
import tr.com.huseyinaydin.application.creditapplication.commands.CreateCreditApplicationCommand;
import tr.com.huseyinaydin.application.creditapplication.dtos.CreditApplicationResponse;
import tr.com.huseyinaydin.application.creditapplication.queries.GetListByCustomerCreditApplicationQuery;
import tr.com.huseyinaydin.application.cqrs.Mediator;
import tr.com.huseyinaydin.sharedkernel.exception.ApplicationException;
import tr.com.huseyinaydin.sharedkernel.pagination.Paginate;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Named("creditApplicationBean")
@ViewScoped
public class CreditApplicationBean extends AbstractManagedBean {

    private UUID currentCustomerId;
    private List<CreditApplicationResponse> applications = new ArrayList<>();

    // Create form fields
    private UUID creditTypeId;
    private BigDecimal requestedAmount;
    private int requestedTerm;

    private Mediator mediator;

    @PostConstruct
    public void init() {
        mediator = getBean(Mediator.class);
    }

    public void loadApplications(UUID customerId) {
        this.currentCustomerId = customerId;
        try {
            Paginate<CreditApplicationResponse> page = mediator.query(
                    new GetListByCustomerCreditApplicationQuery(customerId, 0, 20));
            applications = new ArrayList<>(page.getItems());
        } catch (Exception e) {
            log.error("Kredi başvuruları yüklenemedi", e);
            addErrorMessage("Kredi başvuruları yüklenemedi: " + e.getMessage());
        }
    }

    public void createApplication() {
        try {
            mediator.send(new CreateCreditApplicationCommand(
                    currentCustomerId, creditTypeId, requestedAmount, requestedTerm
            ));
            loadApplications(currentCustomerId);
            clearForm();
            addSuccessMessage("Kredi başvurusu başarıyla oluşturuldu");
        } catch (ApplicationException e) {
            addErrorMessage(e.getMessage());
        }
    }

    private void clearForm() {
        creditTypeId = null;
        requestedAmount = null;
        requestedTerm = 0;
    }

    // ── Getters / Setters ────────────────────────────────────────────────────

    public UUID getCurrentCustomerId() { return currentCustomerId; }
    public void setCurrentCustomerId(UUID currentCustomerId) {
        this.currentCustomerId = currentCustomerId;
    }
    public List<CreditApplicationResponse> getApplications() { return applications; }
    public UUID getCreditTypeId() { return creditTypeId; }
    public void setCreditTypeId(UUID creditTypeId) { this.creditTypeId = creditTypeId; }
    public BigDecimal getRequestedAmount() { return requestedAmount; }
    public void setRequestedAmount(BigDecimal requestedAmount) {
        this.requestedAmount = requestedAmount;
    }
    public int getRequestedTerm() { return requestedTerm; }
    public void setRequestedTerm(int requestedTerm) { this.requestedTerm = requestedTerm; }
}
