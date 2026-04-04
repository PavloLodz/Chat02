package pl.ldz.chat.api.users;

import io.restassured.response.Response;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;
import pl.ldz.chat.common.AuthHelper;
import pl.ldz.chat.common.BaseApiTest;
import pl.ldz.chat.common.UserRequestFactory;
import pl.ldz.chat.dto.UserRequestDto;
import pl.ldz.chat.dto.UserResponseDto;

import java.util.UUID;

import static io.restassured.RestAssured.given;
import static org.assertj.core.api.Assertions.assertThat;
import static org.hamcrest.Matchers.*;

/**
 * Black-box REST Assured + TestNG tests for {@code /api/v1/users}.
 *
 * <p>Tokens are obtained once per class ({@code @BeforeClass}) via the seeded
 * accounts created by the source project's {@code DataSeeder}. DTOs mirror the
 * source project classes exactly.
 *
 * <p>Authorization matrix tested:
 * <pre>
 * Endpoint            VIEWER  USER  ADMIN  AUDITOR  anon
 * GET  /users          200    200    200     200     401
 * GET  /users/{id}     200    200    200     200     401
 * POST /users          403    403    201     403     401
 * PUT  /users/{id}     403    200    200     403     401
 * DELETE /users/{id}   403    403    204     403     401
 * </pre>
 */
public class UserApiTest extends BaseApiTest {

    private static final String USERS_URL       = "/api/v1/users";
    private static final String NON_EXISTENT_ID = "00000000-0000-0000-0000-000000000000";

    // Tokens obtained once before all tests in this class
    private String adminToken;
    private String userToken;
    private String viewerToken;
    private String auditorToken;

    @BeforeClass(alwaysRun = true)
    public void obtainTokens() {
        adminToken   = AuthHelper.loginAsAdmin(requestSpec, objectMapper).getToken();
        userToken    = AuthHelper.loginAsUser(requestSpec, objectMapper).getToken();
        viewerToken  = AuthHelper.loginAsViewer(requestSpec, objectMapper).getToken();
        auditorToken = AuthHelper.loginAsAuditor(requestSpec, objectMapper).getToken();
    }

    // =========================================================================
    // GET /api/v1/users  (paginated list)
    // =========================================================================

    @Test(groups = "list",
          description = "GET /users → 200 with Spring Page structure for ADMIN")
    public void getAll_asAdmin_returns200WithPageStructure() {
        given()
                .spec(withToken(adminToken))
                .when()
                .get(USERS_URL)
                .then()
                .statusCode(200)
                .body("content",       notNullValue())
                .body("totalElements", greaterThanOrEqualTo(0))
                .body("pageable",      notNullValue());
    }

    @Test(groups = "list",
          description = "GET /users → 200 for VIEWER")
    public void getAll_asViewer_returns200() {
        given().spec(withToken(viewerToken)).when().get(USERS_URL).then().statusCode(200);
    }

    @Test(groups = "list",
          description = "GET /users → 200 for USER")
    public void getAll_asUser_returns200() {
        given().spec(withToken(userToken)).when().get(USERS_URL).then().statusCode(200);
    }

    @Test(groups = "list",
          description = "GET /users → 200 for AUDITOR")
    public void getAll_asAuditor_returns200() {
        given().spec(withToken(auditorToken)).when().get(USERS_URL).then().statusCode(200);
    }

    @Test(groups = "list",
          description = "GET /users → 401 without a token")
    public void getAll_unauthenticated_returns401() {
        given().spec(requestSpec).when().get(USERS_URL).then().statusCode(401);
    }

    @Test(groups = "list",
          description = "GET /users → 401 with malformed JWT")
    public void getAll_malformedToken_returns401() {
        given()
                .spec(requestSpec)
                .header("Authorization", "Bearer not.a.real.jwt")
                .when()
                .get(USERS_URL)
                .then()
                .statusCode(401);
    }

    @Test(groups = "list",
          description = "GET /users?page=0&size=2 → page metadata matches requested size")
    public void getAll_paginationParams_areRespected() {
        given()
                .spec(withToken(adminToken))
                .queryParam("page", 0)
                .queryParam("size", 2)
                .queryParam("sort", "username,asc")
                .when()
                .get(USERS_URL)
                .then()
                .statusCode(200)
                .body("pageable.pageSize",   equalTo(2))
                .body("pageable.pageNumber", equalTo(0));
    }

    // =========================================================================
    // POST /api/v1/users  (create)
    // =========================================================================

