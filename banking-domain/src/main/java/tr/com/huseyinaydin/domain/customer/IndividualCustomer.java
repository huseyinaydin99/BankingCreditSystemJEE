package tr.com.huseyinaydin.domain.customer;

import java.time.LocalDate;
import java.util.UUID;
import tr.com.huseyinaydin.domain.events.IndividualCustomerCreatedEvent;

public class IndividualCustomer extends Customer {

    private String firstName;
    private String lastName;
    private String nationalId;
    private LocalDate dateOfBirth;
    private String motherName;
    private String fatherName;

    protected IndividualCustomer() {
        super();
    }

    public IndividualCustomer(String firstName, String lastName, String nationalId, String email) {
        super();
        this.id = UUID.randomUUID();
        if (firstName == null || firstName.isBlank()) throw new IllegalArgumentException("Ad boş olamaz");
        if (lastName == null || lastName.isBlank()) throw new IllegalArgumentException("Soyad boş olamaz");
        if (nationalId == null || !nationalId.matches("\\d{11}")) throw new IllegalArgumentException("TC kimlik numarası 11 rakamdan oluşmalıdır");
        
        this.firstName = firstName;
        this.lastName = lastName;
        this.nationalId = nationalId;
        super.updateContactInfo(null, email, null);
        addDomainEvent(new IndividualCustomerCreatedEvent(this.id, this.firstName, this.lastName, this.nationalId, this.getEmail()));
    }

    public void updatePersonalInfo(String firstName, String lastName, LocalDate dateOfBirth, String motherName, String fatherName) {
        if (firstName == null || firstName.isBlank()) throw new IllegalArgumentException("Ad boş olamaz");
        if (lastName == null || lastName.isBlank()) throw new IllegalArgumentException("Soyad boş olamaz");
        this.firstName = firstName;
        this.lastName = lastName;
        this.dateOfBirth = dateOfBirth;
        this.motherName = motherName;
        this.fatherName = fatherName;
    }

    public String getFirstName() { return firstName; }
    public String getLastName() { return lastName; }
    public String getNationalId() { return nationalId; }
    public LocalDate getDateOfBirth() { return dateOfBirth; }
    public String getMotherName() { return motherName; }
    public String getFatherName() { return fatherName; }

    @Override
    public String getFullName() {
        return firstName + " " + lastName;
    }
}
