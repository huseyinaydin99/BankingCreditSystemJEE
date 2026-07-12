package tr.com.huseyinaydin.domain.customer;

// import jakarta.persistence.Column;           — META-INF/orm/CorporateCustomer.xml ile eşleme sağlanmaktadır.
// import jakarta.persistence.DiscriminatorValue;
// import jakarta.persistence.Entity;
// import jakarta.persistence.Table;

import java.time.LocalDate;
import java.util.UUID;

// @Entity
// @Table(name = "CORPORATE_CUSTOMERS")
// @DiscriminatorValue("2")
public class CorporateCustomer extends Customer {

    // @Column(name = "COMPANY_NAME", nullable = false, length = 100)
    private String companyName;

    // @Column(name = "TAX_NUMBER", nullable = false, unique = true, length = 10)
    private String taxNumber;

    // @Column(name = "TAX_OFFICE", length = 100)
    private String taxOffice;

    // @Column(name = "COMPANY_REGISTRATION_NUMBER", length = 50)
    private String companyRegistrationNumber;

    // @Column(name = "TRADE_REGISTRATION_NUMBER", nullable = false, unique = true, length = 20)
    // Ticaret Sicil No — companyRegistrationNumber'dan farklı, benzersiz ve zorunlu alan.
    private String tradeRegistrationNumber;

    // @Column(name = "AUTHORIZED_PERSON_NAME", length = 200)
    private String authorizedPersonName;

    // @Column(name = "COMPANY_FOUNDATION_DATE")
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
