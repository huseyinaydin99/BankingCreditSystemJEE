package tr.com.huseyinaydin.application.config;

import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;
import tr.com.huseyinaydin.application.cqrs.CommandBus;
import tr.com.huseyinaydin.application.cqrs.Mediator;
import tr.com.huseyinaydin.application.cqrs.QueryBus;
import tr.com.huseyinaydin.application.cqrs.SpringMediator;

import java.util.concurrent.Executor;

@Configuration
@ComponentScan(basePackages = "tr.com.huseyinaydin.application")
public class BankingApplicationConfig {

    @Bean(name = "bankingAsyncExecutor")
    public ThreadPoolTaskExecutor bankingAsyncExecutor() {
        ThreadPoolTaskExecutor executor = new ThreadPoolTaskExecutor();
        executor.setCorePoolSize(5);
        executor.setMaxPoolSize(20);
        executor.setQueueCapacity(100);
        executor.setThreadNamePrefix("banking-async-");
        executor.initialize();
        return executor;
    }

    @Bean
    public Mediator mediator(ApplicationContext context,
                             @Qualifier("bankingAsyncExecutor") Executor asyncExecutor) {
        return new SpringMediator(context, asyncExecutor);
    }

    @Bean
    public CommandBus commandBus(Mediator mediator) {
        return mediator;
    }

    @Bean
    public QueryBus queryBus(Mediator mediator) {
        return mediator;
    }
}
