package tr.com.huseyinaydin.infrastructure.cache;

import io.micrometer.core.instrument.MeterRegistry;
import org.springframework.cache.CacheManager;
import org.springframework.stereotype.Component;
import tr.com.huseyinaydin.application.ports.cache.IQueryCache;

import java.time.Duration;
import java.util.Optional;

@Component
public class CaffeineQueryCache<K, V> implements IQueryCache<K, V> {

    private final MeterRegistry meterRegistry;
    private final CacheManager cacheManager;

    public CaffeineQueryCache(MeterRegistry meterRegistry, CacheManager cacheManager) {
        this.meterRegistry = meterRegistry;
        this.cacheManager = cacheManager;
    }

    @Override
    @SuppressWarnings("unchecked")
    public Optional<V> get(String cacheName, K key) {
        org.springframework.cache.Cache springCache = cacheManager.getCache(cacheName);
        if (springCache != null) {
            org.springframework.cache.Cache.ValueWrapper wrapper = springCache.get(key);
            if (wrapper != null) {
                meterRegistry.counter("cache.gets", "result", "hit", "cache", cacheName).increment();
                return Optional.ofNullable((V) wrapper.get());
            }
        }
        meterRegistry.counter("cache.gets", "result", "miss", "cache", cacheName).increment();
        return Optional.empty();
    }

    @Override
    public void put(String cacheName, K key, V value, Duration ttl) {
        org.springframework.cache.Cache springCache = cacheManager.getCache(cacheName);
        if (springCache != null) {
            springCache.put(key, value);
        }
    }

    @Override
    public void evict(String cacheName, K key) {
        org.springframework.cache.Cache springCache = cacheManager.getCache(cacheName);
        if (springCache != null) {
            springCache.evict(key);
        }
    }

    @Override
    public void evictAll(String cacheName) {
        org.springframework.cache.Cache springCache = cacheManager.getCache(cacheName);
        if (springCache != null) {
            springCache.clear();
        }
    }
}
