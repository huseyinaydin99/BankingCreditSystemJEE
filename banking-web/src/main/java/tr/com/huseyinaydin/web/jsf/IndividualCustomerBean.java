package tr.com.huseyinaydin.web.jsf;

import jakarta.annotation.PostConstruct;
import jakarta.faces.view.ViewScoped;
import jakarta.inject.Named;
import tr.com.huseyinaydin.application.cqrs.Mediator;
import tr.com.huseyinaydin.application.customers.commands.CreateIndividualCustomerCommand;
import tr.com.huseyinaydin.application.customers.commands.DeleteIndividualCustomerCommand;
import tr.com.huseyinaydin.application.customers.dtos.IndividualCustomerResponse;
import tr.com.huseyinaydin.application.customers.queries.GetListIndividualCustomerQuery;
import tr.com.huseyinaydin.sharedkernel.exception.ApplicationException;
import tr.com.huseyinaydin.sharedkernel.pagination.Paginate;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Named("individualCustomerBean")
@ViewScoped
public class IndividualCustomerBean extends AbstractManagedBean {

    private List<IndividualCustomerResponse> customers = new ArrayList<>();
    private IndividualCustomerResponse selected;
    private String searchNationalId;

    // Create form fields
    private String firstName;
    private String lastName;
    private String nationalId;
    private LocalDate dateOfBirth;
    private String motherName;
    private String fatherName;
    private String phoneNumber;
    private String email;
    private String address;
    private String password;

    private Mediator mediator;

    @PostConstruct
    public void init() {
        mediator = getBean(Mediator.class);
        loadCustomers();
    }

    public void loadCustomers() {
        try {
            Paginate<IndividualCustomerResponse> page =
                    mediator.query(new GetListIndividualCustomerQuery(0, 20));
            customers = new ArrayList<>(page.getItems());
        } catch (Exception e) {
            log.error("Müşteriler yüklenemedi", e);
            addErrorMessage("Müşteriler yüklenemedi: " + e.getMessage());
        }
    }

    public void prepareCreate() {
        clearForm();
        selected = null;
    }

    public void save() {
        try {
            mediator.send(new CreateIndividualCustomerCommand(
                    firstName, lastName, nationalId, dateOfBirth,
                    motherName, fatherName, phoneNumber, email, address, password
            ));
            clearForm();
            loadCustomers();
            addSuccessMessage("Bireysel müşteri başarıyla oluşturuldu");
        } catch (ApplicationException e) {
            addErrorMessage(e.getMessage());
        }
    }

    public void delete(UUID id) {
        try {
            mediator.send(new DeleteIndividualCustomerCommand(id));
            loadCustomers();
            addSuccessMessage("Müşteri başarıyla silindi");
        } catch (ApplicationException e) {
            addErrorMessage(e.getMessage());
        }
    }

    public void searchByNationalId() {
        if (searchNationalId == null || searchNationalId.isBlank()) {
            loadCustomers();
            return;
        }
        customers = customers.stream()
                .filter(c -> searchNationalId.equals(c.nationalId()))
                .toList();
    }

    private void clearForm() {
        firstName = lastName = nationalId = motherName = fatherName =
                phoneNumber = email = address = password = null;
        dateOfBirth = null;
    }

    // ── Getters / Setters ────────────────────────────────────────────────────

    public List<IndividualCustomerResponse> getCustomers() { return customers; }
    public IndividualCustomerResponse getSelected() { return selected; }
    public void setSelected(IndividualCustomerResponse selected) { this.selected = selected; }
    public String getSearchNationalId() { return searchNationalId; }
    public void setSearchNationalId(String searchNationalId) { this.searchNationalId = searchNationalId; }
    public String getFirstName() { return firstName; }
    public void setFirstName(String firstName) { this.firstName = firstName; }
    public String getLastName() { return lastName; }
    public void setLastName(String lastName) { this.lastName = lastName; }
    public String getNationalId() { return nationalId; }
    public void setNationalId(String nationalId) { this.nationalId = nationalId; }
    public LocalDate getDateOfBirth() { return dateOfBirth; }
    public void setDateOfBirth(LocalDate dateOfBirth) { this.dateOfBirth = dateOfBirth; }
    public String getMotherName() { return motherName; }
    public void setMotherName(String motherName) { this.motherName = motherName; }
    public String getFatherName() { return fatherName; }
    public void setFatherName(String fatherName) { this.fatherName = fatherName; }
    public String getPhoneNumber() { return phoneNumber; }
    public void setPhoneNumber(String phoneNumber) { this.phoneNumber = phoneNumber; }
    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }
    public String getAddress() { return address; }
    public void setAddress(String address) { this.address = address; }
    public String getPassword() { return password; }
    public void setPassword(String password) { this.password = password; }
}
