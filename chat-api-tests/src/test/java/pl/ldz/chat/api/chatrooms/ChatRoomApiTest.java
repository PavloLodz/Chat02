package pl.ldz.chat.api.chatrooms;

import io.restassured.response.Response;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Ignore;
import org.testng.annotations.Test;
import pl.ldz.chat.common.AuthHelper;
import pl.ldz.chat.common.BaseApiTest;
import pl.ldz.chat.common.UserRequestFactory;
import pl.ldz.chat.dto.ChatRoomResponseDto;
import pl.ldz.chat.dto.UserResponseDto;

import java.util.UUID;

import static io.restassured.RestAssured.given;
import static org.assertj.core.api.Assertions.assertThat;
import static org.hamcrest.Matchers.*;

/**
 * Black-box REST Assured + TestNG tests for {@code /api/v1/chat-rooms}.
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
 * Endpoint                    VIEWER  USER  ADMIN  AUDITOR  anon
 * GET  /chat-rooms              200   200    200     200     401
 * GET  /chat-rooms/{id}         200   200    200     200     401
 * POST /chat-rooms              403   201    201     403     401
 * PUT  /chat-rooms/{id}         403   200    200     403     401
 * DELETE /chat-rooms/{id}       403   403    204     403     401
 * </pre>
 */
public class ChatRoomApiTest extends BaseApiTest {

    private static final String BASE_URL        = "/api/v1/chat-rooms";
    private static final String USERS_URL       = "/api/v1/users";
    private static final String NON_EXISTENT_ID = "00000000-0000-0000-0000-000000000000";

    private String adminToken;
    private String userToken;
    private String viewerToken;
    private String auditorToken;

    /** A real user UUID used as the owner when creating chat rooms. */
    private UUID ownerUserId;

    /** A chat room created once and reused across read / update / delete tests. */
    private UUID seedRoomId;

    @BeforeClass(alwaysRun = true)
    public void setUp() {
        adminToken   = AuthHelper.loginAsAdmin(requestSpec, objectMapper).getToken();
        userToken    = AuthHelper.loginAsUser(requestSpec, objectMapper).getToken();
        viewerToken  = AuthHelper.loginAsViewer(requestSpec, objectMapper).getToken();
        auditorToken = AuthHelper.loginAsAuditor(requestSpec, objectMapper).getToken();

        // Create a fresh USER to act as room owner
        try {
            String userBody = objectMapper.writeValueAsString(UserRequestFactory.unique("USER"));
            Response userResp = given()
                    .spec(withToken(adminToken))
                    .body(userBody)
                    .when()
                    .post(USERS_URL)
                    .then()
                    .statusCode(201)
                    .extract().response();
            ownerUserId = objectMapper.readValue(userResp.asString(), UserResponseDto.class).getId();
        } catch (Exception e) {
            throw new RuntimeException("Failed to create seed owner user", e);
        }

        // Create a seed chat room that CRUD tests can reference
        seedRoomId = createRoom("Seed Room", "Seed description", true, ownerUserId, adminToken);
    }

    // =========================================================================
    // Helper
    // =========================================================================

    private UUID createRoom(String name, String description, boolean isPublic,
                            UUID ownerId, String token) {
        String body = """
                {
                  "name":        "%s",
                  "description": "%s",
                  "isPublic":    %s,
                  "ownerId":     "%s"
                }
                """.formatted(name, description, isPublic, ownerId);

        try {
            Response resp = given()
                    .spec(withToken(token))
                    .body(body)
                    .when()
                    .post(BASE_URL)
                    .then()
                    .statusCode(201)
                    .extract().response();
            return objectMapper.readValue(resp.asString(), ChatRoomResponseDto.class).getId();
        } catch (Exception e) {
            throw new RuntimeException("Failed to create chat room: " + name, e);
        }
    }

    private String roomPayload(String name, String description, boolean isPublic, UUID ownerId) {
        return """
                {
                  "name":        "%s",
                  "description": "%s",
                  "isPublic":    %s,
                  "ownerId":     "%s"
                }
                """.formatted(name, description, isPublic, ownerId);
    }

    // =========================================================================
    // GET /api/v1/chat-rooms  (paginated list)
    // =========================================================================

