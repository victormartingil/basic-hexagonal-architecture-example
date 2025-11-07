package com.example.hexarch.user.infrastructure.event;

import com.example.hexarch.user.application.port.UserEventPublisher;
import com.example.hexarch.user.domain.event.UserCreatedEvent;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

/**
 * Alternative event publisher adapter that only logs events without publishing.
 * Useful for testing and debugging. Default implementation is SpringEventUserEventPublisherAdapter.
 */
@Component
public class LogUserEventPublisherAdapter implements UserEventPublisher {

    private static final Logger logger = LoggerFactory.getLogger(LogUserEventPublisherAdapter.class);

    @Override
    public void publish(UserCreatedEvent event) {
        logger.info("[LOG ONLY] User created event published: {}", event);
    }
}
