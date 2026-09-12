package tr.com.huseyinaydin.application.creditapplication.commands;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import tr.com.huseyinaydin.application.creditapplication.rules.CreditApplicationBusinessRules;
import tr.com.huseyinaydin.application.ports.IUnitOfWork;
import tr.com.huseyinaydin.domain.creditapplication.CreditApplication;
import tr.com.huseyinaydin.domain.credittype.CreditType;
import tr.com.huseyinaydin.domain.customer.IndividualCustomer;
import tr.com.huseyinaydin.domain.repositories.ICreditApplicationRepository;
import tr.com.huseyinaydin.domain.repositories.IIndividualCustomerRepository;
import tr.com.huseyinaydin.sharedkernel.exception.BusinessException;
import tr.com.huseyinaydin.domain.fixtures.CreditTypeTestFixture;
import tr.com.huseyinaydin.domain.fixtures.IndividualCustomerTestFixture;
import tr.com.huseyinaydin.application.common.BankingErrorCodes;

import java.math.BigDecimal;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.BDDMockito.willThrow;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.doNothing;

@ExtendWith(MockitoExtension.class)
public class CreateCreditApplicationCommandHandlerTest {

    @Mock
    private IUnitOfWork uow;
    @Mock
    private CreditApplicationBusinessRules businessRules;
    @Mock
    private ICreditApplicationRepository creditApplicationRepository;
    @Mock
    private IIndividualCustomerRepository individualCustomerRepository;

    @InjectMocks
    private CreateCreditApplicationCommand.Handler handler;

    @Test
    @DisplayName("Kredi başvurusu başarıyla oluşturulmalı")
    void shouldCreateCreditApplicationSuccessfully() {
        // Arrange
        IndividualCustomer customer = IndividualCustomerTestFixture.random();
        CreditType creditType = CreditTypeTestFixture.random();
        
        CreateCreditApplicationCommand command = new CreateCreditApplicationCommand(
                customer.getId(),
                creditType.getId(),
                new BigDecimal("50000"),
                12
        );

        given(businessRules.creditTypeMustExist(command.creditTypeId())).willReturn(creditType);
        doNothing().when(businessRules).amountMustBeInRange(command.requestedAmount(), creditType);
        doNothing().when(businessRules).termMustBeInRange(command.requestedTerm(), creditType);
        
        given(uow.individualCustomers()).willReturn(individualCustomerRepository);
        given(individualCustomerRepository.findById(customer.getId())).willReturn(Optional.of(customer));
        given(uow.creditApplications()).willReturn(creditApplicationRepository);

        // Act
        CreateCreditApplicationCommand.Response response = handler.handle(command);

        // Assert
        assertThat(response).isNotNull();
        assertThat(response.customerId()).isEqualTo(command.customerId());
        assertThat(response.creditTypeId()).isEqualTo(command.creditTypeId());
        assertThat(response.requestedAmount()).isEqualTo(command.requestedAmount());
        assertThat(response.requestedTerm()).isEqualTo(command.requestedTerm());

        ArgumentCaptor<CreditApplication> captor = ArgumentCaptor.forClass(CreditApplication.class);
        verify(creditApplicationRepository).save(captor.capture());
        CreditApplication savedApplication = captor.getValue();

        assertThat(savedApplication.getCustomerId()).isEqualTo(customer.getId());
        assertThat(savedApplication.getRequestedAmount()).isEqualTo(command.requestedAmount());
        
        verify(uow).beginTransaction();
        verify(uow).commit();
    }

    @Test
    @DisplayName("Talep edilen miktar limitler dışında ise hata fırlatmalı")
    void shouldThrowExceptionWhenAmountIsOutOfRange() {
        // Arrange
        IndividualCustomer customer = IndividualCustomerTestFixture.random();
        CreditType creditType = CreditTypeTestFixture.random();
        
        CreateCreditApplicationCommand command = new CreateCreditApplicationCommand(
                customer.getId(),
                creditType.getId(),
                new BigDecimal("5000000000"), // Too large
                12
        );

        given(businessRules.creditTypeMustExist(command.creditTypeId())).willReturn(creditType);
        willThrow(new BusinessException("Talep edilen miktar bu kredi türü için izin verilen aralığın dışındadır", BankingErrorCodes.AMOUNT_OUT_OF_RANGE))
                .given(businessRules).amountMustBeInRange(command.requestedAmount(), creditType);

        // Act & Assert
        assertThatThrownBy(() -> handler.handle(command))
                .isInstanceOf(BusinessException.class)
                .hasMessageContaining("Talep edilen miktar bu kredi türü için izin verilen aralığın dışındadır");
    }
}
