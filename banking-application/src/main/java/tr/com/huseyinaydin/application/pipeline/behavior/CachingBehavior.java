package tr.com.huseyinaydin.application.pipeline.behavior;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;
import tr.com.huseyinaydin.application.cqrs.IPipelineBehavior;
import tr.com.huseyinaydin.application.cqrs.PipelineDelegate;
import tr.com.huseyinaydin.application.pipeline.ICacheableRequest;
import tr.com.huseyinaydin.application.ports.cache.IQueryCache;

import java.util.Optional;

@Component
@Order(3)
public class CachingBehavior<TRequest, TResponse> implements IPipelineBehavior<TRequest, TResponse> {

    private static final Logger log = LoggerFactory.getLogger(CachingBehavior.class);
    
    private final IQueryCache<String, TResponse> queryCache;

    public CachingBehavior(IQueryCache<String, TResponse> queryCache) {
        this.queryCache = queryCache;
    }

    @Override
    public TResponse handle(TRequest request, PipelineDelegate<TResponse> next) {
        if (!(request instanceof ICacheableRequest cacheableRequest)) {
            return next.proceed();
        }

        String cacheName = cacheableRequest.getCacheName();
        String cacheKey = cacheableRequest.getCacheKey();

        Optional<TResponse> cachedResponse = queryCache.get(cacheName, cacheKey);
        if (cachedResponse.isPresent()) {
            log.debug("Cache hit for {}::{}", cacheName, cacheKey);
            return cachedResponse.get();
        }

        log.debug("Cache miss for {}::{}", cacheName, cacheKey);
        TResponse response = next.proceed();
        
        queryCache.put(cacheName, cacheKey, response, cacheableRequest.getTtl());
        return response;
    }
}
