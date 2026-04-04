package pl.ldz.chat.api.common;

import io.restassured.specification.RequestSpecification;

import static io.restassured.RestAssured.given;

/**
 * Utility that logs in with a seeded user and returns the JWT token.
 *
 * <p>The Chat02 {@code DataSeeder} seeds four users on startup:
 * <ul>
 *   <li>viewer  / vp  (role VIEWER)</li>
 *   <li>user    / up  (role USER)</li>
 *   <li>admin   / ap  (role ADMIN)</li>
 *   <li>auditor / ap  (role AUDITOR)</li>
 * </ul>
 */
public final class AuthHelper {

    private AuthHelper() {}

    public static String loginAs(RequestSpecification spec, String username, String password) {
        String body = String.format("""
                {"username": "%s", "password": "%s"}
                """, username, password);

        return given()
                .spec(spec)
                .body(body)
                .when()
                .post("/api/v1/auth/login")
                .then()
                .statusCode(200)
                .extract()
                .path("token");
    }

    // Convenience wrappers for the seeded users
    public static String adminToken(RequestSpecification spec)   { return loginAs(spec, "admin",   "ap"); }
    public static String userToken(RequestSpecification spec)    { return loginAs(spec, "user",    "up"); }
    public static String viewerToken(RequestSpecification spec)  { return loginAs(spec, "viewer",  "vp"); }
    public static String auditorToken(RequestSpecification spec) { return loginAs(spec, "auditor", "ap"); }
}