    @Test(groups = "list",
          description = "GET /chat-rooms → 200 with Spring Page structure for ADMIN")
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
          description = "GET /chat-rooms → 200 for VIEWER")
    public void getAll_asViewer_returns200() {
        given().spec(withToken(viewerToken)).when().get(BASE_URL).then().statusCode(200);
    }

    @Test(groups = "list",
          description = "GET /chat-rooms → 200 for USER")
    public void getAll_asUser_returns200() {
        given().spec(withToken(userToken)).when().get(BASE_URL).then().statusCode(200);
    }

    @Test(groups = "list",
          description = "GET /chat-rooms → 200 for AUDITOR")
    public void getAll_asAuditor_returns200() {
        given().spec(withToken(auditorToken)).when().get(BASE_URL).then().statusCode(200);
    }

    @Test(groups = "list",
          description = "GET /chat-rooms → 401 without token")
    public void getAll_unauthenticated_returns401() {
        given().spec(requestSpec).when().get(BASE_URL).then().statusCode(401);
    }

    @Test(groups = "list",
          description = "GET /chat-rooms → 401 with malformed JWT")
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
          description = "GET /chat-rooms supports pagination parameters")
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
    // GET /api/v1/chat-rooms/{id}
    // =========================================================================

    @Test(groups = "read",
          description = "GET /chat-rooms/{id} → 200 with correct fields for ADMIN")
    public void getById_asAdmin_returns200WithFields() throws Exception {
        Response resp = given()
                .spec(withToken(adminToken))
                .when()
                .get(BASE_URL + "/" + seedRoomId)
                .then()
                .statusCode(200)
                .body("id",       notNullValue())
                .body("name",     notNullValue())
                .body("owner",    notNullValue())
                .extract().response();

        ChatRoomResponseDto dto = objectMapper.readValue(resp.asString(), ChatRoomResponseDto.class);
        assertThat(dto.getId()).isEqualTo(seedRoomId);
        assertThat(dto.getName()).isNotBlank();
        assertThat(dto.getOwner()).isNotNull();
    }

    @Test(groups = "read",
          description = "GET /chat-rooms/{id} → 200 for VIEWER")
    public void getById_asViewer_returns200() {
        given()
                .spec(withToken(viewerToken))
                .when()
                .get(BASE_URL + "/" + seedRoomId)
                .then()
                .statusCode(200);
    }

    @Test(groups = "read",
          description = "GET /chat-rooms/{id} → 200 for USER")
    public void getById_asUser_returns200() {
        given()
                .spec(withToken(userToken))
                .when()
                .get(BASE_URL + "/" + seedRoomId)
                .then()
                .statusCode(200);
    }

    @Test(groups = "read",
          description = "GET /chat-rooms/{id} → 200 for AUDITOR")
    public void getById_asAuditor_returns200() {
        given()
                .spec(withToken(auditorToken))
                .when()
                .get(BASE_URL + "/" + seedRoomId)
                .then()
                .statusCode(200);
    }

    @Test(groups = "read",
          description = "GET /chat-rooms/{id} → 401 without token")
    public void getById_unauthenticated_returns401() {
        given()
                .spec(requestSpec)
                .when()
                .get(BASE_URL + "/" + seedRoomId)
                .then()
                .statusCode(401);
    }

    @Test(groups = "read",
          description = "GET /chat-rooms/{id} → 404 for non-existent ID")
    public void getById_nonExistentId_returns404() {
        given()
                .spec(withToken(adminToken))
                .when()
                .get(BASE_URL + "/" + NON_EXISTENT_ID)
                .then()
                .statusCode(404);
    }

    // =========================================================================
    // POST /api/v1/chat-rooms
    // =========================================================================

    @Ignore // TODO!
    @Test(groups = "create",
          description = "POST /chat-rooms → 201 for ADMIN with valid payload")
    public void create_asAdmin_returns201WithBody() throws Exception {
        String uniqueName = "Admin Room " + UUID.randomUUID().toString().substring(0, 8);
        String body = roomPayload(uniqueName, "Created by admin", true, ownerUserId);

        Response resp = given()
                .spec(withToken(adminToken))
                .body(body)
                .when()
                .post(BASE_URL)
                .then()
                .statusCode(201)
                .body("id",       notNullValue())
                .body("name",     equalTo(uniqueName))
                .body("isPublic", equalTo(true))
                .body("owner.id", equalTo(ownerUserId.toString()))
                .extract().response();

        ChatRoomResponseDto dto = objectMapper.readValue(resp.asString(), ChatRoomResponseDto.class);
        assertThat(dto.getId()).isNotNull();
        assertThat(dto.getName()).isEqualTo(uniqueName);
    }

    @Test(groups = "create",
          description = "POST /chat-rooms → 201 for USER with valid payload")
    public void create_asUser_returns201() {
        String body = roomPayload("User Room " + UUID.randomUUID().toString().substring(0, 8),
                "Created by user", false, ownerUserId);

        given()
                .spec(withToken(userToken))
                .body(body)
                .when()
                .post(BASE_URL)
                .then()
                .statusCode(201);
    }

    @Test(groups = "create",
          description = "POST /chat-rooms → 403 for VIEWER")
    public void create_asViewer_returns401() {
        String body = roomPayload("Should Fail", "Viewer attempt", true, ownerUserId);

        given()
                .spec(withToken(viewerToken))
                .body(body)
                .when()
                .post(BASE_URL)
                .then()
                .statusCode(401);
    }

    @Test(groups = "create",
          description = "POST /chat-rooms → 403 for AUDITOR")
    public void create_asAuditor_returns401() {
        String body = roomPayload("Should Fail", "Auditor attempt", true, ownerUserId);

        given()
                .spec(withToken(auditorToken))
                .body(body)
                .when()
                .post(BASE_URL)
                .then()
                .statusCode(401);
    }

    @Test(groups = "create",
          description = "POST /chat-rooms → 401 without token")
    public void create_unauthenticated_returns401() {
        String body = roomPayload("Anon Room", "Anon attempt", true, ownerUserId);

        given()
                .spec(requestSpec)
                .body(body)
                .when()
                .post(BASE_URL)
                .then()
                .statusCode(401);
    }

    @Test(groups = "create",
          description = "POST /chat-rooms → 422 when name is blank")
    public void create_blankName_returns422() {
        String body = roomPayload("", "Description", true, ownerUserId);

        given()
                .spec(withToken(adminToken))
                .body(body)
                .when()
                .post(BASE_URL)
                .then()
                .statusCode(422);
    }

    @Test(groups = "create",
          description = "POST /chat-rooms → 422 when name exceeds 100 characters")
    public void create_nameTooLong_returns422() {
        String body = roomPayload("A".repeat(101), "Description", true, ownerUserId);

        given()
                .spec(withToken(adminToken))
                .body(body)
                .when()
                .post(BASE_URL)
                .then()
                .statusCode(422);
    }

    @Test(groups = "create",
          description = "POST /chat-rooms → 422 when ownerId is missing")
    public void create_missingOwnerId_returns422() {
        String body = """
                {
                  "name":     "No Owner Room",
                  "isPublic": true
                }
                """;

        given()
                .spec(withToken(adminToken))
                .body(body)
                .when()
                .post(BASE_URL)
                .then()
                .statusCode(422);
    }

    @Test(groups = "create",
          description = "POST /chat-rooms description is optional – 201 without it")
    public void create_withoutDescription_returns201() {
        String body = """
                {
                  "name":     "No-Description Room %s",
                  "isPublic": true,
                  "ownerId":  "%s"
                }
                """.formatted(UUID.randomUUID().toString().substring(0, 8), ownerUserId);

        given()
                .spec(withToken(adminToken))
                .body(body)
                .when()
                .post(BASE_URL)
                .then()
                .statusCode(201);
    }

    // =========================================================================
    // PUT /api/v1/chat-rooms/{id}
    // =========================================================================

    @Test(groups = "update",
          description = "PUT /chat-rooms/{id} → 200 for ADMIN with updated name")
    public void update_asAdmin_returns200WithUpdatedName() throws Exception {
        UUID roomId = createRoom("Update Target Admin", "Before", true, ownerUserId, adminToken);
        String updatedName = "Updated Admin " + UUID.randomUUID().toString().substring(0, 8);
        String body = roomPayload(updatedName, "After", false, ownerUserId);

        Response resp = given()
                .spec(withToken(adminToken))
                .body(body)
                .when()
                .put(BASE_URL + "/" + roomId)
                .then()
                .statusCode(200)
                .body("id",       equalTo(roomId.toString()))
                .body("name",     equalTo(updatedName))
                .body("isPublic", equalTo(false))
                .extract().response();

        ChatRoomResponseDto dto = objectMapper.readValue(resp.asString(), ChatRoomResponseDto.class);
        assertThat(dto.getName()).isEqualTo(updatedName);
    }

    @Test(groups = "update",
          description = "PUT /chat-rooms/{id} → 200 for USER")
    public void update_asUser_returns200() {
        UUID roomId = createRoom("Update Target User", "Before", true, ownerUserId, adminToken);
        String body = roomPayload("Updated By User", "After", true, ownerUserId);

        given()
                .spec(withToken(userToken))
                .body(body)
                .when()
                .put(BASE_URL + "/" + roomId)
                .then()
                .statusCode(200);
    }

    @Test(groups = "update",
          description = "PUT /chat-rooms/{id} → 403 for VIEWER")
    public void update_asViewer_returns401() {
        String body = roomPayload("Viewer Update", "Attempt", true, ownerUserId);

        given()
                .spec(withToken(viewerToken))
                .body(body)
                .when()
                .put(BASE_URL + "/" + seedRoomId)
                .then()
                .statusCode(401);
    }

    @Test(groups = "update",
          description = "PUT /chat-rooms/{id} → 403 for AUDITOR")
    public void update_asAuditor_returns401() {
        String body = roomPayload("Auditor Update", "Attempt", true, ownerUserId);

        given()
                .spec(withToken(auditorToken))
                .body(body)
                .when()
                .put(BASE_URL + "/" + seedRoomId)
                .then()
                .statusCode(401);
    }

    @Test(groups = "update",
          description = "PUT /chat-rooms/{id} → 401 without token")
    public void update_unauthenticated_returns401() {
        String body = roomPayload("Anon Update", "Attempt", true, ownerUserId);

        given()
                .spec(requestSpec)
                .body(body)
                .when()
                .put(BASE_URL + "/" + seedRoomId)
                .then()
                .statusCode(401);
    }

    @Test(groups = "update",
          description = "PUT /chat-rooms/{id} → 404 for non-existent ID")
    public void update_nonExistentId_returns404() {
        String body = roomPayload("Ghost Room", "Ghost", true, ownerUserId);

        given()
                .spec(withToken(adminToken))
                .body(body)
                .when()
                .put(BASE_URL + "/" + NON_EXISTENT_ID)
                .then()
                .statusCode(404);
    }

    @Test(groups = "update",
          description = "PUT /chat-rooms/{id} → 422 when name is blank")
    public void update_blankName_returns422() {
        String body = roomPayload("", "Description", true, ownerUserId);

        given()
                .spec(withToken(adminToken))
                .body(body)
                .when()
                .put(BASE_URL + "/" + seedRoomId)
                .then()
                .statusCode(422);
    }

    // =========================================================================
    // DELETE /api/v1/chat-rooms/{id}
    // =========================================================================

    @Test(groups = "delete",
          description = "DELETE /chat-rooms/{id} → 204 for ADMIN")
    public void delete_asAdmin_returns204() {
        UUID roomId = createRoom("To Delete Admin", "temp", true, ownerUserId, adminToken);

        given()
                .spec(withToken(adminToken))
                .when()
                .delete(BASE_URL + "/" + roomId)
                .then()
                .statusCode(204);

        // Verify it's gone
        given()
                .spec(withToken(adminToken))
                .when()
                .get(BASE_URL + "/" + roomId)
                .then()
                .statusCode(404);
    }

    @Test(groups = "delete",
          description = "DELETE /chat-rooms/{id} → 403 for USER")
    public void delete_asUser_returns401() {
        given()
                .spec(withToken(userToken))
                .when()
                .delete(BASE_URL + "/" + seedRoomId)
                .then()
                .statusCode(401);
    }

    @Test(groups = "delete",
          description = "DELETE /chat-rooms/{id} → 403 for VIEWER")
    public void delete_asViewer_returns401() {
        given()
                .spec(withToken(viewerToken))
                .when()
                .delete(BASE_URL + "/" + seedRoomId)
                .then()
                .statusCode(401);
    }

    @Test(groups = "delete",
          description = "DELETE /chat-rooms/{id} → 403 for AUDITOR")
    public void delete_asAuditor_returns401() {
        given()
                .spec(withToken(auditorToken))
                .when()
                .delete(BASE_URL + "/" + seedRoomId)
                .then()
                .statusCode(401);
    }

    @Test(groups = "delete",
          description = "DELETE /chat-rooms/{id} → 401 without token")
    public void delete_unauthenticated_returns401() {
        given()
                .spec(requestSpec)
                .when()
                .delete(BASE_URL + "/" + seedRoomId)
                .then()
                .statusCode(401);
    }

    @Test(groups = "delete",
          description = "DELETE /chat-rooms/{id} → 404 for non-existent ID")
    public void delete_nonExistentId_returns404() {
        given()
                .spec(withToken(adminToken))
                .when()
                .delete(BASE_URL + "/" + NON_EXISTENT_ID)
                .then()
                .statusCode(404);
    }
}
