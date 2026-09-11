package tr.com.huseyinaydin.application.ports.metrics;

public interface IMeterRegistry {
    void recordCommandDuration(String commandType, long durationMs, boolean success);
    void incrementCounter(String metricName, String... tags);
}
