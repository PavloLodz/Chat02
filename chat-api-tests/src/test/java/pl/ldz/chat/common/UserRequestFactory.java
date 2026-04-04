package pl.ldz.chat.common;

import pl.ldz.chat.dto.UserRequestDto;

import java.util.UUID;

/**
 * Factory for creating {@link UserRequestDto} instances used in tests.
 *
 * <p>All factory methods produce objects whose field values match the
 * validation constraints declared in the source project:
 * <ul>
 *   <li>{@code username} – not blank, max 50 chars</li>
 *   <li>{@code email}    – not blank, valid e-mail</li>
 *   <li>{@code passwordHash} – not blank</li>
 * </ul>
 */
public final class UserRequestFactory {

    private UserRequestFactory() {}

    /**
     * Returns a valid {@link UserRequestDto} with a unique username / e-mail
     * so that each test can create its own user without conflicts.
     *
     * @param role the role string to assign, e.g. "USER" or "ADMIN"
     */
    public static UserRequestDto unique(String role) {
        String uid = UUID.randomUUID().toString().replace("-", "").substring(0, 8);
        return UserRequestDto.builder()
                .username("user_" + uid)
                .email("user_" + uid + "@example.com")
                .passwordHash("pass_" + uid)
                .displayName("Display " + uid)
                .avatarUrl(null)
                .online(false)
                .role(role)
                .build();
    }

    /** Returns a valid {@link UserRequestDto} with fully explicit values. */
    public static UserRequestDto of(String username, String email, String password,
                                    String displayName, String role) {
        return UserRequestDto.builder()
                .username(username)
                .email(email)
                .passwordHash(password)
                .displayName(displayName)
                .avatarUrl(null)
                .online(false)
                .role(role)
                .build();
    }

    /** Returns a {@link UserRequestDto} with a blank username – fails validation (422). */
    public static UserRequestDto withBlankUsername() {
        return UserRequestDto.builder()
                .username("")
                .email("valid@example.com")
                .passwordHash("pass123")
                .displayName("Valid Name")
                .online(false)
                .role("USER")
                .build();
    }

    /** Returns a {@link UserRequestDto} with an invalid e-mail – fails validation (422). */
    public static UserRequestDto withInvalidEmail() {
        String uid = UUID.randomUUID().toString().replace("-", "").substring(0, 8);
        return UserRequestDto.builder()
                .username("user_" + uid)
                .email("not-an-email")
                .passwordHash("pass123")
                .displayName("Valid Name")
                .online(false)
                .role("USER")
                .build();
    }

    /** Returns a {@link UserRequestDto} with a blank password – fails validation (422). */
    public static UserRequestDto withBlankPassword() {
        String uid = UUID.randomUUID().toString().replace("-", "").substring(0, 8);
        return UserRequestDto.builder()
                .username("user_" + uid)
                .email("user_" + uid + "@example.com")
                .passwordHash("")
                .displayName("Valid Name")
                .online(false)
                .role("USER")
                .build();
    }

    /** Returns a {@link UserRequestDto} whose username is 51 chars – fails @Size(max=50). */
    public static UserRequestDto withUsernameTooLong() {
        String uid = UUID.randomUUID().toString().replace("-", "").substring(0, 8);
        return UserRequestDto.builder()
                .username("a".repeat(51))
                .email("user_" + uid + "@example.com")
                .passwordHash("pass123")
                .displayName("Valid Name")
                .online(false)
                .role("USER")
                .build();
    }

    /** Returns a completely invalid {@link UserRequestDto} – all constrained fields violated. */
    public static UserRequestDto invalid() {
        return UserRequestDto.builder()
                .username("")
                .email("not-an-email")
                .passwordHash("")
                .online(false)
                .role("USER")
                .build();
    }
}
