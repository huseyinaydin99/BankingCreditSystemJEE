package tr.com.huseyinaydin.application.credittype.commands;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import tr.com.huseyinaydin.application.credittype.rules.CreditTypeBusinessRules;
import tr.com.huseyinaydin.application.ports.IUnitOfWork;
import tr.com.huseyinaydin.domain.credittype.CreditType;
import tr.com.huseyinaydin.domain.repositories.ICreditTypeRepository;
import tr.com.huseyinaydin.sharedkernel.exception.BusinessException;
import tr.com.huseyinaydin.domain.fixtures.CreditTypeTestFixture;
import tr.com.huseyinaydin.application.common.BankingErrorCodes;

import java.math.BigDecimal;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.BDDMockito.willThrow;
import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
public class CreateCreditTypeCommandHandlerTest {

    @Mock
    private IUnitOfWork uow;
    @Mock
    private CreditTypeBusinessRules rules;
    @Mock
    private ICreditTypeRepository creditTypeRepository;

    @InjectMocks
    private CreateCreditTypeCommand.Handler handler;

    @Test
    @DisplayName("Kredi türü başarıyla oluşturulmalı")
    void shouldCreateCreditTypeSuccessfully() {
        // Arrange
        CreditType fixture = CreditTypeTestFixture.random();
        CreateCreditTypeCommand command = new CreateCreditTypeCommand(
                fixture.getName(),
                fixture.getDescription(),
                fixture.getCustomerType(),
                fixture.getMinimumAmount().getAmount(),
                fixture.getMaximumAmount().getAmount(),
                fixture.getMinimumTermMonths(),
                fixture.getMaximumTermMonths(),
                fixture.getAnnualInterestRate(),
                null
        );

        given(uow.creditTypes()).willReturn(creditTypeRepository);

        // Act
        CreateCreditTypeCommand.Response response = handler.handle(command);

        // Assert
        assertThat(response).isNotNull();
        assertThat(response.name()).isEqualTo(command.name());
        assertThat(response.customerType()).isEqualTo(command.customerType().name());
        assertThat(response.minimumAmount()).isEqualTo(command.minimumAmount());
        assertThat(response.maximumAmount()).isEqualTo(command.maximumAmount());

        ArgumentCaptor<CreditType> captor = ArgumentCaptor.forClass(CreditType.class);
        verify(creditTypeRepository).save(captor.capture());
        CreditType savedType = captor.getValue();

        assertThat(savedType.getName()).isEqualTo(command.name());
        assertThat(savedType.getAnnualInterestRate()).isEqualTo(command.annualInterestRate());

        verify(uow).beginTransaction();
        verify(uow).commit();
    }

    @Test
    @DisplayName("Minimum tutar maksimum tutardan büyük olduğunda hata fırlatmalı")
    void shouldThrowExceptionWhenMinAmountIsGreaterThanMaxAmount() {
        // Arrange
        CreditType fixture = CreditTypeTestFixture.random();
        CreateCreditTypeCommand command = new CreateCreditTypeCommand(
                fixture.getName(),
                fixture.getDescription(),
                fixture.getCustomerType(),
                new BigDecimal("100000"), // min > max
                new BigDecimal("1000"),
                fixture.getMinimumTermMonths(),
                fixture.getMaximumTermMonths(),
                fixture.getAnnualInterestRate(),
                null
        );

        willThrow(new BusinessException("Minimum tutar maksimum tutardan büyük olamaz", BankingErrorCodes.CREDIT_TYPE_MIN_AMOUNT_EXCEEDS_MAX))
                .given(rules).validateFinancialConstraints(
                        any(), any(), any(Integer.class), any(Integer.class), any());

        // Act & Assert
        assertThatThrownBy(() -> handler.handle(command))
                .isInstanceOf(BusinessException.class)
                .hasMessageContaining("Minimum tutar maksimum tutardan büyük olamaz");
    }
}
