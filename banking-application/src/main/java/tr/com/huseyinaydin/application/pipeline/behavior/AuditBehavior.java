package tr.com.huseyinaydin.application.pipeline.behavior;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.annotation.Order;
import tr.com.huseyinaydin.application.cqrs.IPipelineBehavior;
import tr.com.huseyinaydin.application.cqrs.PipelineDelegate;
import tr.com.huseyinaydin.application.pipeline.ICurrentUserService;
import tr.com.huseyinaydin.application.ports.IAuditService;
import tr.com.huseyinaydin.application.ports.IpAddressProvider;
import tr.com.huseyinaydin.sharedkernel.audit.AuditAction;
import tr.com.huseyinaydin.sharedkernel.audit.AuditEntry;
import tr.com.huseyinaydin.sharedkernel.messaging.ICommand;

import java.lang.reflect.Method;
import java.time.Instant;
import java.util.Iterator;
import java.util.Map;
import java.util.UUID;
import java.util.regex.Pattern;

/**
 * Denetim izi behavior'ı. Yalnızca {@link ICommand} isteklerinde çalışır ve komut
 * başarıyla tamamlandıktan (transaction commit) SONRA bir denetim kaydı yazar.
 *
 * Sıralama: {@code @Order(6)} — TransactionBehavior ({@code @Order(10)}) daha içte kaldığı
 * için {@code next.proceed()} tx commit olduktan sonra döner; böylece yalnızca başarılı
 * komutlar denetlenir. Denetim yazımı best-effort'tur; hatası asıl sonucu etkilemez.
 *
 * NOT: Bu jenerik katman entity'nin DB'deki önceki halini bilemez. "before/after"
 * yaklaşımı olarak komut yükü serialize edilir: CREATE/UPDATE → newValue, DELETE → oldValue.
 * Gerçek eski-değer yakalama entity-bazlı hook gerektirir (kapsam dışı).
 */
@Order(6)
public class AuditBehavior<TRequest, TResponse> implements IPipelineBehavior<TRequest, TResponse> {

    private static final Logger log = LoggerFactory.getLogger(AuditBehavior.class);

    // Adı bu deseni içeren alanlar JSON'da maskelenir (parola vb. denetime sızmasın).
    private static final Pattern SENSITIVE_FIELD =
            Pattern.compile("(?i)(password|secret|token|otp|hash)");
    private static final String REDACTED = "***";

    private final IAuditService auditService;
    private final IpAddressProvider ipAddressProvider;
    private final ICurrentUserService currentUserService;
    private final ObjectMapper objectMapper;

    public AuditBehavior(IAuditService auditService,
                         IpAddressProvider ipAddressProvider,
                         ICurrentUserService currentUserService) {
        this.auditService = auditService;
        this.ipAddressProvider = ipAddressProvider;
        this.currentUserService = currentUserService;
        this.objectMapper = new ObjectMapper();
        this.objectMapper.findAndRegisterModules(); // jsr310 vb.
    }

    @Override
    public TResponse handle(TRequest request, PipelineDelegate<TResponse> next) {
        if (auditService == null || !(request instanceof ICommand)) {
            return next.proceed();
        }

        // Önce asıl işlem (ve iç transaction) tamamlanır; hata olursa audit yazılmaz.
        TResponse response = next.proceed();

        try {
            auditService.record(buildEntry(request, response));
        } catch (Exception ex) {
            log.warn("Denetim kaydı yazılamadı [{}]: {}",
                    request.getClass().getSimpleName(), ex.getMessage());
        }

        return response;
    }

    private AuditEntry buildEntry(TRequest request, TResponse response) {
        String commandName = request.getClass().getSimpleName();
        AuditAction action = resolveAction(commandName);
        String entityType = resolveEntityType(commandName);
        String entityId = extractId(response);
        if (entityId == null) {
            entityId = extractId(request);
        }

        String payload = serializeRedacted(request);
        boolean isDelete = action == AuditAction.DELETE;
        String oldValue = isDelete ? payload : null;
        String newValue = isDelete ? null : payload;

        return new AuditEntry(
                entityId,
                entityType,
                action,
                resolvePerformedBy(),
                Instant.now(),
                resolveIpAddress(),
                oldValue,
                newValue);
    }

    private static AuditAction resolveAction(String commandName) {
        if (commandName.startsWith("Create")) return AuditAction.CREATE;
        if (commandName.startsWith("Delete")) return AuditAction.DELETE;
        return AuditAction.UPDATE; // Update/Approve/Reject/MoveToReview... hepsi state değiştirir
    }

    private static String resolveEntityType(String commandName) {
        String type = commandName;
        if (type.endsWith("Command")) {
            type = type.substring(0, type.length() - "Command".length());
        }
        for (String prefix : new String[]{"Create", "Update", "Delete"}) {
            if (type.startsWith(prefix)) {
                type = type.substring(prefix.length());
                break;
            }
        }
        return type.isBlank() ? commandName : type;
    }

    private UUID resolvePerformedBy() {
        if (currentUserService == null) return null;
        String userId = currentUserService.getCurrentUserId();
        if (userId == null) return null;
        try {
            return UUID.fromString(userId);
        } catch (IllegalArgumentException ex) {
            return null; // "anonymous" gibi UUID olmayan değerler
        }
    }

    private String resolveIpAddress() {
        if (ipAddressProvider == null) return null;
        try {
            return ipAddressProvider.getClientIpAddress();
        } catch (Exception ex) {
            return null;
        }
    }

    /** Response ya da command üzerinden id() / getId() ile kimlik çıkarır. */
    private static String extractId(Object target) {
        if (target == null) return null;
        for (String methodName : new String[]{"id", "getId"}) {
            try {
                Method m = target.getClass().getMethod(methodName);
                Object value = m.invoke(target);
                if (value != null) return String.valueOf(value);
            } catch (ReflectiveOperationException ignored) {
                // metot yok/erişilemez — diğerini dene
            }
        }
        return null;
    }

    private String serializeRedacted(Object command) {
        try {
            JsonNode node = objectMapper.valueToTree(command);
            redact(node);
            return objectMapper.writeValueAsString(node);
        } catch (Exception ex) {
            log.debug("Command serileştirilemedi: {}", ex.getMessage());
            return null;
        }
    }

    private static void redact(JsonNode node) {
        if (node instanceof ObjectNode objectNode) {
            Iterator<Map.Entry<String, JsonNode>> fields = objectNode.fields();
            while (fields.hasNext()) {
                Map.Entry<String, JsonNode> field = fields.next();
                if (SENSITIVE_FIELD.matcher(field.getKey()).find()) {
                    objectNode.put(field.getKey(), REDACTED);
                } else {
                    redact(field.getValue());
                }
            }
        } else if (node != null && node.isArray()) {
            node.forEach(AuditBehavior::redact);
        }
    }
}
