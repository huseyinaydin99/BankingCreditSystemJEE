package tr.com.huseyinaydin.domain.customer;

import java.time.LocalDate;
import java.util.UUID;
import tr.com.huseyinaydin.domain.events.CorporateCustomerCreatedEvent;

public class CorporateCustomer extends Customer {

    private String companyName;
    private String taxNumber;
    private String taxOffice;
    private String companyRegistrationNumber;
    private String tradeRegistrationNumber;
    private String authorizedPersonName;
    private LocalDate companyFoundationDate;

    protected CorporateCustomer() {
        super();
    }

    public CorporateCustomer(String companyName, String taxNumber, String email) {
        super();
        this.id = UUID.randomUUID();
        setCompanyName(companyName);
        setTaxNumber(taxNumber);
        setEmail(email);
        addDomainEvent(new CorporateCustomerCreatedEvent(this.id, this.companyName, this.taxNumber, this.getEmail()));
    }

    public String getCompanyName() { return companyName; }

    public void setCompanyName(String companyName) {
        if (companyName == null || companyName.isBlank()) {
            throw new IllegalArgumentException("Şirket adı boş olamaz");
        }
        this.companyName = companyName;
    }

    public String getTaxNumber() { return taxNumber; }

    public void setTaxNumber(String taxNumber) {
        if (taxNumber == null || !taxNumber.matches("\\d{10}")) {
            throw new IllegalArgumentException("Vergi numarası 10 rakamdan oluşmalıdır");
        }
        this.taxNumber = taxNumber;
    }

    public String getTaxOffice() { return taxOffice; }
    public void setTaxOffice(String taxOffice) { this.taxOffice = taxOffice; }

    public String getCompanyRegistrationNumber() { return companyRegistrationNumber; }
    public void setCompanyRegistrationNumber(String number) { this.companyRegistrationNumber = number; }

    public String getTradeRegistrationNumber() { return tradeRegistrationNumber; }

    public void setTradeRegistrationNumber(String tradeRegistrationNumber) {
        if (tradeRegistrationNumber == null || !tradeRegistrationNumber.matches("\\d{4,16}")) {
            throw new IllegalArgumentException("Ticaret Sicil No 4-16 rakamdan oluşmalıdır");
        }
        this.tradeRegistrationNumber = tradeRegistrationNumber;
    }

    public String getAuthorizedPersonName() { return authorizedPersonName; }
    public void setAuthorizedPersonName(String name) { this.authorizedPersonName = name; }

    public LocalDate getCompanyFoundationDate() { return companyFoundationDate; }
    public void setCompanyFoundationDate(LocalDate date) { this.companyFoundationDate = date; }

    @Override
    public String getFullName() {
        return companyName;
    }
}
