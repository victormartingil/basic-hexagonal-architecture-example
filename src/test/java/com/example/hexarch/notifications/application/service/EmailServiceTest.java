package com.example.hexarch.notifications.application.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.assertj.core.api.Assertions.assertThatCode;

/**
 * UNIT TEST - EmailService
 * <p>
 * Test unitario puro para EmailService sin Spring Context.
 * Estos tests cubren la lógica del servicio y son contados por JaCoCo.
 * </p>
 *
 * DIFERENCIA CON EmailServiceIntegrationTest:
 * - Este test: Unit test puro (sin @SpringBootTest, sin Testcontainers)
 * - Integration test: Usa contexto completo de Spring + Docker
 *
 * @see EmailServiceIntegrationTest para tests de Circuit Breaker
 */
@ExtendWith(MockitoExtension.class)
@DisplayName("EmailService - Unit Tests")
class EmailServiceTest {

    private EmailService emailService;

    @BeforeEach
    void setUp() {
        emailService = new EmailService();
    }

    /**
     * TEST CASE 1: Should send email successfully when failure rate is 0
     */
    @Test
    @DisplayName("Should send welcome email successfully with 0% failure rate")
    void shouldSendEmailSuccessfully() {
        // GIVEN - Failure rate at 0% (no failures)
        ReflectionTestUtils.setField(emailService, "failureRatePercentage", 0);

        // WHEN / THEN - Should complete without exception
        assertThatCode(() -> emailService.sendWelcomeEmail("test@example.com", "testuser"))
                .doesNotThrowAnyException();
    }

    /**
     * TEST CASE 2: Should throw exception when failure rate is 100
     */
    @Test
    @DisplayName("Should throw RuntimeException with 100% failure rate")
    void shouldFailWithHighFailureRate() {
        // GIVEN - Failure rate at 100% (always fails)
        ReflectionTestUtils.setField(emailService, "failureRatePercentage", 100);

        // WHEN / THEN - Should throw RuntimeException
        assertThatCode(() -> emailService.sendWelcomeEmail("test@example.com", "testuser"))
                .isInstanceOf(RuntimeException.class)
                .hasMessageContaining("Email service temporarily unavailable");
    }

    /**
     * TEST CASE 3: Should handle valid email addresses
     */
    @Test
    @DisplayName("Should accept valid email addresses")
    void shouldHandleValidEmails() {
        // GIVEN
        ReflectionTestUtils.setField(emailService, "failureRatePercentage", 0);

        // WHEN / THEN - Multiple valid emails
        assertThatCode(() -> {
            emailService.sendWelcomeEmail("user@example.com", "user1");
            emailService.sendWelcomeEmail("test.user@domain.co.uk", "user2");
            emailService.sendWelcomeEmail("user+tag@example.com", "user3");
        }).doesNotThrowAnyException();
    }

    /**
     * TEST CASE 4: Should handle different usernames
     */
    @Test
    @DisplayName("Should handle different username formats")
    void shouldHandleDifferentUsernames() {
        // GIVEN
        ReflectionTestUtils.setField(emailService, "failureRatePercentage", 0);

        // WHEN / THEN - Different username formats
        assertThatCode(() -> {
            emailService.sendWelcomeEmail("test@example.com", "john_doe");
            emailService.sendWelcomeEmail("test@example.com", "user123");
            emailService.sendWelcomeEmail("test@example.com", "test-user");
        }).doesNotThrowAnyException();
    }

    /**
     * TEST CASE 5: Should verify EmailService instantiation
     */
    @Test
    @DisplayName("Should create EmailService successfully")
    void shouldCreateEmailService() {
        // GIVEN / WHEN
        EmailService service = new EmailService();

        // THEN
        assertThatCode(() -> {
            ReflectionTestUtils.setField(service, "failureRatePercentage", 0);
            service.sendWelcomeEmail("test@example.com", "testuser");
        }).doesNotThrowAnyException();
    }
}
