package tr.com.huseyinaydin.application.customers.commands;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import tr.com.huseyinaydin.application.customers.dtos.CreatedCorporateCustomerResponse;
import tr.com.huseyinaydin.application.customers.rules.CorporateCustomerBusinessRules;
import tr.com.huseyinaydin.application.ports.IMapper;
import tr.com.huseyinaydin.application.ports.IPasswordHashService;
import tr.com.huseyinaydin.application.ports.PasswordHash;
import tr.com.huseyinaydin.application.ports.IUnitOfWork;
import tr.com.huseyinaydin.domain.customer.CorporateCustomer;
import tr.com.huseyinaydin.domain.repositories.IApplicationUserRepository;
import tr.com.huseyinaydin.domain.repositories.ICorporateCustomerRepository;
import tr.com.huseyinaydin.domain.user.ApplicationUser;
import tr.com.huseyinaydin.sharedkernel.exception.ConflictException;
import tr.com.huseyinaydin.domain.fixtures.CorporateCustomerTestFixture;

import java.time.LocalDate;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.BDDMockito.given;
import static org.mockito.BDDMockito.willThrow;
import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
public class CreateCorporateCustomerCommandHandlerTest {

    @Mock
    private IUnitOfWork uow;
    @Mock
    private CorporateCustomerBusinessRules businessRules;
    @Mock
    private IPasswordHashService passwordHashService;
    @Mock
    private IMapper mapper;

    @Mock
    private ICorporateCustomerRepository corporateCustomerRepository;
    @Mock
    private IApplicationUserRepository applicationUserRepository;

    @InjectMocks
    private CreateCorporateCustomerCommand.Handler handler;

    @Test
    @DisplayName("Kurumsal müşteri başarıyla oluşturulmalı")
    void shouldCreateCorporateCustomerSuccessfully() {
        // Arrange
        CorporateCustomer fixture = CorporateCustomerTestFixture.random();
        CreateCorporateCustomerCommand command = new CreateCorporateCustomerCommand(
                fixture.getCompanyName(),
                fixture.getTaxNumber(),
                fixture.getTradeRegistrationNumber(),
                fixture.getTaxOffice(),
                fixture.getCompanyRegistrationNumber(),
                fixture.getAuthorizedPersonName(),
                fixture.getCompanyFoundationDate(),
                fixture.getPhoneNumber(),
                fixture.getEmail(),
                fixture.getAddress(),
                "Password123*"
        );

        given(passwordHashService.createHash(command.password()))
                .willReturn(new PasswordHash(new byte[]{1, 2, 3}, new byte[]{4, 5, 6}));
        given(uow.corporateCustomers()).willReturn(corporateCustomerRepository);
        given(uow.applicationUsers()).willReturn(applicationUserRepository);

        // Act
        CreatedCorporateCustomerResponse response = handler.handle(command);

        // Assert
        assertThat(response).isNotNull();
        assertThat(response.companyName()).isEqualTo(command.companyName());
        assertThat(response.taxNumber()).isEqualTo(command.taxNumber());
        assertThat(response.message()).isEqualTo("Kurumsal müşteri başarıyla oluşturuldu");

        ArgumentCaptor<CorporateCustomer> customerCaptor = ArgumentCaptor.forClass(CorporateCustomer.class);
        verify(corporateCustomerRepository).save(customerCaptor.capture());
        CorporateCustomer savedCustomer = customerCaptor.getValue();

        assertThat(savedCustomer.getCompanyName()).isEqualTo(command.companyName());
        assertThat(savedCustomer.getTaxNumber()).isEqualTo(command.taxNumber());

        ArgumentCaptor<ApplicationUser> userCaptor = ArgumentCaptor.forClass(ApplicationUser.class);
        verify(applicationUserRepository).save(userCaptor.capture());
        ApplicationUser savedUser = userCaptor.getValue();

        assertThat(savedUser.getEmail()).isEqualTo(command.email());
        verify(uow).beginTransaction();
        verify(uow).commit();
    }

    @Test
    @DisplayName("Aynı vergi numarası ile kayıt yapılmak istendiğinde hata fırlatmalı")
    void shouldThrowExceptionWhenTaxNumberIsDuplicated() {
        // Arrange
        CorporateCustomer fixture = CorporateCustomerTestFixture.random();
        CreateCorporateCustomerCommand command = new CreateCorporateCustomerCommand(
                fixture.getCompanyName(),
                fixture.getTaxNumber(),
                fixture.getTradeRegistrationNumber(),
                fixture.getTaxOffice(),
                fixture.getCompanyRegistrationNumber(),
                fixture.getAuthorizedPersonName(),
                fixture.getCompanyFoundationDate(),
                fixture.getPhoneNumber(),
                fixture.getEmail(),
                fixture.getAddress(),
                "Password123*"
        );

        willThrow(new ConflictException("taxNumber", command.taxNumber(), "Bu Vergi No zaten kayıtlı"))
                .given(businessRules).taxNumberCannotBeDuplicatedWhenInserted(command.taxNumber());

        // Act & Assert
        assertThatThrownBy(() -> handler.handle(command))
                .isInstanceOf(ConflictException.class)
                .hasMessageContaining("Bu Vergi No zaten kayıtlı");
    }
}
