package tr.com.huseyinaydin.application.config;

import org.springframework.context.support.GenericApplicationContext;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;
import tr.com.huseyinaydin.application.cqrs.SpringMediator;

/**
 * XML config veya @ComponentScan kullanılamadığı senaryolarda
 * (örn. entegrasyon testleri, embedded context) application bean'lerini
 * programmatic olarak kaydeder.
 */
public final class ApplicationServiceRegistration {

    public static final String MEDIATOR_BEAN = "mediator";
    public static final String COMMAND_BUS_BEAN = "commandBus";
    public static final String QUERY_BUS_BEAN = "queryBus";
    public static final String ASYNC_EXECUTOR_BEAN = "bankingAsyncExecutor";

    private ApplicationServiceRegistration() {
    }

    public static void registerTo(GenericApplicationContext context) {
        if (!context.containsBean(ASYNC_EXECUTOR_BEAN)) {
            context.registerBean(ASYNC_EXECUTOR_BEAN, ThreadPoolTaskExecutor.class,
                    ApplicationServiceRegistration::buildExecutor);
        }
        if (!context.containsBean(MEDIATOR_BEAN)) {
            context.registerBean(MEDIATOR_BEAN, SpringMediator.class, () -> {
                ThreadPoolTaskExecutor executor = context.getBean(ASYNC_EXECUTOR_BEAN, ThreadPoolTaskExecutor.class);
                return new SpringMediator(context, executor);
            });
        }
    }

    private static ThreadPoolTaskExecutor buildExecutor() {
        // Async işlemleri çalıştırmak için thread pool oluşturulur
        ThreadPoolTaskExecutor executor = new ThreadPoolTaskExecutor();
        // Aynı anda en az 5 iş parçacığı (thread) hazır bekler
        executor.setCorePoolSize(5);
        // Yoğunluk artarsa en fazla 20 thread'e kadar çıkar
        executor.setMaxPoolSize(20);
        // 100 işlem sırada bekleyebilir, fazlası reddedilir veya bloklanır
        executor.setQueueCapacity(100);
        // Thread isimlerine "banking-async-" ön eki eklenir (log takibi için)
        executor.setThreadNamePrefix("banking-async-");
        // Ayarlar aktif edilir ve executor başlatılır
        executor.initialize();
        // Hazırlanan thread pool döndürülür
        return executor;
    }
}
