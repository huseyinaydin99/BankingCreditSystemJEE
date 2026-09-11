package tr.com.huseyinaydin.infrastructure.metrics;

import io.micrometer.core.instrument.MeterRegistry;
import io.micrometer.core.instrument.Timer;
import org.springframework.stereotype.Component;
import tr.com.huseyinaydin.application.ports.metrics.IMeterRegistry;

import java.util.concurrent.TimeUnit;

@Component
public class MicrometerRegistryAdapter implements IMeterRegistry {

    private final MeterRegistry meterRegistry;

    public MicrometerRegistryAdapter(MeterRegistry meterRegistry) {
        this.meterRegistry = meterRegistry;
    }

    @Override
    public void recordCommandDuration(String commandType, long durationMs, boolean success) {
        Timer.builder("banking.command.execution")
                .tag("commandType", commandType)
                .tag("status", success ? "success" : "failure")
                .register(meterRegistry)
                .record(durationMs, TimeUnit.MILLISECONDS);
    }

    @Override
    public void incrementCounter(String metricName, String... tags) {
        meterRegistry.counter(metricName, tags).increment();
    }
}
