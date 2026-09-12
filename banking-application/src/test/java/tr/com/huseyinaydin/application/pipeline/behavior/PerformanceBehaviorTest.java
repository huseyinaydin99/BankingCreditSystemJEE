package tr.com.huseyinaydin.application.pipeline.behavior;

import ch.qos.logback.classic.Level;
import ch.qos.logback.classic.Logger;
import ch.qos.logback.classic.spi.ILoggingEvent;
import ch.qos.logback.core.read.ListAppender;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.slf4j.LoggerFactory;
import tr.com.huseyinaydin.application.cqrs.PipelineDelegate;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.BDDMockito.given;

@ExtendWith(MockitoExtension.class)
class PerformanceBehaviorTest {

    @Mock
    private PipelineDelegate<String> next;

    private PerformanceBehavior<Object, String> performanceBehavior;
    private ListAppender<ILoggingEvent> listAppender;
    private Logger logger;

    static class FastCommand {}
    static class SlowCommand {}

    @BeforeEach
    void setUp() {
        performanceBehavior = new PerformanceBehavior<>();
        logger = (Logger) LoggerFactory.getLogger(PerformanceBehavior.class);
        listAppender = new ListAppender<>();
        listAppender.start();
        logger.addAppender(listAppender);
    }

    @AfterEach
    void tearDown() {
        logger.detachAppender(listAppender);
    }

    @Test
    @DisplayName("Hizli istekte log atilmamali")
    void shouldNotLogOnFastExecution() {
        FastCommand command = new FastCommand();
        given(next.proceed()).willReturn("SUCCESS");

        String result = performanceBehavior.handle(command, next);

        assertThat(result).isEqualTo("SUCCESS");
        assertThat(listAppender.list).isEmpty();
    }

    @Test
    @DisplayName("500ms uzerindeki istekte WARN log atilmali")
    void shouldLogWarnOnSlowExecution() {
        SlowCommand command = new SlowCommand();
        given(next.proceed()).willAnswer(invocation -> {
            Thread.sleep(550); // Simulating slow execution > WARN_THRESHOLD_MS (500)
            return "SUCCESS";
        });

        String result = performanceBehavior.handle(command, next);

        assertThat(result).isEqualTo("SUCCESS");
        assertThat(listAppender.list).isNotEmpty();
        
        ILoggingEvent event = listAppender.list.get(0);
        assertThat(event.getLevel()).isEqualTo(Level.WARN);
        assertThat(event.getMessage()).contains("Yavaş istek uyarısı");
        assertThat(event.getMessage()).contains("SlowCommand");
    }

    @Test
    @DisplayName("2000ms uzerindeki istekte ERROR log atilmali")
    void shouldLogErrorOnVerySlowExecution() {
        SlowCommand command = new SlowCommand();
        given(next.proceed()).willAnswer(invocation -> {
            Thread.sleep(2050); // Simulating very slow execution > ERROR_THRESHOLD_MS (2000)
            return "SUCCESS";
        });

        String result = performanceBehavior.handle(command, next);

        assertThat(result).isEqualTo("SUCCESS");
        assertThat(listAppender.list).isNotEmpty();
        
        ILoggingEvent event = listAppender.list.get(0);
        assertThat(event.getLevel()).isEqualTo(Level.ERROR);
        assertThat(event.getMessage()).contains("YAVAŞ İSTEK");
        assertThat(event.getMessage()).contains("Performans incelemesi gereklidir");
    }
}
