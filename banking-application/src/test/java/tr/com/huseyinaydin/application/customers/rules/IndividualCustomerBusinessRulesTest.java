package tr.com.huseyinaydin.application.customers.rules;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import tr.com.huseyinaydin.domain.customer.IndividualCustomer;
import tr.com.huseyinaydin.domain.repositories.IIndividualCustomerRepository;
import tr.com.huseyinaydin.sharedkernel.exception.BusinessException;
import tr.com.huseyinaydin.sharedkernel.exception.ConflictException;
import tr.com.huseyinaydin.sharedkernel.exception.NotFoundException;
import tr.com.huseyinaydin.domain.fixtures.IndividualCustomerTestFixture;
import tr.com.huseyinaydin.application.common.BankingErrorCodes;

import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThatCode;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.BDDMockito.given;

@ExtendWith(MockitoExtension.class)
public class IndividualCustomerBusinessRulesTest {

    @Mock
    private IIndividualCustomerRepository repository;

    @InjectMocks
    private IndividualCustomerBusinessRules rules;

    @Test
    @DisplayName("TC Kimlik numarası tekrar etmediğinde sorunsuz geçmeli")
    void nationalIdCannotBeDuplicated_Success() {
        String nationalId = "12345678901";
        given(repository.findByNationalId(nationalId)).willReturn(Optional.empty());

        assertThatCode(() -> rules.nationalIdCannotBeDuplicatedWhenInserted(nationalId))
                .doesNotThrowAnyException();
    }

    @Test
    @DisplayName("TC Kimlik numarası tekrar ettiğinde ConflictException fırlatmalı")
    void nationalIdCannotBeDuplicated_Fails() {
        String nationalId = "12345678901";
        IndividualCustomer customer = IndividualCustomerTestFixture.random();
        given(repository.findByNationalId(nationalId)).willReturn(Optional.of(customer));

        assertThatThrownBy(() -> rules.nationalIdCannotBeDuplicatedWhenInserted(nationalId))
                .isInstanceOf(ConflictException.class)
                .hasMessageContaining("Bu TC Kimlik No zaten kayıtlı");
    }

    @Test
    @DisplayName("Müşteri bulunduğunda hata fırlatmamalı")
    void customerShouldExistWhenRequested_Success() {
        UUID id = UUID.randomUUID();
        IndividualCustomer customer = IndividualCustomerTestFixture.random();
        given(repository.findById(id)).willReturn(Optional.of(customer));

        assertThatCode(() -> rules.customerShouldExistWhenRequested(id))
                .doesNotThrowAnyException();
    }

    @Test
    @DisplayName("Müşteri bulunamadığında NotFoundException fırlatmalı")
    void customerShouldExistWhenRequested_Fails() {
        UUID id = UUID.randomUUID();
        given(repository.findById(id)).willReturn(Optional.empty());

        assertThatThrownBy(() -> rules.customerShouldExistWhenRequested(id))
                .isInstanceOf(NotFoundException.class)
                .hasMessageContaining(id.toString());
    }

    @Test
    @DisplayName("Müşteri aktif olduğunda hata fırlatmamalı")
    void customerShouldBeActive_Success() {
        IndividualCustomer customer = IndividualCustomerTestFixture.random();
        customer.setActive(true);

        assertThatCode(() -> rules.customerShouldBeActive(customer))
                .doesNotThrowAnyException();
    }

    @Test
    @DisplayName("Müşteri aktif değilse BusinessException fırlatmalı")
    void customerShouldBeActive_Fails() {
        IndividualCustomer customer = IndividualCustomerTestFixture.random();
        customer.setActive(false);

        assertThatThrownBy(() -> rules.customerShouldBeActive(customer))
                .isInstanceOf(BusinessException.class)
                .hasMessageContaining("Müşteri aktif değil")
                .hasFieldOrPropertyWithValue("errorCode", BankingErrorCodes.CUSTOMER_INACTIVE);
    }
}
