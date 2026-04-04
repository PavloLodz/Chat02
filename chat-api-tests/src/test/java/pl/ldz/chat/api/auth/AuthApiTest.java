package pl.ldz.chat.api.auth;

import io.restassured.response.Response;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;
import pl.ldz.chat.common.BaseApiTest;
import pl.ldz.chat.dto.auth.JwtResponseDto;
import pl.ldz.chat.dto.auth.LoginRequestDto;

import static io.restassured.RestAssured.given;
import static org.assertj.core.api.Assertions.assertThat;
import static org.hamcrest.Matchers.emptyOrNullString;
import static org.hamcrest.Matchers.not;
import static org.hamcrest.Matchers.notNullValue;

/**
 * Black-box tests for {@code POST /api/v1/auth/login}.
 *
 * <p>Groups:
 * <ul>
 *   <li>{@code happy}  – successful logins for each seeded role</li>
 *   <li>{@code unhappy} – invalid credentials, blank fields, empty body</li>
 * </ul>
 */
public class AuthApiTest extends BaseApiTest {

    private static final String LOGIN_URL = "/api/v1/auth/login";

    // ── helpers ──────────────────────────────────────────────────────────────

    private String bodyOf(LoginRequestDto dto) {
        try {
            return objectMapper.writeValueAsString(dto);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    private LoginRequestDto credentials(String username, String password) {
        return LoginRequestDto.builder().username(username).password(password).build();
    }

    // ── happy path ────────────────────────────────────────────────────────────

    @Test(groups = "happy",
          description = "POST /login → 200 + non-empty token for seeded admin (role ADMIN, pw 'ap')")
    public void loginAsAdmin_returns200WithToken() {
        Response response = given()
                .spec(requestSpec)
                .body(bodyOf(credentials("admin", "ap")))
                .when()
                .post(LOGIN_URL)
                .then()
                .statusCode(200)
                .body("token",     not(emptyOrNullString()))
                .body("expiresAt", notNullValue())
                .extract().response();

        JwtResponseDto dto = deserialize(response, JwtResponseDto.class);
        assertThat(dto.getToken()).isNotBlank();
        assertThat(dto.getExpiresAt()).isNotNull();
    }

    @Test(groups = "happy",
          description = "POST /login → 200 + non-empty token for seeded user (role USER, pw 'up')")
    public void loginAsUser_returns200WithToken() {
        given()
                .spec(requestSpec)
                .body(bodyOf(credentials("user", "up")))
                .when()
                .post(LOGIN_URL)
                .then()
                .statusCode(200)
                .body("token", not(emptyOrNullString()));
    }

    @Test(groups = "happy",
          description = "POST /login → 200 for seeded viewer (role VIEWER, pw 'vp')")
    public void loginAsViewer_returns200WithToken() {
        given()
                .spec(requestSpec)
                .body(bodyOf(credentials("viewer", "vp")))
                .when()
                .post(LOGIN_URL)
                .then()
                .statusCode(200)
                .body("token", not(emptyOrNullString()));
    }

    @Test(groups = "happy",
          description = "POST /login → 200 for seeded auditor (role AUDITOR, pw 'ap')")
    public void loginAsAuditor_returns200WithToken() {
        given()
                .spec(requestSpec)
                .body(bodyOf(credentials("auditor", "ap")))
                .when()
                .post(LOGIN_URL)
                .then()
                .statusCode(200)
                .body("token", not(emptyOrNullString()));
    }

    // ── unhappy path ──────────────────────────────────────────────────────────

    @Test(groups = "unhappy",
          description = "POST /login → 401 for wrong password")
    public void wrongPassword_returns401() {
        given()
                .spec(requestSpec)
                .body(bodyOf(credentials("admin", "wrongpassword")))
                .when()
                .post(LOGIN_URL)
                .then()
                .statusCode(401);
    }

    @Test(groups = "unhappy",
          description = "POST /login → 401 for non-existent username")
    public void unknownUser_returns401() {
        given()
                .spec(requestSpec)
                .body(bodyOf(credentials("ghost_user_xyz", "irrelevant")))
                .when()
                .post(LOGIN_URL)
                .then()
                .statusCode(401);
    }

    @Test(groups = "unhappy",
          description = "POST /login → 400 when @NotBlank username is empty string")
    public void blankUsername_returns400() {
        given()
                .spec(requestSpec)
                .body(bodyOf(credentials("", "ap")))
                .when()
                .post(LOGIN_URL)
                .then()
                .statusCode(400);
    }

    @Test(groups = "unhappy",
          description = "POST /login → 400 when @NotBlank password is empty string")
    public void blankPassword_returns400() {
        given()
                .spec(requestSpec)
                .body(bodyOf(credentials("admin", "")))
                .when()
                .post(LOGIN_URL)
                .then()
                .statusCode(400);
    }

    @Test(groups = "unhappy",
          description = "POST /login → 400 for empty JSON body (both fields missing → @NotBlank)")
    public void emptyBody_returns400() {
        given()
                .spec(requestSpec)
                .body("{}")
                .when()
                .post(LOGIN_URL)
                .then()
                .statusCode(400);
    }

    // ── private helpers ───────────────────────────────────────────────────────

    private <T> T deserialize(Response response, Class<T> clazz) {
        try {
            return objectMapper.readValue(response.asString(), clazz);
        } catch (Exception e) {
            throw new RuntimeException("Failed to deserialise response to " + clazz.getSimpleName(), e);
        }
    }
}
