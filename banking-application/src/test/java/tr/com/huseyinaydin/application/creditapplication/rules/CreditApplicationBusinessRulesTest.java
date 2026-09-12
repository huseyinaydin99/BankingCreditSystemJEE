package tr.com.huseyinaydin.application.creditapplication.rules;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import tr.com.huseyinaydin.application.common.BankingErrorCodes;
import tr.com.huseyinaydin.application.pipeline.ICurrentUserService;
import tr.com.huseyinaydin.domain.creditapplication.CreditApplication;
import tr.com.huseyinaydin.domain.credittype.CreditType;
import tr.com.huseyinaydin.domain.enums.CreditApplicationStatus;
import tr.com.huseyinaydin.domain.enums.UserRole;
import tr.com.huseyinaydin.domain.repositories.IApplicationUserRepository;
import tr.com.huseyinaydin.domain.repositories.ICreditApplicationRepository;
import tr.com.huseyinaydin.domain.repositories.ICreditTypeRepository;
import tr.com.huseyinaydin.sharedkernel.exception.AuthorizationException;
import tr.com.huseyinaydin.sharedkernel.exception.BusinessException;
import tr.com.huseyinaydin.sharedkernel.exception.NotFoundException;
import tr.com.huseyinaydin.domain.fixtures.CreditApplicationTestFixture;
import tr.com.huseyinaydin.domain.fixtures.CreditTypeTestFixture;

import java.math.BigDecimal;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThatCode;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.BDDMockito.given;

@ExtendWith(MockitoExtension.class)
public class CreditApplicationBusinessRulesTest {

    @Mock
    private ICreditTypeRepository creditTypeRepository;
    @Mock
    private ICreditApplicationRepository creditApplicationRepository;
    @Mock
    private IApplicationUserRepository applicationUserRepository;
    @Mock
    private ICurrentUserService currentUserService;

    @InjectMocks
    private CreditApplicationBusinessRules rules;

    @Test
    @DisplayName("Kredi türü bulunduğunda hata fırlatmamalı")
    void creditTypeMustExist_Success() {
        UUID id = UUID.randomUUID();
        CreditType creditType = CreditTypeTestFixture.random();
        given(creditTypeRepository.findById(id)).willReturn(Optional.of(creditType));

        assertThatCode(() -> rules.creditTypeMustExist(id))
                .doesNotThrowAnyException();
    }

    @Test
    @DisplayName("Kredi türü bulunamadığında NotFoundException fırlatmalı")
    void creditTypeMustExist_Fails() {
        UUID id = UUID.randomUUID();
        given(creditTypeRepository.findById(id)).willReturn(Optional.empty());

        assertThatThrownBy(() -> rules.creditTypeMustExist(id))
                .isInstanceOf(NotFoundException.class)
                .hasMessageContaining(id.toString());
    }

    @Test
    @DisplayName("Başvuru bulunduğunda hata fırlatmamalı")
    void applicationMustExist_Success() {
        UUID id = UUID.randomUUID();
        CreditApplication app = CreditApplicationTestFixture.random();
        given(creditApplicationRepository.findById(id)).willReturn(Optional.of(app));

        assertThatCode(() -> rules.applicationMustExist(id))
                .doesNotThrowAnyException();
    }

    @Test
    @DisplayName("Başvuru bulunamadığında NotFoundException fırlatmalı")
    void applicationMustExist_Fails() {
        UUID id = UUID.randomUUID();
        given(creditApplicationRepository.findById(id)).willReturn(Optional.empty());

        assertThatThrownBy(() -> rules.applicationMustExist(id))
                .isInstanceOf(NotFoundException.class)
                .hasMessageContaining(id.toString());
    }

    @Test
    @DisplayName("Sadece PENDING durumundaki başvurular değiştirilebilmeli")
    void onlyPendingCanBeModified_Success() {
        CreditApplication app = CreditApplicationTestFixture.random();
        // default status is PENDING

        assertThatCode(() -> rules.onlyPendingCanBeModified(app))
                .doesNotThrowAnyException();
    }

    @Test
    @DisplayName("PENDING olmayan başvurular değiştirilmeye çalışıldığında hata fırlatmalı")
    void onlyPendingCanBeModified_Fails() {
        CreditApplication app = CreditApplicationTestFixture.random();
        app.moveToReview(); // Change status to bypass PENDING

        assertThatThrownBy(() -> rules.onlyPendingCanBeModified(app))
                .isInstanceOf(BusinessException.class)
                .hasMessageContaining("Yalnızca PENDING durumundaki başvurular");
    }

