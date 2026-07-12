package tr.com.huseyinaydin.web.audit;

import jakarta.servlet.http.HttpServletRequest;
import org.springframework.stereotype.Component;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;
import tr.com.huseyinaydin.application.ports.IpAddressProvider;

/**
 * {@link IpAddressProvider} web implementasyonu. Aktif HTTP isteğini
 * {@link RequestContextHolder} üzerinden alır; proxy arkasındaki gerçek istemci için önce
 * {@code X-Forwarded-For} başlığının ilk IP'sini, yoksa {@code getRemoteAddr()} değerini döner.
 * İstek bağlamı yoksa (ör. asenkron/zamanlanmış çağrı) null döner.
 */
@Component
public class WebIpAddressProvider implements IpAddressProvider {

    private static final String X_FORWARDED_FOR = "X-Forwarded-For";

    @Override
    public String getClientIpAddress() {
        HttpServletRequest request = currentRequest();
        if (request == null) {
            return null;
        }

        String forwarded = request.getHeader(X_FORWARDED_FOR);
        if (forwarded != null && !forwarded.isBlank()) {
            // "client, proxy1, proxy2" — ilk değer gerçek istemcidir
            return forwarded.split(",")[0].trim();
        }
        return request.getRemoteAddr();
    }

    private static HttpServletRequest currentRequest() {
        if (RequestContextHolder.getRequestAttributes()
                instanceof ServletRequestAttributes servletAttributes) {
            return servletAttributes.getRequest();
        }
        return null;
    }
}
