package tr.com.huseyinaydin.infrastructure.cache;

import com.github.benmanes.caffeine.cache.Cache;
import com.github.benmanes.caffeine.cache.Caffeine;
import io.micrometer.core.instrument.MeterRegistry;
import org.springframework.cache.CacheManager;
import org.springframework.stereotype.Component;
import tr.com.huseyinaydin.application.ports.cache.IQueryCache;

import java.time.Duration;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;

@Component
public class CaffeineQueryCache<K, V> implements IQueryCache<K, V> {

    private final MeterRegistry meterRegistry;
    private final CacheManager cacheManager;
    private final ConcurrentHashMap<String, Cache<K, V>> defaultCaches = new ConcurrentHashMap<>();

    public CaffeineQueryCache(MeterRegistry meterRegistry, CacheManager cacheManager) {
        this.meterRegistry = meterRegistry;
        this.cacheManager = cacheManager;
    }

    private Cache<K, V> getOrCreateDefaultCache() {
        return defaultCaches.computeIfAbsent("default", k -> Caffeine.newBuilder()
                .maximumSize(1000)
                .build());
    }

    @Override
    public Optional<V> get(K key) {
        Cache<K, V> cache = getOrCreateDefaultCache();
        V value = cache.getIfPresent(key);
        
        String result = value != null ? "hit" : "miss";
        meterRegistry.counter("cache.gets", "result", result).increment();
        
        return Optional.ofNullable(value);
    }

    @Override
    public void put(K key, V value, Duration ttl) {
        Cache<K, V> cache = defaultCaches.computeIfAbsent("ttl_" + ttl.toMillis(), k -> Caffeine.newBuilder()
                .expireAfterWrite(ttl)
                .maximumSize(1000)
                .build());
        cache.put(key, value);
    }

    @Override
    public void evict(K key) {
        defaultCaches.values().forEach(cache -> cache.invalidate(key));
    }

    @Override
    public void evictAll(String cacheName) {
        org.springframework.cache.Cache springCache = cacheManager.getCache(cacheName);
        if (springCache != null) {
            springCache.clear();
        }
    }
}