    @Test
    @DisplayName("Talep edilen miktar aralıkta olduğunda hata fırlatmamalı")
    void amountMustBeInRange_Success() {
        CreditType creditType = new CreditTypeTestFixture()
                .withMinimumAmount(tr.com.huseyinaydin.domain.valueobjects.Money.of(new BigDecimal("1000"), "TRY"))
                .withMaximumAmount(tr.com.huseyinaydin.domain.valueobjects.Money.of(new BigDecimal("10000"), "TRY"))
                .build();

        assertThatCode(() -> rules.amountMustBeInRange(new BigDecimal("5000"), creditType))
                .doesNotThrowAnyException();
    }

    @Test
    @DisplayName("Talep edilen miktar limit dışı olduğunda BusinessException fırlatmalı")
    void amountMustBeInRange_Fails() {
        CreditType creditType = new CreditTypeTestFixture()
                .withMinimumAmount(tr.com.huseyinaydin.domain.valueobjects.Money.of(new BigDecimal("1000"), "TRY"))
                .withMaximumAmount(tr.com.huseyinaydin.domain.valueobjects.Money.of(new BigDecimal("10000"), "TRY"))
                .build();

        assertThatThrownBy(() -> rules.amountMustBeInRange(new BigDecimal("15000"), creditType))
                .isInstanceOf(BusinessException.class)
                .hasMessageContaining("Talep edilen tutar kredi türü limitlerinin dışında")
                .hasFieldOrPropertyWithValue("errorCode", BankingErrorCodes.AMOUNT_OUT_OF_RANGE);
    }

    @Test
    @DisplayName("Talep edilen vade aralıkta olduğunda hata fırlatmamalı")
    void termMustBeInRange_Success() {
        CreditType creditType = new CreditTypeTestFixture()
                .withMinimumTermMonths(6)
                .withMaximumTermMonths(36)
                .build();

        assertThatCode(() -> rules.termMustBeInRange(12, creditType))
                .doesNotThrowAnyException();
    }

    @Test
    @DisplayName("Talep edilen vade limit dışı olduğunda BusinessException fırlatmalı")
    void termMustBeInRange_Fails() {
        CreditType creditType = new CreditTypeTestFixture()
                .withMinimumTermMonths(6)
                .withMaximumTermMonths(36)
                .build();

        assertThatThrownBy(() -> rules.termMustBeInRange(48, creditType))
                .isInstanceOf(BusinessException.class)
                .hasMessageContaining("Talep edilen vade kredi türü limitlerinin dışında")
                .hasFieldOrPropertyWithValue("errorCode", BankingErrorCodes.TERM_OUT_OF_RANGE);
    }

    @ParameterizedTest
    @CsvSource({
            "PENDING, UNDER_REVIEW, true",
            "PENDING, CANCELLED, true",
            "PENDING, APPROVED, false",
            "PENDING, REJECTED, false",
            "UNDER_REVIEW, APPROVED, true",
            "UNDER_REVIEW, REJECTED, true",
            "UNDER_REVIEW, CANCELLED, true",
            "UNDER_REVIEW, PENDING, false",
            "APPROVED, PENDING, false",
            "APPROVED, REJECTED, false",
            "REJECTED, APPROVED, false",
            "CANCELLED, PENDING, false"
    })
    @DisplayName("Durum geçişleri (State Transitions) doğru çalışmalı")
    void statusTransitionMustBeValid(CreditApplicationStatus current, CreditApplicationStatus target, boolean isValid) {
        if (isValid) {
            assertThatCode(() -> rules.statusTransitionMustBeValid(current, target))
                    .doesNotThrowAnyException();
        } else {
            assertThatThrownBy(() -> rules.statusTransitionMustBeValid(current, target))
                    .isInstanceOf(BusinessException.class)
                    .hasMessageContaining("Geçersiz durum geçişi")
                    .hasFieldOrPropertyWithValue("errorCode", BankingErrorCodes.CREDIT_APPLICATION_INVALID_STATUS_TRANSITION);
        }
    }
    @Test
    @DisplayName("Kullanıcı doğrulama yapmamışsa AuthorizationException fırlatmalı")
    void userCanAccessApplication_Unauthenticated() {
        CreditApplication app = CreditApplicationTestFixture.random();
        given(currentUserService.getCurrentUserId()).willReturn(null);

        assertThatThrownBy(() -> rules.userCanAccessApplication(app))
                .isInstanceOf(AuthorizationException.class)
                .hasMessageContaining("kimlik doğrulama gereklidir");
    }

    @Test
    @DisplayName("Admin yetkili kullanıcı başvuruya erişebilmeli")
    void userCanAccessApplication_Admin() {
        CreditApplication app = CreditApplicationTestFixture.random();
        given(currentUserService.getCurrentUserId()).willReturn(UUID.randomUUID().toString());
        given(currentUserService.isAuthenticated()).willReturn(true);
        given(currentUserService.getCurrentUserRoles()).willReturn(new String[]{"ADMIN"});

        assertThatCode(() -> rules.userCanAccessApplication(app))
                .doesNotThrowAnyException();
    }
}
