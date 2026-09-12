package tr.com.huseyinaydin.application.pipeline.behavior;

import ch.qos.logback.classic.Level;
import ch.qos.logback.classic.Logger;
import ch.qos.logback.classic.spi.ILoggingEvent;
import ch.qos.logback.core.read.ListAppender;
import io.micrometer.tracing.Tracer;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.slf4j.LoggerFactory;
import tr.com.huseyinaydin.application.cqrs.PipelineDelegate;
import tr.com.huseyinaydin.application.pipeline.MockCurrentUserService;

import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.BDDMockito.given;

@ExtendWith(MockitoExtension.class)
class LoggingBehaviorTest {

    private MockCurrentUserService currentUserService;

    @Mock
    private Tracer tracer;

    @Mock
    private PipelineDelegate<String> next;

    private LoggingBehavior<Object, String> loggingBehavior;
    private ListAppender<ILoggingEvent> listAppender;
    private Logger logger;

    static class TestCommand {
        private String nationalId = "12345678901";
        private String password = "secretPassword123";

        public String getNationalId() { return nationalId; }
        public String getPassword() { return password; }
    }

    @BeforeEach
    void setUp() {
        currentUserService = new MockCurrentUserService("user-123", "test@test.com", Set.of("CUSTOMER"));
        loggingBehavior = new LoggingBehavior<>(currentUserService, tracer);

        logger = (Logger) LoggerFactory.getLogger(LoggingBehavior.class);
        listAppender = new ListAppender<>();
        listAppender.start();
        logger.addAppender(listAppender);
    }

    @AfterEach
    void tearDown() {
        logger.detachAppender(listAppender);
    }

    @Test
    @DisplayName("Basarili islemde INFO logu atilmali")
    void shouldLogInfoOnSuccess() {
        TestCommand command = new TestCommand();
        given(next.proceed()).willReturn("SUCCESS");

        String result = loggingBehavior.handle(command, next);

        assertThat(result).isEqualTo("SUCCESS");

        assertThat(listAppender.list).hasSize(1);
        ILoggingEvent event = listAppender.list.get(0);
        assertThat(event.getLevel()).isEqualTo(Level.INFO);
        assertThat(event.getMessage()).isEqualTo("command handled");
        
        assertThat(event.getKeyValuePairs()).anyMatch(kv -> kv.key.equals("userId") && kv.value.equals("user-123"));
        assertThat(event.getKeyValuePairs()).anyMatch(kv -> kv.key.equals("success") && kv.value.equals(true));
        assertThat(event.getKeyValuePairs()).anyMatch(kv -> kv.key.equals("commandType") && kv.value.equals("TestCommand"));
    }

    @Test
    @DisplayName("Hata durumunda ERROR logu atilmali ve maskeleme yapilmali")
    void shouldLogErrorAndMaskOnFailure() {
        TestCommand command = new TestCommand();
        given(next.proceed()).willThrow(new RuntimeException("Test Error"));

        assertThatThrownBy(() -> loggingBehavior.handle(command, next))
                .isInstanceOf(RuntimeException.class);

        assertThat(listAppender.list).hasSize(1);
        ILoggingEvent event = listAppender.list.get(0);
        assertThat(event.getLevel()).isEqualTo(Level.ERROR);
        assertThat(event.getMessage()).isEqualTo("command failed");

        assertThat(event.getKeyValuePairs()).anyMatch(kv -> kv.key.equals("success") && kv.value.equals(false));
        
        var requestKv = event.getKeyValuePairs().stream().filter(kv -> kv.key.equals("request")).findFirst();
        assertThat(requestKv).isPresent();
        String serializedReq = requestKv.get().value.toString();
        
        assertThat(serializedReq).contains("nationalId\":\"12345678901\"");
        assertThat(serializedReq).contains("password\":\"***\"");
    }
}
