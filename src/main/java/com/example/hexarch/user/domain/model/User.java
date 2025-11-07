package com.example.hexarch.user.domain.model;

import com.example.hexarch.user.domain.model.valueobject.Email;
import com.example.hexarch.user.domain.model.valueobject.Username;
import lombok.*;

import java.time.Instant;
import java.util.UUID;

/**
 * User aggregate root. Immutable domain entity with value objects.
 */
@Getter
@EqualsAndHashCode(onlyExplicitlyIncluded = true)
@ToString
@AllArgsConstructor(access = AccessLevel.PRIVATE)
public class User {

    @EqualsAndHashCode.Include
    private final UUID id;
    private final Username username;
    private final Email email;
    private final boolean enabled;
    private final Instant createdAt;

    public static User create(String username, String email) {
        return new User(
            UUID.randomUUID(),
            Username.of(username),
            Email.of(email),
            true,
            Instant.now()
        );
    }

    public static User reconstitute(UUID id, String username, String email, boolean enabled, Instant createdAt) {
        return new User(
            id,
            Username.of(username),
            Email.of(email),
            enabled,
            createdAt
        );
    }

    public User disable() {
        return new User(this.id, this.username, this.email, false, this.createdAt);
    }

    public User enable() {
        return new User(this.id, this.username, this.email, true, this.createdAt);
    }
}
