package tr.com.huseyinaydin.application.pipeline;

import java.time.Duration;

public interface ICacheableRequest {
    String getCacheName();
    String getCacheKey();
    Duration getTtl();
}
