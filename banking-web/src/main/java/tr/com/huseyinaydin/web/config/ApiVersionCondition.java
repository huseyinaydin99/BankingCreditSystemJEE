package tr.com.huseyinaydin.web.config;

import jakarta.servlet.http.HttpServletRequest;
import org.springframework.web.servlet.mvc.condition.RequestCondition;
import tr.com.huseyinaydin.web.annotation.ApiVersion;

public class ApiVersionCondition implements RequestCondition<ApiVersionCondition> {

    private final int version;

    public ApiVersionCondition(int version) {
        this.version = version;
    }

    @Override
    public ApiVersionCondition combine(ApiVersionCondition other) {
        // En son tanımlanan (örneğin metot düzeyindeki) version'u kullan
        return new ApiVersionCondition(other.version);
    }

    @Override
    public ApiVersionCondition getMatchingCondition(HttpServletRequest request) {
        // URI tabanlı version eşleştirme: "/api/v1/..." şeklindeki isteklerde versiyonu çıkar.
        String uri = request.getRequestURI();
        if (uri.matches("^/api/v\\d+/.*")) {
            String[] parts = uri.split("/");
            if (parts.length > 2 && parts[2].startsWith("v")) {
                try {
                    int requestVersion = Integer.parseInt(parts[2].substring(1));
                    if (requestVersion == this.version) {
                        return this;
                    }
                } catch (NumberFormatException ignored) {}
            }
        }
        return null;
    }

    @Override
    public int compareTo(ApiVersionCondition other, HttpServletRequest request) {
        return Integer.compare(other.version, this.version); // Yüksek versiyon öncelikli
    }
}
