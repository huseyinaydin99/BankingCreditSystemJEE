package tr.com.huseyinaydin.domain.customer;






import java.time.LocalDate;
import java.util.UUID;




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
