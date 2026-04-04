package pl.ldz.chat.common;

import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import io.restassured.RestAssured;
import io.restassured.builder.RequestSpecBuilder;
import io.restassured.filter.log.RequestLoggingFilter;
import io.restassured.filter.log.ResponseLoggingFilter;
import io.restassured.http.ContentType;
import io.restassured.specification.RequestSpecification;
import org.testng.annotations.BeforeSuite;

/**
 * Base class for all API tests.
 *
 * <p>Configures REST Assured and a shared Jackson {@link ObjectMapper} once
 * before the TestNG suite starts. The target base URL defaults to
 * {@code http://localhost:8080} and can be overridden with:
 *
 * <pre>mvn test -Dapi.base-url=http://staging.example.com:9090</pre>
 */
public abstract class BaseApiTest {

    /** Unauthenticated request spec – use as the baseline for all requests. */
    protected static RequestSpecification requestSpec;

    /** Shared Jackson mapper configured for Java Time types. */
    protected static ObjectMapper objectMapper;

    @BeforeSuite(alwaysRun = true)
    public void configureSuite() {
        String baseUrl = System.getProperty("api.base-url", "http://localhost:8080");

        requestSpec = new RequestSpecBuilder()
                .setBaseUri(baseUrl)
                .setContentType(ContentType.JSON)
                .setAccept(ContentType.JSON)
                .addFilter(new RequestLoggingFilter())
                .addFilter(new ResponseLoggingFilter())
                .build();

        objectMapper = new ObjectMapper()
                .registerModule(new JavaTimeModule())
                .disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS)
                .configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);

        RestAssured.enableLoggingOfRequestAndResponseIfValidationFails();
    }

    /**
     * Decorates the shared spec with a Bearer token.
     *
     * @param token JWT obtained from {@code POST /api/v1/auth/login}
     * @return a new {@link RequestSpecification} with the Authorization header set
     */
    protected RequestSpecification withToken(String token) {
        return new RequestSpecBuilder()
                .addRequestSpecification(requestSpec)
                .addHeader("Authorization", "Bearer " + token)
                .build();
    }
}
