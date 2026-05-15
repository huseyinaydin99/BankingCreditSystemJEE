package tr.com.huseyinaydin.domain.customer;

import jakarta.persistence.Column;
import jakarta.persistence.DiscriminatorValue;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;

import java.time.LocalDate;
import java.util.UUID;

@Entity
@Table(name = "individual_customers")
@DiscriminatorValue("INDIVIDUAL")
public class IndividualCustomer extends Customer {

    @Column(name = "first_name", nullable = false, length = 100)
    private String firstName;

    @Column(name = "last_name", nullable = false, length = 100)
    private String lastName;

    @Column(name = "national_id", nullable = false, unique = true, length = 11)
    private String nationalId;

    @Column(name = "date_of_birth")
    private LocalDate dateOfBirth;

    @Column(name = "mother_name", length = 100)
    private String motherName;

    @Column(name = "father_name", length = 100)
    private String fatherName;

    protected IndividualCustomer() {
        super();
    }

    public IndividualCustomer(String firstName, String lastName, String nationalId, String email) {
        super();
        this.id = UUID.randomUUID();
        setFirstName(firstName);
        setLastName(lastName);
        setNationalId(nationalId);
        setEmail(email);
    }

    public String getFirstName() { return firstName; }

    public void setFirstName(String firstName) {
        if (firstName == null || firstName.isBlank()) {
            throw new IllegalArgumentException("Ad boş olamaz");
        }
        this.firstName = firstName;
    }

    public String getLastName() { return lastName; }

    public void setLastName(String lastName) {
        if (lastName == null || lastName.isBlank()) {
            throw new IllegalArgumentException("Soyad boş olamaz");
        }
        this.lastName = lastName;
    }

    public String getNationalId() { return nationalId; }

    public void setNationalId(String nationalId) {
        if (nationalId == null || !nationalId.matches("\\d{11}")) {
            throw new IllegalArgumentException("TC kimlik numarası 11 rakamdan oluşmalıdır");
        }
        this.nationalId = nationalId;
    }

    public LocalDate getDateOfBirth() { return dateOfBirth; }
    public void setDateOfBirth(LocalDate dateOfBirth) { this.dateOfBirth = dateOfBirth; }

    public String getMotherName() { return motherName; }
    public void setMotherName(String motherName) { this.motherName = motherName; }

    public String getFatherName() { return fatherName; }
    public void setFatherName(String fatherName) { this.fatherName = fatherName; }

    @Override
    public String getFullName() {
        return firstName + " " + lastName;
    }
}