    @Test(groups = "create",
          description = "POST /users → 201 with full UserResponseDto for ADMIN")
    public void create_asAdmin_returns201WithResponseDto() throws Exception {
        UserRequestDto request = UserRequestFactory.unique("USER");

        Response response = given()
                .spec(withToken(adminToken))
                .body(objectMapper.writeValueAsString(request))
                .when()
                .post(USERS_URL)
                .then()
                .statusCode(201)
                .extract().response();

        UserResponseDto created = objectMapper.readValue(response.asString(), UserResponseDto.class);

        assertThat(created.getId()).isNotNull();
        assertThat(created.getVersion()).isNotNull();
        assertThat(created.getCreationTimestamp()).isNotNull();
        assertThat(created.getUpdateTimestamp()).isNotNull();
        assertThat(created.getUsername()).isEqualTo(request.getUsername());
        assertThat(created.getEmail()).isEqualTo(request.getEmail());
        assertThat(created.getDisplayName()).isEqualTo(request.getDisplayName());
        assertThat(created.isOnline()).isFalse();
        assertThat(created.getRole()).isEqualTo(request.getRole());
    }

    @Test(groups = "create",
          description = "POST /users → 403 for VIEWER (hasRole ADMIN only)")
    public void create_asViewer_returns403() throws Exception {
        given()
                .spec(withToken(viewerToken))
                .body(objectMapper.writeValueAsString(UserRequestFactory.unique("USER")))
                .when()
                .post(USERS_URL)
                .then()
                .statusCode(403);
    }

    @Test(groups = "create",
          description = "POST /users → 403 for USER role")
    public void create_asUserRole_returns403() throws Exception {
        given()
                .spec(withToken(userToken))
                .body(objectMapper.writeValueAsString(UserRequestFactory.unique("USER")))
                .when()
                .post(USERS_URL)
                .then()
                .statusCode(403);
    }

    @Test(groups = "create",
          description = "POST /users → 403 for AUDITOR role")
    public void create_asAuditor_returns403() throws Exception {
        given()
                .spec(withToken(auditorToken))
                .body(objectMapper.writeValueAsString(UserRequestFactory.unique("USER")))
                .when()
                .post(USERS_URL)
                .then()
                .statusCode(403);
    }

    @Test(groups = "create",
          description = "POST /users → 401 without token")
    public void create_unauthenticated_returns401() throws Exception {
        given()
                .spec(requestSpec)
                .body(objectMapper.writeValueAsString(UserRequestFactory.unique("USER")))
                .when()
                .post(USERS_URL)
                .then()
                .statusCode(401);
    }

    @Test(groups = "create",
          description = "POST /users → 422 when username is blank (@NotBlank)")
    public void create_blankUsername_returns422() throws Exception {
        given()
                .spec(withToken(adminToken))
                .body(objectMapper.writeValueAsString(UserRequestFactory.withBlankUsername()))
                .when()
                .post(USERS_URL)
                .then()
                .statusCode(422);
    }

    @Test(groups = "create",
          description = "POST /users → 422 when email is invalid (@Email)")
    public void create_invalidEmail_returns422() throws Exception {
        given()
                .spec(withToken(adminToken))
                .body(objectMapper.writeValueAsString(UserRequestFactory.withInvalidEmail()))
                .when()
                .post(USERS_URL)
                .then()
                .statusCode(422);
    }

    @Test(groups = "create",
          description = "POST /users → 422 when passwordHash is blank (@NotBlank)")
    public void create_blankPassword_returns422() throws Exception {
        given()
                .spec(withToken(adminToken))
                .body(objectMapper.writeValueAsString(UserRequestFactory.withBlankPassword()))
                .when()
                .post(USERS_URL)
                .then()
                .statusCode(422);
    }

    @Test(groups = "create",
          description = "POST /users → 422 when username exceeds 50 chars (@Size max=50)")
    public void create_usernameTooLong_returns422() throws Exception {
        given()
                .spec(withToken(adminToken))
                .body(objectMapper.writeValueAsString(UserRequestFactory.withUsernameTooLong()))
                .when()
                .post(USERS_URL)
                .then()
                .statusCode(422);
    }

    // =========================================================================
    // GET /api/v1/users/{id}
    // =========================================================================

    @Test(groups = "getById",
          description = "GET /users/{id} → 200 with correct UserResponseDto for ADMIN")
    public void getById_asAdmin_returns200WithDto() throws Exception {
        // Create a user first, then fetch it
        UserRequestDto request = UserRequestFactory.unique("USER");
        UserResponseDto created = createUser(request);

        Response response = given()
                .spec(withToken(adminToken))
                .when()
                .get(USERS_URL + "/" + created.getId())
                .then()
                .statusCode(200)
                .extract().response();

        UserResponseDto fetched = objectMapper.readValue(response.asString(), UserResponseDto.class);
        assertThat(fetched.getId()).isEqualTo(created.getId());
        assertThat(fetched.getUsername()).isEqualTo(request.getUsername());
        assertThat(fetched.getEmail()).isEqualTo(request.getEmail());
    }

