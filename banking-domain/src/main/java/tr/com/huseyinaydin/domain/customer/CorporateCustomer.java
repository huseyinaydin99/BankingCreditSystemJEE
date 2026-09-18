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

    public CorporateCustomer(String companyName, String taxNumber, String email, byte[] passwordHash, byte[] passwordSalt) {
        super();
        this.id = UUID.randomUUID();
        if (companyName == null || companyName.isBlank()) throw new IllegalArgumentException("Şirket adı boş olamaz");
        if (taxNumber == null || !taxNumber.matches("\\d{10}")) throw new IllegalArgumentException("Vergi numarası 10 rakamdan oluşmalıdır");
        
        this.companyName = companyName;
        this.taxNumber = taxNumber;
        super.updateContactInfo(null, email, null);
        addDomainEvent(new CorporateCustomerCreatedEvent(this.id, this.companyName, this.taxNumber, this.getEmail(), passwordHash, passwordSalt));
    }

    public void updateCompanyInfo(String companyName, String taxOffice, String companyRegistrationNumber, String tradeRegistrationNumber, String authorizedPersonName, LocalDate companyFoundationDate) {
        if (companyName == null || companyName.isBlank()) throw new IllegalArgumentException("Şirket adı boş olamaz");
        if (tradeRegistrationNumber != null && !tradeRegistrationNumber.isBlank() && !tradeRegistrationNumber.matches("\\d{4,16}")) {
            throw new IllegalArgumentException("Ticaret Sicil No 4-16 rakamdan oluşmalıdır");
        }
        this.companyName = companyName;
        this.taxOffice = taxOffice;
        this.companyRegistrationNumber = companyRegistrationNumber;
        this.tradeRegistrationNumber = tradeRegistrationNumber;
        this.authorizedPersonName = authorizedPersonName;
        this.companyFoundationDate = companyFoundationDate;
    }

    public String getCompanyName() { return companyName; }
    public String getTaxNumber() { return taxNumber; }
    public String getTaxOffice() { return taxOffice; }
    public String getCompanyRegistrationNumber() { return companyRegistrationNumber; }
    public String getTradeRegistrationNumber() { return tradeRegistrationNumber; }
    public String getAuthorizedPersonName() { return authorizedPersonName; }
    public LocalDate getCompanyFoundationDate() { return companyFoundationDate; }

    @Override
    public String getFullName() {
        return companyName;
    }
}
