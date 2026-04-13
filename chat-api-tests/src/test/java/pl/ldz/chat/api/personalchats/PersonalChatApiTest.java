package pl.ldz.chat.api.personalchats;

import io.restassured.response.Response;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;
import pl.ldz.chat.common.AuthHelper;
import pl.ldz.chat.common.BaseApiTest;
import pl.ldz.chat.common.UserRequestFactory;
import pl.ldz.chat.dto.PersonalChatResponseDto;
import pl.ldz.chat.dto.UserResponseDto;

import java.util.UUID;

import static io.restassured.RestAssured.given;
import static org.assertj.core.api.Assertions.assertThat;
import static org.hamcrest.Matchers.*;

/**
 * Black-box REST Assured + TestNG tests for {@code /api/v1/personal-chats}.
 *
 * <h2>Prerequisites</h2>
 * The tests rely on the following seeded accounts created by the source
 * project's {@code DataSeeder} on startup:
 * <ul>
 *   <li>viewer  / vp  (VIEWER)</li>
 *   <li>user    / up  (USER)</li>
 *   <li>admin   / ap  (ADMIN)</li>
 *   <li>auditor / ap  (AUDITOR)</li>
 * </ul>
 *
 * <h2>Authorization matrix</h2>
 * <pre>
 * Endpoint                       VIEWER  USER  ADMIN  AUDITOR  anon
 * GET  /personal-chats             403   403    200     403     401
 * GET  /personal-chats/{id}        403   403    200     403     401
 * POST /personal-chats             403   201    201     403     401
 * PUT  /personal-chats/{id}        403   403    200     403     401
 * DELETE /personal-chats/{id}      403   403    204     403     401
 * </pre>
 *
 * <p>Note: the controller currently restricts GET/PUT/DELETE to ADMIN only,
 * while POST allows USER and ADMIN. This matrix reflects the
 * {@code @PreAuthorize} annotations in {@code PersonalChatController}.
 */
public class PersonalChatApiTest extends BaseApiTest {

    private static final String BASE_URL        = "/api/v1/personal-chats";
    private static final String USERS_URL       = "/api/v1/users";
    private static final String NON_EXISTENT_ID = "00000000-0000-0000-0000-000000000000";

    private String adminToken;
    private String userToken;
    private String viewerToken;
    private String auditorToken;

    /** Two distinct user IDs required to form a personal chat. */
    private UUID participant1Id;
    private UUID participant2Id;

    /** A personal chat created once in setUp and shared across read tests. */
    private UUID seedChatId;

    @BeforeClass(alwaysRun = true)
    public void setUp() {
        adminToken   = AuthHelper.loginAsAdmin(requestSpec, objectMapper).getToken();
        userToken    = AuthHelper.loginAsUser(requestSpec, objectMapper).getToken();
        viewerToken  = AuthHelper.loginAsViewer(requestSpec, objectMapper).getToken();
        auditorToken = AuthHelper.loginAsAuditor(requestSpec, objectMapper).getToken();

        // Create two distinct participants via the Users API
        participant1Id = createUser("USER");
        participant2Id = createUser("USER");

        // Create a seed personal chat for read / update / delete tests
        seedChatId = createPersonalChat(participant1Id, participant2Id, adminToken);
    }

    // =========================================================================
    // Helpers
    // =========================================================================

    private UUID createUser(String role) {
        try {
            String body = objectMapper.writeValueAsString(UserRequestFactory.unique(role));
            Response resp = given()
                    .spec(withToken(adminToken))
                    .body(body)
                    .when()
                    .post(USERS_URL)
                    .then()
                    .statusCode(201)
                    .extract().response();
            return objectMapper.readValue(resp.asString(), UserResponseDto.class).getId();
        } catch (Exception e) {
            throw new RuntimeException("Failed to create test user", e);
        }
    }

    private UUID createPersonalChat(UUID user1Id, UUID user2Id, String token) {
        String body = personalChatPayload(user1Id, user2Id);
        try {
            Response resp = given()
                    .spec(withToken(token))
                    .body(body)
                    .when()
                    .post(BASE_URL)
                    .then()
                    .statusCode(201)
                    .extract().response();
            return objectMapper.readValue(resp.asString(), PersonalChatResponseDto.class).getId();
        } catch (Exception e) {
            throw new RuntimeException("Failed to create personal chat", e);
        }
    }

    private String personalChatPayload(UUID user1Id, UUID user2Id) {
        return """
                {
                  "user1Id": "%s",
                  "user2Id": "%s"
                }
                """.formatted(user1Id, user2Id);
    }

