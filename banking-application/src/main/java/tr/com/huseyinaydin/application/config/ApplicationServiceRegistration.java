package tr.com.huseyinaydin.application.config;

import org.springframework.context.support.GenericApplicationContext;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;
import tr.com.huseyinaydin.application.cqrs.SpringMediator;


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
        ThreadPoolTaskExecutor executor = new ThreadPoolTaskExecutor();
        executor.setCorePoolSize(5);
        executor.setMaxPoolSize(20);
        executor.setQueueCapacity(100);
        executor.setThreadNamePrefix("banking-async-");
        executor.initialize();
        return executor;
    }
}
