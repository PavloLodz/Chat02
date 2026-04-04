package pl.ldz.chat.api.common;

import java.util.Map;
import java.util.UUID;

/**
 * Builds JSON payloads for the {@code UserRequestDto} shape.
 *
 * <pre>
 * String body = UserPayload.builder()
 *     .username("alice")
 *     .email("alice@example.com")
 *     .password("secret")
 *     .role("USER")
 *     .build();
 * </pre>
 */
public final class UserPayload {

    private UserPayload() {}

    /** Generates a unique payload – useful when each test needs a fresh user. */
    public static String unique(String role) {
        String uid = UUID.randomUUID().toString().replace("-", "").substring(0, 8);
        return builder()
                .username("user_" + uid)
                .email("user_" + uid + "@example.com")
                .password("pass_" + uid)
                .displayName("Display " + uid)
                .role(role)
                .build();
    }

    public static Builder builder() { return new Builder(); }

    public static final class Builder {
        private String username    = "testuser";
        private String email       = "testuser@example.com";
        private String password    = "secret123";
        private String displayName = "Test User";
        private String avatarUrl   = null;
        private boolean online     = false;
        private String role        = "USER";

        public Builder username(String v)    { this.username    = v; return this; }
        public Builder email(String v)       { this.email       = v; return this; }
        public Builder password(String v)    { this.password    = v; return this; }
        public Builder displayName(String v) { this.displayName = v; return this; }
        public Builder avatarUrl(String v)   { this.avatarUrl   = v; return this; }
        public Builder online(boolean v)     { this.online      = v; return this; }
        public Builder role(String v)        { this.role        = v; return this; }

        /** Returns a JSON string matching {@code UserRequestDto}. */
        public String build() {
            String avatar = avatarUrl == null ? "null" : "\"" + avatarUrl + "\"";
            return """
                    {
                      "username":     "%s",
                      "email":        "%s",
                      "passwordHash": "%s",
                      "displayName":  "%s",
                      "avatarUrl":    %s,
                      "online":       %s,
                      "role":         "%s"
                    }
                    """.formatted(username, email, password, displayName, avatar, online, role);
        }
    }
}
