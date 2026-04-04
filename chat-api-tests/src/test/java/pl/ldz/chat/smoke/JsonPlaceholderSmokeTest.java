package pl.ldz.chat.smoke;

import io.restassured.builder.RequestSpecBuilder;
import io.restassured.filter.log.RequestLoggingFilter;
import io.restassured.filter.log.ResponseLoggingFilter;
import io.restassured.http.ContentType;
import io.restassured.response.Response;
import io.restassured.specification.RequestSpecification;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

import static io.restassured.RestAssured.given;
import static org.assertj.core.api.Assertions.assertThat;
import static org.hamcrest.Matchers.*;

/**
 * Stack smoke-test against the public JSONPlaceholder API (https://jsonplaceholder.typicode.com).
 *
 * <p>These tests require NO local server and exist purely to verify that the
 * project compiles, Lombok generates correctly, TestNG runs, and REST Assured
 * can execute HTTP calls with assertions.
 *
 * <p>JSONPlaceholder is a free, stable mock REST API maintained by the open-source
 * community and widely used for this exact purpose.
 */
public class JsonPlaceholderSmokeTest {

    private static final String BASE_URL = "https://jsonplaceholder.typicode.com";

    private RequestSpecification spec;

    @BeforeClass
    public void setup() {
        spec = new RequestSpecBuilder()
                .setBaseUri(BASE_URL)
                .setContentType(ContentType.JSON)
                .setAccept(ContentType.JSON)
                .addFilter(new RequestLoggingFilter())
                .addFilter(new ResponseLoggingFilter())
                .build();
    }

    // ── GET /posts/1 ──────────────────────────────────────────────────────────

    @Test(groups = "smoke",
          description = "GET /posts/1 → 200 with expected fields (Hamcrest)")
    public void getPost_returns200_withExpectedFields() {
        given()
                .spec(spec)
                .when()
                .get("/posts/1")
                .then()
                .statusCode(200)
                .body("id",     equalTo(1))
                .body("userId", equalTo(1))
                .body("title",  not(emptyOrNullString()))
                .body("body",   not(emptyOrNullString()));
    }

    @Test(groups = "smoke",
          description = "GET /posts/1 → fields are correct (AssertJ)")
    public void getPost_assertJ_fieldsAreCorrect() {
        Response response = given()
                .spec(spec)
                .when()
                .get("/posts/1")
                .then()
                .statusCode(200)
                .extract().response();

        assertThat(response.jsonPath().getInt("id")).isEqualTo(1);
        assertThat(response.jsonPath().getString("title")).isNotBlank();
        assertThat(response.jsonPath().getString("body")).isNotBlank();
    }

    // ── GET /posts → list ─────────────────────────────────────────────────────

    @Test(groups = "smoke",
          description = "GET /posts → 200 with a non-empty list of 100 posts")
    public void getPosts_returnsList_of100() {
        given()
                .spec(spec)
                .when()
                .get("/posts")
                .then()
                .statusCode(200)
                .body("size()", equalTo(100));
    }

    // ── POST /posts ───────────────────────────────────────────────────────────

    @Test(groups = "smoke",
          description = "POST /posts → 201 and echo back the submitted fields")
    public void createPost_returns201_withEchoedBody() {
        String payload = """
                {
                  "title":  "smoke-test title",
                  "body":   "smoke-test body",
                  "userId": 42
                }
                """;

        given()
                .spec(spec)
                .body(payload)
                .when()
                .post("/posts")
                .then()
                .statusCode(201)
                .body("title",  equalTo("smoke-test title"))
                .body("body",   equalTo("smoke-test body"))
                .body("userId", equalTo(42))
                .body("id",     notNullValue());
    }

    // ── GET /users/1 ──────────────────────────────────────────────────────────

    @Test(groups = "smoke",
          description = "GET /users/1 → 200 with name, email and address fields present")
    public void getUser_returns200_withContactFields() {
        given()
                .spec(spec)
                .when()
                .get("/users/1")
                .then()
                .statusCode(200)
                .body("id",      equalTo(1))
                .body("name",    not(emptyOrNullString()))
                .body("email",   not(emptyOrNullString()))
                .body("address", notNullValue());
    }

    // ── GET /posts/9999 → 404 ────────────────────────────────────────────────

    @Test(groups = "smoke",
          description = "GET /posts/9999 → 404 for non-existent resource")
    public void getNonExistentPost_returns404() {
        given()
                .spec(spec)
                .when()
                .get("/posts/9999")
                .then()
                .statusCode(404);
    }
}