    // =========================================================================
    // GET /api/v1/personal-chats  (paginated list)
    // =========================================================================

    @Test(groups = "list",
          description = "GET /personal-chats → 200 with Spring Page structure for ADMIN")
    public void getAll_asAdmin_returns200WithPageStructure() {
        given()
                .spec(withToken(adminToken))
                .when()
                .get(BASE_URL)
                .then()
                .statusCode(200)
                .body("content",       notNullValue())
                .body("totalElements", greaterThanOrEqualTo(0))
                .body("pageable",      notNullValue());
    }

    @Test(groups = "list",
          description = "GET /personal-chats → 403 for VIEWER")
    public void getAll_asViewer_returns401() {
        given().spec(withToken(viewerToken)).when().get(BASE_URL).then().statusCode(401);
    }

    @Test(groups = "list",
          description = "GET /personal-chats → 403 for USER")
    public void getAll_asUser_returns401() {
        given().spec(withToken(userToken)).when().get(BASE_URL).then().statusCode(401);
    }

    @Test(groups = "list",
          description = "GET /personal-chats → 403 for AUDITOR")
    public void getAll_asAuditor_returns401() {
        given().spec(withToken(auditorToken)).when().get(BASE_URL).then().statusCode(401);
    }

    @Test(groups = "list",
          description = "GET /personal-chats → 401 without token")
    public void getAll_unauthenticated_returns401() {
        given().spec(requestSpec).when().get(BASE_URL).then().statusCode(401);
    }

    @Test(groups = "list",
          description = "GET /personal-chats → 401 with malformed JWT")
    public void getAll_malformedToken_returns401() {
        given()
                .spec(requestSpec)
                .header("Authorization", "Bearer not.a.valid.token")
                .when()
                .get(BASE_URL)
                .then()
                .statusCode(401);
    }

    @Test(groups = "list",
          description = "GET /personal-chats supports pagination parameters")
    public void getAll_withPagination_returnsCorrectPage() {
        given()
                .spec(withToken(adminToken))
                .queryParam("page", 0)
                .queryParam("size", 5)
                .when()
                .get(BASE_URL)
                .then()
                .statusCode(200)
                .body("size",   equalTo(5))
                .body("number", equalTo(0));
    }

    // =========================================================================
    // GET /api/v1/personal-chats/{id}
    // =========================================================================

    @Test(groups = "read",
          description = "GET /personal-chats/{id} → 200 with correct fields for ADMIN")
    public void getById_asAdmin_returns200WithFields() throws Exception {
        Response resp = given()
                .spec(withToken(adminToken))
                .when()
                .get(BASE_URL + "/" + seedChatId)
                .then()
                .statusCode(200)
                .body("id",    notNullValue())
                .body("user1", notNullValue())
                .body("user2", notNullValue())
                .extract().response();

        PersonalChatResponseDto dto = objectMapper.readValue(resp.asString(), PersonalChatResponseDto.class);
        assertThat(dto.getId()).isEqualTo(seedChatId);
        assertThat(dto.getUser1()).isNotNull();
        assertThat(dto.getUser2()).isNotNull();
    }

    @Test(groups = "read",
          description = "GET /personal-chats/{id} → 403 for VIEWER")
    public void getById_asViewer_returns401() {
        given()
                .spec(withToken(viewerToken))
                .when()
                .get(BASE_URL + "/" + seedChatId)
                .then()
                .statusCode(401);
    }

    @Test(groups = "read",
          description = "GET /personal-chats/{id} → 403 for USER")
    public void getById_asUser_returns401() {
        given()
                .spec(withToken(userToken))
                .when()
                .get(BASE_URL + "/" + seedChatId)
                .then()
                .statusCode(401);
    }

    @Test(groups = "read",
          description = "GET /personal-chats/{id} → 403 for AUDITOR")
    public void getById_asAuditor_returns401() {
        given()
                .spec(withToken(auditorToken))
                .when()
                .get(BASE_URL + "/" + seedChatId)
                .then()
                .statusCode(401);
    }

    @Test(groups = "read",
          description = "GET /personal-chats/{id} → 401 without token")
    public void getById_unauthenticated_returns401() {
        given()
                .spec(requestSpec)
                .when()
                .get(BASE_URL + "/" + seedChatId)
                .then()
                .statusCode(401);
    }

    @Test(groups = "read",
          description = "GET /personal-chats/{id} → 404 for non-existent ID")
    public void getById_nonExistentId_returns404() {
        given()
                .spec(withToken(adminToken))
                .when()
                .get(BASE_URL + "/" + NON_EXISTENT_ID)
                .then()
                .statusCode(404);
    }