    @Test(groups = "getById",
          description = "GET /users/{id} → 200 for VIEWER")
    public void getById_asViewer_returns200() throws Exception {
        UserResponseDto created = createUser(UserRequestFactory.unique("USER"));
        given()
                .spec(withToken(viewerToken))
                .when()
                .get(USERS_URL + "/" + created.getId())
                .then()
                .statusCode(200);
    }

    @Test(groups = "getById",
          description = "GET /users/{id} → 200 for AUDITOR")
    public void getById_asAuditor_returns200() throws Exception {
        UserResponseDto created = createUser(UserRequestFactory.unique("USER"));
        given()
                .spec(withToken(auditorToken))
                .when()
                .get(USERS_URL + "/" + created.getId())
                .then()
                .statusCode(200);
    }

    @Test(groups = "getById",
          description = "GET /users/{id} → 404 for non-existent UUID")
    public void getById_nonExistentId_returns404() {
        given()
                .spec(withToken(adminToken))
                .when()
                .get(USERS_URL + "/" + NON_EXISTENT_ID)
                .then()
                .statusCode(404);
    }

    @Test(groups = "getById",
          description = "GET /users/{id} → 401 without token")
    public void getById_unauthenticated_returns401() {
        given()
                .spec(requestSpec)
                .when()
                .get(USERS_URL + "/" + NON_EXISTENT_ID)
                .then()
                .statusCode(401);
    }

    // =========================================================================
    // PUT /api/v1/users/{id}  (update)
    // =========================================================================

    @Test(groups = "update",
          description = "PUT /users/{id} → 200 with updated UserResponseDto for ADMIN")
    public void update_asAdmin_returns200WithUpdatedDto() throws Exception {
        UserResponseDto created = createUser(UserRequestFactory.unique("USER"));

        UserRequestDto updateRequest = UserRequestFactory.of(
                "upd_" + UUID.randomUUID().toString().substring(0, 6),
                "upd_" + UUID.randomUUID().toString().substring(0, 6) + "@example.com",
                "newpassword",
                "Updated Name",
                "USER"
        );

        Response response = given()
                .spec(withToken(adminToken))
                .body(objectMapper.writeValueAsString(updateRequest))
                .when()
                .put(USERS_URL + "/" + created.getId())
                .then()
                .statusCode(200)
                .extract().response();

        UserResponseDto updated = objectMapper.readValue(response.asString(), UserResponseDto.class);
        assertThat(updated.getId()).isEqualTo(created.getId());
        assertThat(updated.getDisplayName()).isEqualTo("Updated Name");
        assertThat(updated.getUsername()).isEqualTo(updateRequest.getUsername());
    }

    @Test(groups = "update",
          description = "PUT /users/{id} → 200 for USER role (hasAnyRole USER, ADMIN)")
    public void update_asUserRole_returns200() throws Exception {
        UserResponseDto created = createUser(UserRequestFactory.unique("USER"));

        given()
                .spec(withToken(userToken))
                .body(objectMapper.writeValueAsString(UserRequestFactory.unique("USER")))
                .when()
                .put(USERS_URL + "/" + created.getId())
                .then()
                .statusCode(200);
    }

    @Test(groups = "update",
          description = "PUT /users/{id} → 403 for VIEWER")
    public void update_asViewer_returns403() throws Exception {
        UserResponseDto created = createUser(UserRequestFactory.unique("USER"));

        given()
                .spec(withToken(viewerToken))
                .body(objectMapper.writeValueAsString(UserRequestFactory.unique("USER")))
                .when()
                .put(USERS_URL + "/" + created.getId())
                .then()
                .statusCode(403);
    }

    @Test(groups = "update",
          description = "PUT /users/{id} → 403 for AUDITOR")
    public void update_asAuditor_returns403() throws Exception {
        UserResponseDto created = createUser(UserRequestFactory.unique("USER"));

        given()
                .spec(withToken(auditorToken))
                .body(objectMapper.writeValueAsString(UserRequestFactory.unique("USER")))
                .when()
                .put(USERS_URL + "/" + created.getId())
                .then()
                .statusCode(403);
    }

