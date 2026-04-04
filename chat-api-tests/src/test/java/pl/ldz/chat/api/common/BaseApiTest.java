package pl.ldz.chat.api.common;

import io.restassured.RestAssured;
import io.restassured.builder.RequestSpecBuilder;
import io.restassured.filter.log.RequestLoggingFilter;
import io.restassured.filter.log.ResponseLoggingFilter;
import io.restassured.http.ContentType;
import io.restassured.specification.RequestSpecification;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import org.testng.annotations.BeforeSuite;

/**
 * Base class for all API tests.
 *
 * <p>Configures REST Assured once before any test runs. The target base URL
 * defaults to {@code http://localhost:8080} and can be overridden with the
 * {@code api.base-url} system property:
 *
 * <pre>mvn test -Dapi.base-url=http://staging.example.com</pre>
 */
public abstract class BaseApiTest {

    /** Shared request spec (no auth). Used as a starting point for all requests. */
    protected static RequestSpecification requestSpec;

    /** Shared Jackson mapper configured for Java Time types. */
    protected static ObjectMapper objectMapper;

    @BeforeSuite(alwaysRun = true)
    public void configureRestAssured() {
        String baseUrl = System.getProperty("api.base-url", "http://localhost:8080");

        requestSpec = new RequestSpecBuilder()
                .setBaseUri(baseUrl)
                .setContentType(ContentType.JSON)
                .setAccept(ContentType.JSON)
                .addFilter(new RequestLoggingFilter())   // prints every request to stdout
                .addFilter(new ResponseLoggingFilter())  // prints every response to stdout
                .build();

        objectMapper = new ObjectMapper()
                .registerModule(new JavaTimeModule())
                .disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS)
                .configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);

        RestAssured.enableLoggingOfRequestAndResponseIfValidationFails();
    }

    /**
     * Returns a request spec pre-populated with a Bearer token.
     *
     * @param token JWT obtained from the login endpoint
     */
    protected RequestSpecification withToken(String token) {
        return new RequestSpecBuilder()
                .addRequestSpecification(requestSpec)
                .addHeader("Authorization", "Bearer " + token)
                .build();
    }
}
