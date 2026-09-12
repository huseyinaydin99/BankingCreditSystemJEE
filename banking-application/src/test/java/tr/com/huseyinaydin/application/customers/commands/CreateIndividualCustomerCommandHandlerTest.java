package tr.com.huseyinaydin.application.customers.commands;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import tr.com.huseyinaydin.application.customers.dtos.CreatedIndividualCustomerResponse;
import tr.com.huseyinaydin.application.customers.rules.IndividualCustomerBusinessRules;
import tr.com.huseyinaydin.application.ports.IMapper;
import tr.com.huseyinaydin.application.ports.IPasswordHashService;
import tr.com.huseyinaydin.application.ports.PasswordHash;
import tr.com.huseyinaydin.application.ports.IUnitOfWork;
import tr.com.huseyinaydin.domain.customer.IndividualCustomer;
import tr.com.huseyinaydin.domain.repositories.IApplicationUserRepository;
import tr.com.huseyinaydin.domain.repositories.IIndividualCustomerRepository;
import tr.com.huseyinaydin.domain.user.ApplicationUser;
import tr.com.huseyinaydin.sharedkernel.exception.BusinessException;
import tr.com.huseyinaydin.sharedkernel.exception.ConflictException;
import tr.com.huseyinaydin.domain.fixtures.IndividualCustomerTestFixture;

import java.time.LocalDate;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.BDDMockito.willThrow;
import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
public class CreateIndividualCustomerCommandHandlerTest {

    @Mock
    private IUnitOfWork uow;
    @Mock
    private IndividualCustomerBusinessRules businessRules;
    @Mock
    private IPasswordHashService passwordHashService;
    @Mock
    private IMapper mapper;

    @Mock
    private IIndividualCustomerRepository individualCustomerRepository;
    @Mock
    private IApplicationUserRepository applicationUserRepository;

    @InjectMocks
    private CreateIndividualCustomerCommand.Handler handler;

    @Test
    @DisplayName("Bireysel müşteri başarıyla oluşturulmalı")
    void shouldCreateIndividualCustomerSuccessfully() {
        // Arrange
        IndividualCustomer fixture = IndividualCustomerTestFixture.random();
        CreateIndividualCustomerCommand command = new CreateIndividualCustomerCommand(
                fixture.getFirstName(),
                fixture.getLastName(),
                fixture.getNationalId(),
                fixture.getDateOfBirth(),
                fixture.getMotherName(),
                fixture.getFatherName(),
                "5551234567",
                fixture.getEmail(),
                "Test Adres",
                "Password123*"
        );

        given(passwordHashService.createHash(command.password()))
                .willReturn(new PasswordHash(new byte[]{1, 2, 3}, new byte[]{4, 5, 6}));
        given(uow.individualCustomers()).willReturn(individualCustomerRepository);
        given(uow.applicationUsers()).willReturn(applicationUserRepository);

        // Act
        CreatedIndividualCustomerResponse response = handler.handle(command);

        // Assert
        assertThat(response).isNotNull();
        assertThat(response.firstName()).isEqualTo(command.firstName());
        assertThat(response.lastName()).isEqualTo(command.lastName());
        assertThat(response.nationalId()).isEqualTo(command.nationalId());
        assertThat(response.email()).isEqualTo(command.email());
        assertThat(response.message()).isEqualTo("Bireysel müşteri başarıyla oluşturuldu");

        ArgumentCaptor<IndividualCustomer> customerCaptor = ArgumentCaptor.forClass(IndividualCustomer.class);
        verify(individualCustomerRepository).save(customerCaptor.capture());
        IndividualCustomer savedCustomer = customerCaptor.getValue();

        assertThat(savedCustomer.getFirstName()).isEqualTo(command.firstName());
        assertThat(savedCustomer.getNationalId()).isEqualTo(command.nationalId());

        ArgumentCaptor<ApplicationUser> userCaptor = ArgumentCaptor.forClass(ApplicationUser.class);
        verify(applicationUserRepository).save(userCaptor.capture());
        ApplicationUser savedUser = userCaptor.getValue();

        assertThat(savedUser.getEmail()).isEqualTo(command.email());
        verify(uow).beginTransaction();
        verify(uow).commit();
    }

    @Test
    @DisplayName("Aynı T.C. kimlik numarası ile kayıt yapılmak istendiğinde hata fırlatmalı")
    void shouldThrowExceptionWhenNationalIdIsDuplicated() {
        // Arrange
        IndividualCustomer fixture = IndividualCustomerTestFixture.random();
        CreateIndividualCustomerCommand command = new CreateIndividualCustomerCommand(
                fixture.getFirstName(),
                fixture.getLastName(),
                fixture.getNationalId(),
                fixture.getDateOfBirth(),
                fixture.getMotherName(),
                fixture.getFatherName(),
                "5551234567",
                fixture.getEmail(),
                "Test Adres",
                "Password123*"
        );

        willThrow(new ConflictException("nationalId", command.nationalId(), "Bu TC Kimlik No zaten kayıtlı"))
                .given(businessRules).nationalIdCannotBeDuplicatedWhenInserted(command.nationalId());

        // Act & Assert
        assertThatThrownBy(() -> handler.handle(command))
                .isInstanceOf(ConflictException.class)
                .hasMessageContaining("Bu TC Kimlik No zaten kayıtlı");
    }
}
