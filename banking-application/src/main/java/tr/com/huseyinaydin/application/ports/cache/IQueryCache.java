package tr.com.huseyinaydin.application.ports.cache;

import java.time.Duration;
import java.util.Optional;

public interface IQueryCache<K, V> {
    Optional<V> get(String cacheName, K key);
    void put(String cacheName, K key, V value, Duration ttl);
    void evict(String cacheName, K key);
    void evictAll(String cacheName);
}
