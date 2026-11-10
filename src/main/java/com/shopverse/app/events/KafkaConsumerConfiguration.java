package com.shopverse.app.events;
import org.springframework.context.annotation.*;
import org.springframework.kafka.listener.DefaultErrorHandler;
import org.springframework.util.backoff.FixedBackOff;
@Configuration
public class KafkaConsumerConfiguration {
    @Bean
    public DefaultErrorHandler kafkaErrorHandler() {
        // Never acknowledge a failed notification: retry until repaired, rather than silently drop.
        var handler=new DefaultErrorHandler(new FixedBackOff(1000L, FixedBackOff.UNLIMITED_ATTEMPTS));
        handler.addRetryableExceptions(Exception.class);
        return handler;
    }
}
