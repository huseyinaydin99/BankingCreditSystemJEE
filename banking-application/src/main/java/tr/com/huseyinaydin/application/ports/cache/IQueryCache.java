package tr.com.huseyinaydin.application.ports.cache;

import java.time.Duration;
import java.util.Optional;

public interface IQueryCache<K, V> {
    Optional<V> get(K key);
    void put(K key, V value, Duration ttl);
    void evict(K key);
    void evictAll(String cacheName);
}
