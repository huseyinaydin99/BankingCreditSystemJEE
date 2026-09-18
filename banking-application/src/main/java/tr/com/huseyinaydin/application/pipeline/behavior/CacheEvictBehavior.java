package tr.com.huseyinaydin.application.pipeline.behavior;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;
import tr.com.huseyinaydin.application.cqrs.IPipelineBehavior;
import tr.com.huseyinaydin.application.cqrs.PipelineDelegate;
import tr.com.huseyinaydin.application.pipeline.ICacheEvictRequest;
import tr.com.huseyinaydin.application.ports.cache.IQueryCache;

@Component
@Order(8)
public class CacheEvictBehavior<TRequest, TResponse> implements IPipelineBehavior<TRequest, TResponse> {

    private static final Logger log = LoggerFactory.getLogger(CacheEvictBehavior.class);

    private final IQueryCache<String, Object> queryCache;

    public CacheEvictBehavior(IQueryCache<String, Object> queryCache) {
        this.queryCache = queryCache;
    }

    @Override
    public TResponse handle(TRequest request, PipelineDelegate<TResponse> next) {
        TResponse response = next.proceed();

        if (request instanceof ICacheEvictRequest evictRequest) {
            String cacheName = evictRequest.getCacheName();
            if (evictRequest.isEvictAll()) {
                log.debug("Evicting all from cache {}", cacheName);
                queryCache.evictAll(cacheName);
            } else {
                String cacheKey = evictRequest.getCacheKey();
                log.debug("Evicting key {} from cache {}", cacheKey, cacheName);
                queryCache.evict(cacheName, cacheKey);
            }
        }

        return response;
    }
}
