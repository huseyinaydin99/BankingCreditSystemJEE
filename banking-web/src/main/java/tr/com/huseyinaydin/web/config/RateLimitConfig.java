package tr.com.huseyinaydin.web.config;

import com.hazelcast.config.Config;
import com.hazelcast.core.Hazelcast;
import com.hazelcast.core.HazelcastInstance;
import com.hazelcast.map.IMap;
import io.github.bucket4j.grid.hazelcast.HazelcastProxyManager;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class RateLimitConfig {

    @Bean(destroyMethod = "shutdown")
    public HazelcastInstance hazelcastInstance() {
        Config config = new Config();
        config.setClusterName("banking-rate-limit-cluster");
        return Hazelcast.getOrCreateHazelcastInstance(config);
    }

    @Bean
    public HazelcastProxyManager<String> hazelcastProxyManager(HazelcastInstance hazelcastInstance) {
        IMap<String, byte[]> map = hazelcastInstance.getMap("rate-limits");
        return new HazelcastProxyManager<>(map);
    }
}
