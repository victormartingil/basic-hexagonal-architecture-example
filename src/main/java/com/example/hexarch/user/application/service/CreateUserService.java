package com.example.hexarch.user.application.service;

import com.example.hexarch.user.application.model.CreateUserCommand;
import com.example.hexarch.user.application.port.CreateUserUseCase;
import com.example.hexarch.user.application.model.UserResult;
import com.example.hexarch.user.application.port.ExternalUserApiClient;
import com.example.hexarch.user.application.port.ExternalUserApiClient.ExternalUserData;
import com.example.hexarch.user.application.port.UserEventPublisher;
import com.example.hexarch.user.application.port.UserRepository;
import com.example.hexarch.user.domain.event.UserCreatedEvent;
import com.example.hexarch.user.domain.exception.UserAlreadyExistsException;
import com.example.hexarch.user.domain.model.User;
import io.micrometer.core.instrument.MeterRegistry;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Service implementation for user creation use case.
 * Orchestrates domain logic and coordinates output ports.
 */
@Service
@Transactional
public class CreateUserService implements CreateUserUseCase {

    private static final Logger log = LoggerFactory.getLogger(CreateUserService.class);

    private final UserRepository userRepository;
    private final UserEventPublisher userEventPublisher;
    private final ExternalUserApiClient externalUserApiClient;
    private final MeterRegistry meterRegistry;
    private final String environment;

    public CreateUserService(
            UserRepository userRepository,
            UserEventPublisher userEventPublisher,
            @Qualifier("httpInterface") ExternalUserApiClient externalUserApiClient,
            MeterRegistry meterRegistry,
            @Value("${ENVIRONMENT:local}") String environment
    ) {
        this.userRepository = userRepository;
        this.userEventPublisher = userEventPublisher;
        this.externalUserApiClient = externalUserApiClient;
        this.meterRegistry = meterRegistry;
        this.environment = environment;
    }

    @Override
    public UserResult execute(CreateUserCommand command) {

        log.info("Creating user: username={}, email={}", command.username(), command.email());

        if (userRepository.existsByUsername(command.username())) {
            log.warn("Username already exists: {}", command.username());
            throw new UserAlreadyExistsException(command.username());
        }

        if (userRepository.existsByEmail(command.email())) {
            log.warn("Email already exists: {}", command.email());
            throw new UserAlreadyExistsException(command.email());
        }

        ExternalUserData externalData = fetchExternalUserData();
        if (!externalData.isEmpty()) {
            log.info("External data fetched: username={}, website={}",
                    externalData.username(), externalData.website());
        }

        User user = User.create(command.username(), command.email());
        User savedUser = userRepository.save(user);

        log.info("User created successfully: userId={}, username={}, email={}",
                savedUser.getId(),
                savedUser.getUsername().getValue(),
                savedUser.getEmail().getValue());

        UserCreatedEvent event = UserCreatedEvent.from(
            savedUser.getId(),
            savedUser.getUsername().getValue(),
            savedUser.getEmail().getValue()
        );
        userEventPublisher.publish(event);

        meterRegistry.counter("users.created.total",
                              "status", "success",
                              "environment", environment)
                     .increment();

        return new UserResult(
            savedUser.getId(),
            savedUser.getUsername().getValue(),
            savedUser.getEmail().getValue(),
            savedUser.isEnabled(),
            savedUser.getCreatedAt()
        );
    }

    /**
     * Fetches enrichment data from external API (optional, non-critical).
     * Returns empty data if external API fails to avoid blocking user creation.
     */
    private ExternalUserData fetchExternalUserData() {
        try {
            return externalUserApiClient.getUserById(1)
                    .orElse(ExternalUserData.empty());
        } catch (Exception e) {
            log.warn("Failed to fetch external user data: {}. Continuing with user creation...",
                    e.getMessage());
            return ExternalUserData.empty();
        }
    }
}