    // =========================================================================
    // POST /api/v1/personal-chats
    // =========================================================================

    @Test(groups = "create",
          description = "POST /personal-chats → 201 for ADMIN with valid payload")
    public void create_asAdmin_returns201WithBody() throws Exception {
        UUID u1 = createUser("USER");
        UUID u2 = createUser("USER");
        String body = personalChatPayload(u1, u2);

        Response resp = given()
                .spec(withToken(adminToken))
                .body(body)
                .when()
                .post(BASE_URL)
                .then()
                .statusCode(201)
                .body("id",    notNullValue())
                .body("user1", notNullValue())
                .body("user2", notNullValue())
                .extract().response();

        PersonalChatResponseDto dto = objectMapper.readValue(resp.asString(), PersonalChatResponseDto.class);
        assertThat(dto.getId()).isNotNull();
        assertThat(dto.getUser1().getId()).isEqualTo(u1);
        assertThat(dto.getUser2().getId()).isEqualTo(u2);
    }

    @Test(groups = "create",
          description = "POST /personal-chats → 201 for USER with valid payload")
    public void create_asUser_returns201() {
        UUID u1 = createUser("USER");
        UUID u2 = createUser("USER");
        String body = personalChatPayload(u1, u2);

        given()
                .spec(withToken(userToken))
                .body(body)
                .when()
                .post(BASE_URL)
                .then()
                .statusCode(201);
    }

    @Test(groups = "create",
          description = "POST /personal-chats → 403 for VIEWER")
    public void create_asViewer_returns401() {
        UUID u1 = createUser("USER");
        UUID u2 = createUser("USER");

        given()
                .spec(withToken(viewerToken))
                .body(personalChatPayload(u1, u2))
                .when()
                .post(BASE_URL)
                .then()
                .statusCode(401);
    }

    @Test(groups = "create",
          description = "POST /personal-chats → 403 for AUDITOR")
    public void create_asAuditor_returns401() {
        UUID u1 = createUser("USER");
        UUID u2 = createUser("USER");

        given()
                .spec(withToken(auditorToken))
                .body(personalChatPayload(u1, u2))
                .when()
                .post(BASE_URL)
                .then()
                .statusCode(401);
    }

    @Test(groups = "create",
          description = "POST /personal-chats → 401 without token")
    public void create_unauthenticated_returns401() {
        UUID u1 = createUser("USER");
        UUID u2 = createUser("USER");

        given()
                .spec(requestSpec)
                .body(personalChatPayload(u1, u2))
                .when()
                .post(BASE_URL)
                .then()
                .statusCode(401);
    }

    @Test(groups = "create",
          description = "POST /personal-chats → 422 when user1Id is missing")
    public void create_missingUser1Id_returns422() {
        String body = """
                {
                  "user2Id": "%s"
                }
                """.formatted(participant2Id);

        given()
                .spec(withToken(adminToken))
                .body(body)
                .when()
                .post(BASE_URL)
                .then()
                .statusCode(422);
    }

    @Test(groups = "create",
          description = "POST /personal-chats → 422 when user2Id is missing")
    public void create_missingUser2Id_returns422() {
        String body = """
                {
                  "user1Id": "%s"
                }
                """.formatted(participant1Id);

        given()
                .spec(withToken(adminToken))
                .body(body)
                .when()
                .post(BASE_URL)
                .then()
                .statusCode(422);
    }

    @Test(groups = "create",
          description = "POST /personal-chats → 422 when both user IDs are missing")
    public void create_emptyBody_returns422() {
        given()
                .spec(withToken(adminToken))
                .body("{}")
                .when()
                .post(BASE_URL)
                .then()
                .statusCode(422);
    }

    // =========================================================================
    // PUT /api/v1/personal-chats/{id}
    // =========================================================================

    @Test(groups = "update",
          description = "PUT /personal-chats/{id} → 200 for ADMIN with swapped participants")
    public void update_asAdmin_returns200WithUpdatedParticipants() throws Exception {
        UUID u1 = createUser("USER");
        UUID u2 = createUser("USER");
        UUID chatId = createPersonalChat(u1, u2, adminToken);

        // Swap participants in the update
        UUID u3 = createUser("USER");
        String body = personalChatPayload(u2, u3);

        Response resp = given()
                .spec(withToken(adminToken))
                .body(body)
                .when()
                .put(BASE_URL + "/" + chatId)
                .then()
                .statusCode(200)
                .body("id", equalTo(chatId.toString()))
                .extract().response();

        PersonalChatResponseDto dto = objectMapper.readValue(resp.asString(), PersonalChatResponseDto.class);
        assertThat(dto.getId()).isEqualTo(chatId);
    }

