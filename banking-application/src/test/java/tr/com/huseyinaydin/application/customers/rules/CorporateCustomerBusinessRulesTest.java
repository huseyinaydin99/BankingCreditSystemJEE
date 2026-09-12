package tr.com.huseyinaydin.application.customers.rules;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import tr.com.huseyinaydin.domain.customer.CorporateCustomer;
import tr.com.huseyinaydin.domain.repositories.ICorporateCustomerRepository;
import tr.com.huseyinaydin.sharedkernel.exception.BusinessException;
import tr.com.huseyinaydin.sharedkernel.exception.ConflictException;
import tr.com.huseyinaydin.sharedkernel.exception.NotFoundException;
import tr.com.huseyinaydin.domain.fixtures.CorporateCustomerTestFixture;
import tr.com.huseyinaydin.application.common.BankingErrorCodes;

import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThatCode;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.BDDMockito.given;

@ExtendWith(MockitoExtension.class)
public class CorporateCustomerBusinessRulesTest {

    @Mock
    private ICorporateCustomerRepository repository;

    @InjectMocks
    private CorporateCustomerBusinessRules rules;

    @Test
    @DisplayName("Vergi numarası tekrar etmediğinde sorunsuz geçmeli")
    void taxNumberCannotBeDuplicated_Success() {
        String taxNumber = "1234567890";
        given(repository.findByTaxNumber(taxNumber)).willReturn(Optional.empty());

        assertThatCode(() -> rules.taxNumberCannotBeDuplicatedWhenInserted(taxNumber))
                .doesNotThrowAnyException();
    }

    @Test
    @DisplayName("Vergi numarası tekrar ettiğinde ConflictException fırlatmalı")
    void taxNumberCannotBeDuplicated_Fails() {
        String taxNumber = "1234567890";
        CorporateCustomer customer = CorporateCustomerTestFixture.random();
        given(repository.findByTaxNumber(taxNumber)).willReturn(Optional.of(customer));

        assertThatThrownBy(() -> rules.taxNumberCannotBeDuplicatedWhenInserted(taxNumber))
                .isInstanceOf(ConflictException.class)
                .hasMessageContaining("Bu Vergi No zaten kayıtlı");
    }
    
    @Test
    @DisplayName("Ticaret Sicil No tekrar etmediğinde sorunsuz geçmeli")
    void tradeRegistrationNumberCannotBeDuplicated_Success() {
        String regNo = "123456";
        given(repository.findByTradeRegistrationNumber(regNo)).willReturn(Optional.empty());

        assertThatCode(() -> rules.tradeRegistrationNumberCannotBeDuplicatedWhenInserted(regNo))
                .doesNotThrowAnyException();
    }

    @Test
    @DisplayName("Ticaret Sicil No tekrar ettiğinde ConflictException fırlatmalı")
    void tradeRegistrationNumberCannotBeDuplicated_Fails() {
        String regNo = "123456";
        CorporateCustomer customer = CorporateCustomerTestFixture.random();
        given(repository.findByTradeRegistrationNumber(regNo)).willReturn(Optional.of(customer));

        assertThatThrownBy(() -> rules.tradeRegistrationNumberCannotBeDuplicatedWhenInserted(regNo))
                .isInstanceOf(ConflictException.class)
                .hasMessageContaining("Bu Ticaret Sicil Numarası zaten kayıtlı");
    }

    @Test
    @DisplayName("Müşteri bulunduğunda hata fırlatmamalı")
    void customerShouldExistWhenRequested_Success() {
        UUID id = UUID.randomUUID();
        CorporateCustomer customer = CorporateCustomerTestFixture.random();
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
        CorporateCustomer customer = CorporateCustomerTestFixture.random();
        customer.setActive(true);

        assertThatCode(() -> rules.customerShouldBeActive(customer))
                .doesNotThrowAnyException();
    }

    @Test
    @DisplayName("Müşteri aktif değilse BusinessException fırlatmalı")
    void customerShouldBeActive_Fails() {
        CorporateCustomer customer = CorporateCustomerTestFixture.random();
        customer.setActive(false);

        assertThatThrownBy(() -> rules.customerShouldBeActive(customer))
                .isInstanceOf(BusinessException.class)
                .hasMessageContaining("Müşteri aktif değil")
                .hasFieldOrPropertyWithValue("errorCode", BankingErrorCodes.CUSTOMER_INACTIVE);
    }
}
