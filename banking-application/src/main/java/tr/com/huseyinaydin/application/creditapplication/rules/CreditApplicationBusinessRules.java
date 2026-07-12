package tr.com.huseyinaydin.application.creditapplication.rules;

import org.springframework.stereotype.Component;
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

import java.math.BigDecimal;
import java.util.Arrays;
import java.util.EnumMap;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;

@Component
@SuppressWarnings("SpringJavaInjectionPointsAutowiringInspection")
public class CreditApplicationBusinessRules {

    private final ICreditTypeRepository creditTypeRepository;
    private final ICreditApplicationRepository creditApplicationRepository;
    private final IApplicationUserRepository applicationUserRepository;
    private final ICurrentUserService currentUserService;

    /*
     * İzinli durum geçiş grafiği: PENDING → UNDER_REVIEW → APPROVED/REJECTED.
     * CANCELLED, henüz sonuçlanmamış (PENDING/UNDER_REVIEW) başvurulardan erişilebilir.
     * Terminal durumlar (APPROVED/REJECTED/CANCELLED) için giden geçiş yoktur.
     */
    private static final Map<CreditApplicationStatus, Set<CreditApplicationStatus>> ALLOWED_TRANSITIONS =
            new EnumMap<>(CreditApplicationStatus.class);

    static {
        ALLOWED_TRANSITIONS.put(CreditApplicationStatus.PENDING,
                Set.of(CreditApplicationStatus.UNDER_REVIEW, CreditApplicationStatus.CANCELLED));
        ALLOWED_TRANSITIONS.put(CreditApplicationStatus.UNDER_REVIEW,
                Set.of(CreditApplicationStatus.APPROVED, CreditApplicationStatus.REJECTED,
                        CreditApplicationStatus.CANCELLED));
        ALLOWED_TRANSITIONS.put(CreditApplicationStatus.APPROVED, Set.of());
        ALLOWED_TRANSITIONS.put(CreditApplicationStatus.REJECTED, Set.of());
        ALLOWED_TRANSITIONS.put(CreditApplicationStatus.CANCELLED, Set.of());
    }

    public CreditApplicationBusinessRules(ICreditTypeRepository creditTypeRepository,
                                          ICreditApplicationRepository creditApplicationRepository,
                                          IApplicationUserRepository applicationUserRepository,
                                          ICurrentUserService currentUserService) {
        this.creditTypeRepository = creditTypeRepository;
        this.creditApplicationRepository = creditApplicationRepository;
        this.applicationUserRepository = applicationUserRepository;
        this.currentUserService = currentUserService;
    }

    public CreditType creditTypeMustExist(UUID id) {
        return creditTypeRepository.findById(id).orElseThrow(() ->
                new NotFoundException("CREDIT_TYPE", id.toString()));
    }

    public CreditApplication applicationMustExist(UUID id) {
        return creditApplicationRepository.findById(id).orElseThrow(() ->
                new NotFoundException("CREDIT_APPLICATION", id.toString()));
    }

    /**
     * Satır bazlı erişim yetkisi: OFFICER ve ADMIN tüm başvurulara erişebilir;
     * CUSTOMER yalnızca kendi müşteri kaydına ait başvurulara erişebilir. Aksi hâlde
     * {@link AuthorizationException} (HTTP 401/403) fırlatılır.
     */
    public void userCanAccessApplication(CreditApplication application) {
        String currentUserId = currentUserService.getCurrentUserId();
        if (currentUserId == null || !currentUserService.isAuthenticated()) {
            throw new AuthorizationException("VIEW_CREDIT_APPLICATION",
                    "Bu işlem için kimlik doğrulama gereklidir");
        }

        Set<String> roles = Arrays.stream(currentUserService.getCurrentUserRoles())
                .map(String::toUpperCase)
                .collect(Collectors.toSet());

        // Personel rolleri tüm başvurulara erişebilir.
        if (roles.contains(UserRole.OFFICER.name()) || roles.contains(UserRole.ADMIN.name())) {
            return;
        }

        // CUSTOMER: yalnızca kendi başvuruları.
        UUID ownerCustomerId = resolveCurrentCustomerId(currentUserId);
        if (ownerCustomerId == null || !ownerCustomerId.equals(application.getCustomerId())) {
            throw new AuthorizationException("VIEW_CREDIT_APPLICATION",
                    "Bu başvuruya erişim yetkiniz yok");
        }
    }

    private UUID resolveCurrentCustomerId(String currentUserId) {
        UUID userId;
        try {
            userId = UUID.fromString(currentUserId);
        } catch (IllegalArgumentException ex) {
            return null;
        }
        return applicationUserRepository.findById(userId)
                .map(user -> user.getCustomerId())
                .orElse(null);
    }

    /** Yalnızca PENDING durumundaki başvurular güncellenebilir/silinebilir. */
    public void onlyPendingCanBeModified(CreditApplication application) {
        if (application.getStatus() != CreditApplicationStatus.PENDING) {
            throw new BusinessException(
                    "Yalnızca PENDING durumundaki başvurular üzerinde bu işlem yapılabilir. "
                            + "Mevcut durum: " + application.getStatus(),
                    BankingErrorCodes.CREDIT_APPLICATION_NOT_PENDING);
        }
    }

    /**
     * Onay iş akışının durum geçişini doğrular. Yalnızca
     * PENDING → UNDER_REVIEW → APPROVED/REJECTED (ve iptal) çizgisine izin verilir;
     * geçersiz geçişte {@link BusinessException} fırlatılır.
     */
    public void statusTransitionMustBeValid(CreditApplicationStatus current,
                                            CreditApplicationStatus target) {
        Set<CreditApplicationStatus> allowed =
                ALLOWED_TRANSITIONS.getOrDefault(current, Set.of());
        if (!allowed.contains(target)) {
            throw new BusinessException(
                    "Geçersiz durum geçişi: " + current + " → " + target,
                    BankingErrorCodes.CREDIT_APPLICATION_INVALID_STATUS_TRANSITION);
        }
    }

    public void amountMustBeInRange(BigDecimal amount, CreditType creditType) {
        if (amount.compareTo(creditType.getMinimumAmount().getAmount()) < 0
                || amount.compareTo(creditType.getMaximumAmount().getAmount()) > 0) {
            throw new BusinessException(
                    "Talep edilen tutar kredi türü limitlerinin dışında. Min: "
                            + creditType.getMinimumAmount().getAmount()
                            + ", Max: " + creditType.getMaximumAmount().getAmount(),
                    BankingErrorCodes.AMOUNT_OUT_OF_RANGE
            );
        }
    }

    public void termMustBeInRange(int term, CreditType creditType) {
        if (term < creditType.getMinimumTermMonths() || term > creditType.getMaximumTermMonths()) {
            throw new BusinessException(
                    "Talep edilen vade kredi türü limitlerinin dışında. Min: "
                            + creditType.getMinimumTermMonths() + ", Max: " + creditType.getMaximumTermMonths(),
                    BankingErrorCodes.TERM_OUT_OF_RANGE
            );
        }
    }
}
