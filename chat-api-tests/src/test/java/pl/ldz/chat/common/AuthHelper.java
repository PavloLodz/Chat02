package pl.ldz.chat.common;

import com.fasterxml.jackson.databind.ObjectMapper;
import io.restassured.response.Response;
import io.restassured.specification.RequestSpecification;
import pl.ldz.chat.dto.auth.JwtResponseDto;
import pl.ldz.chat.dto.auth.LoginRequestDto;

import static io.restassured.RestAssured.given;

/**
 * Utility that logs in with one of the seeded accounts and returns
 * a fully-deserialised {@link JwtResponseDto}.
 *
 * <p>The {@code DataSeeder} in the source project creates four users on startup:
 * <table border="1">
 *   <tr><th>username</th><th>password</th><th>role</th></tr>
 *   <tr><td>viewer</td><td>vp</td><td>VIEWER</td></tr>
 *   <tr><td>user</td><td>up</td><td>USER</td></tr>
 *   <tr><td>admin</td><td>ap</td><td>ADMIN</td></tr>
 *   <tr><td>auditor</td><td>ap</td><td>AUDITOR</td></tr>
 * </table>
 */
public final class AuthHelper {

    private static final String LOGIN_URL = "/api/v1/auth/login";

    private AuthHelper() {}

    /**
     * Performs a login request and deserialises the response into a {@link JwtResponseDto}.
     *
     * @param spec     shared unauthenticated request spec
     * @param mapper   shared {@link ObjectMapper}
     * @param username account username
     * @param password account password
     * @return deserialised JWT response
     */
    public static JwtResponseDto login(RequestSpecification spec,
                                       ObjectMapper mapper,
                                       String username,
                                       String password) {
        LoginRequestDto loginRequest = LoginRequestDto.builder()
                .username(username)
                .password(password)
                .build();

        try {
            String body = mapper.writeValueAsString(loginRequest);

            Response response = given()
                    .spec(spec)
                    .body(body)
                    .when()
                    .post(LOGIN_URL)
                    .then()
                    .statusCode(200)
                    .extract()
                    .response();

            return mapper.readValue(response.asString(), JwtResponseDto.class);
        } catch (Exception e) {
            throw new RuntimeException("Login failed for user: " + username, e);
        }
    }

    /** Convenience: log in as the seeded admin (role ADMIN, password "ap"). */
    public static JwtResponseDto loginAsAdmin(RequestSpecification spec, ObjectMapper mapper) {
        return login(spec, mapper, "admin", "ap");
    }

    /** Convenience: log in as the seeded user (role USER, password "up"). */
    public static JwtResponseDto loginAsUser(RequestSpecification spec, ObjectMapper mapper) {
        return login(spec, mapper, "user", "up");
    }

    /** Convenience: log in as the seeded viewer (role VIEWER, password "vp"). */
    public static JwtResponseDto loginAsViewer(RequestSpecification spec, ObjectMapper mapper) {
        return login(spec, mapper, "viewer", "vp");
    }

    /** Convenience: log in as the seeded auditor (role AUDITOR, password "ap"). */
    public static JwtResponseDto loginAsAuditor(RequestSpecification spec, ObjectMapper mapper) {
        return login(spec, mapper, "auditor", "ap");
    }

    /** Extracts only the token string from a login response. */
    public static String tokenFor(RequestSpecification spec, ObjectMapper mapper,
                                  String username, String password) {
        return login(spec, mapper, username, password).getToken();
    }
}