    @Test(groups = "update",
          description = "PUT /users/{id} → 401 without token")
    public void update_unauthenticated_returns401() throws Exception {
        given()
                .spec(requestSpec)
                .body(objectMapper.writeValueAsString(UserRequestFactory.unique("USER")))
                .when()
                .put(USERS_URL + "/" + NON_EXISTENT_ID)
                .then()
                .statusCode(401);
    }

    @Test(groups = "update",
          description = "PUT /users/{id} → 404 when updating a non-existent user")
    public void update_nonExistentId_returns404() throws Exception {
        given()
                .spec(withToken(adminToken))
                .body(objectMapper.writeValueAsString(UserRequestFactory.unique("USER")))
                .when()
                .put(USERS_URL + "/" + NON_EXISTENT_ID)
                .then()
                .statusCode(404);
    }

    @Test(groups = "update",
          description = "PUT /users/{id} → 422 for invalid payload (blank username + bad email)")
    public void update_invalidPayload_returns422() throws Exception {
        UserResponseDto created = createUser(UserRequestFactory.unique("USER"));

        given()
                .spec(withToken(adminToken))
                .body(objectMapper.writeValueAsString(UserRequestFactory.invalid()))
                .when()
                .put(USERS_URL + "/" + created.getId())
                .then()
                .statusCode(422);
    }

    // =========================================================================
    // DELETE /api/v1/users/{id}
    // =========================================================================

    @Test(groups = "delete",
          description = "DELETE /users/{id} → 204 for ADMIN")
    public void delete_asAdmin_returns204() throws Exception {
        UserResponseDto created = createUser(UserRequestFactory.unique("USER"));

        given()
                .spec(withToken(adminToken))
                .when()
                .delete(USERS_URL + "/" + created.getId())
                .then()
                .statusCode(204);
    }

    @Test(groups = "delete",
          description = "DELETE /users/{id} → user is gone (404) after successful deletion")
    public void delete_asAdmin_userNotFoundAfterwards() throws Exception {
        UserResponseDto created = createUser(UserRequestFactory.unique("USER"));
        UUID id = created.getId();

        // Delete
        given().spec(withToken(adminToken)).when().delete(USERS_URL + "/" + id).then().statusCode(204);

        // Verify gone
        given().spec(withToken(adminToken)).when().get(USERS_URL + "/" + id).then().statusCode(404);
    }

    @Test(groups = "delete",
          description = "DELETE /users/{id} → 403 for USER role")
    public void delete_asUserRole_returns403() throws Exception {
        UserResponseDto created = createUser(UserRequestFactory.unique("USER"));

        given()
                .spec(withToken(userToken))
                .when()
                .delete(USERS_URL + "/" + created.getId())
                .then()
                .statusCode(403);
    }

    @Test(groups = "delete",
          description = "DELETE /users/{id} → 403 for VIEWER")
    public void delete_asViewer_returns403() throws Exception {
        UserResponseDto created = createUser(UserRequestFactory.unique("USER"));

        given()
                .spec(withToken(viewerToken))
                .when()
                .delete(USERS_URL + "/" + created.getId())
                .then()
                .statusCode(403);
    }

    @Test(groups = "delete",
          description = "DELETE /users/{id} → 403 for AUDITOR")
    public void delete_asAuditor_returns403() throws Exception {
        UserResponseDto created = createUser(UserRequestFactory.unique("USER"));

        given()
                .spec(withToken(auditorToken))
                .when()
                .delete(USERS_URL + "/" + created.getId())
                .then()
                .statusCode(403);
    }

    @Test(groups = "delete",
          description = "DELETE /users/{id} → 401 without token")
    public void delete_unauthenticated_returns401() {
        given()
                .spec(requestSpec)
                .when()
                .delete(USERS_URL + "/" + NON_EXISTENT_ID)
                .then()
                .statusCode(401);
    }

    @Test(groups = "delete",
          description = "DELETE /users/{id} → 404 for non-existent UUID")
    public void delete_nonExistentId_returns404() {
        given()
                .spec(withToken(adminToken))
                .when()
                .delete(USERS_URL + "/" + NON_EXISTENT_ID)
                .then()
                .statusCode(404);
    }

    // ── private helpers ───────────────────────────────────────────────────────

    /**
     * Creates a user via the API (as admin) and returns the deserialised
     * {@link UserResponseDto}. Used as a setup step inside individual tests.
     */
    private UserResponseDto createUser(UserRequestDto request) throws Exception {
        Response response = given()
                .spec(withToken(adminToken))
                .body(objectMapper.writeValueAsString(request))
                .when()
                .post(USERS_URL)
                .then()
                .statusCode(201)
                .extract().response();

        return objectMapper.readValue(response.asString(), UserResponseDto.class);
    }
}
