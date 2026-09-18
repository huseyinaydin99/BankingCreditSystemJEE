package tr.com.huseyinaydin.application.pipeline;

public interface ICacheEvictRequest {
    String getCacheName();
    String getCacheKey();
    boolean isEvictAll();
}