    @Test(groups = "update",
          description = "PUT /personal-chats/{id} → 403 for USER")
    public void update_asUser_returns401() {
        UUID u1 = createUser("USER");
        UUID u2 = createUser("USER");
        String body = personalChatPayload(u1, u2);

        given()
                .spec(withToken(userToken))
                .body(body)
                .when()
                .put(BASE_URL + "/" + seedChatId)
                .then()
                .statusCode(401);
    }

    @Test(groups = "update",
          description = "PUT /personal-chats/{id} → 403 for VIEWER")
    public void update_asViewer_returns401() {
        String body = personalChatPayload(participant1Id, participant2Id);

        given()
                .spec(withToken(viewerToken))
                .body(body)
                .when()
                .put(BASE_URL + "/" + seedChatId)
                .then()
                .statusCode(401);
    }

    @Test(groups = "update",
          description = "PUT /personal-chats/{id} → 403 for AUDITOR")
    public void update_asAuditor_returns401() {
        String body = personalChatPayload(participant1Id, participant2Id);

        given()
                .spec(withToken(auditorToken))
                .body(body)
                .when()
                .put(BASE_URL + "/" + seedChatId)
                .then()
                .statusCode(401);
    }

    @Test(groups = "update",
          description = "PUT /personal-chats/{id} → 401 without token")
    public void update_unauthenticated_returns401() {
        String body = personalChatPayload(participant1Id, participant2Id);

        given()
                .spec(requestSpec)
                .body(body)
                .when()
                .put(BASE_URL + "/" + seedChatId)
                .then()
                .statusCode(401);
    }

    @Test(groups = "update",
          description = "PUT /personal-chats/{id} → 404 for non-existent ID")
    public void update_nonExistentId_returns404() {
        String body = personalChatPayload(participant1Id, participant2Id);

        given()
                .spec(withToken(adminToken))
                .body(body)
                .when()
                .put(BASE_URL + "/" + NON_EXISTENT_ID)
                .then()
                .statusCode(404);
    }

    // =========================================================================
    // DELETE /api/v1/personal-chats/{id}
    // =========================================================================

    @Test(groups = "delete",
          description = "DELETE /personal-chats/{id} → 204 for ADMIN")
    public void delete_asAdmin_returns204() {
        UUID u1 = createUser("USER");
        UUID u2 = createUser("USER");
        UUID chatId = createPersonalChat(u1, u2, adminToken);

        given()
                .spec(withToken(adminToken))
                .when()
                .delete(BASE_URL + "/" + chatId)
                .then()
                .statusCode(204);

        // Verify it's gone
        given()
                .spec(withToken(adminToken))
                .when()
                .get(BASE_URL + "/" + chatId)
                .then()
                .statusCode(404);
    }

    @Test(groups = "delete",
          description = "DELETE /personal-chats/{id} → 403 for USER")
    public void delete_asUser_returns401() {
        given()
                .spec(withToken(userToken))
                .when()
                .delete(BASE_URL + "/" + seedChatId)
                .then()
                .statusCode(401);
    }

    @Test(groups = "delete",
          description = "DELETE /personal-chats/{id} → 403 for VIEWER")
    public void delete_asViewer_returns401() {
        given()
                .spec(withToken(viewerToken))
                .when()
                .delete(BASE_URL + "/" + seedChatId)
                .then()
                .statusCode(401);
    }

    @Test(groups = "delete",
          description = "DELETE /personal-chats/{id} → 403 for AUDITOR")
    public void delete_asAuditor_returns401() {
        given()
                .spec(withToken(auditorToken))
                .when()
                .delete(BASE_URL + "/" + seedChatId)
                .then()
                .statusCode(401);
    }

    @Test(groups = "delete",
          description = "DELETE /personal-chats/{id} → 401 without token")
    public void delete_unauthenticated_returns401() {
        given()
                .spec(requestSpec)
                .when()
                .delete(BASE_URL + "/" + seedChatId)
                .then()
                .statusCode(401);
    }

    @Test(groups = "delete",
          description = "DELETE /personal-chats/{id} → 404 for non-existent ID")
    public void delete_nonExistentId_returns404() {
        given()
                .spec(withToken(adminToken))
                .when()
                .delete(BASE_URL + "/" + NON_EXISTENT_ID)
                .then()
                .statusCode(404);
    }
}
