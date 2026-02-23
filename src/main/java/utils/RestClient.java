package utils;

import io.qameta.allure.restassured.AllureRestAssured;
import io.restassured.RestAssured;
import io.restassured.builder.RequestSpecBuilder;
import io.restassured.config.LogConfig;
import io.restassured.config.RestAssuredConfig;
import io.restassured.filter.log.LogDetail;
import io.restassured.filter.log.RequestLoggingFilter;
import io.restassured.filter.log.ResponseLoggingFilter;
import io.restassured.http.ContentType;
import io.restassured.response.Response;
import io.restassured.specification.RequestSpecification;
import tools.jackson.core.type.TypeReference;
import tools.jackson.databind.ObjectMapper;

import java.util.Map;

import static io.restassured.RestAssured.given;

public class RestClient {
    private final RequestSpecification spec;
    private final ObjectMapper mapper = new ObjectMapper();

    private static final RestAssuredConfig DEFAULT_REST_ASSURED_CONFIG = RestAssured.config()
            .logConfig(LogConfig.logConfig()
                    .enableLoggingOfRequestAndResponseIfValidationFails(LogDetail.ALL));

    public RestClient(String baseUrl) {
        this.spec = new RequestSpecBuilder()
                .setBaseUri(baseUrl)
                .setConfig(DEFAULT_REST_ASSURED_CONFIG)
                .setContentType(ContentType.JSON)
                .setAccept(ContentType.JSON)
                .addFilter(new AllureRestAssured())
                .addFilter(new RequestLoggingFilter())
                .addFilter(new ResponseLoggingFilter())
                .build();
    }

    /**
     * Is overloaded to allow passing player details as a generic object, which will be converted to a map of query parameters.
     * Useful for testing edge cases with missing or extra parameters, or parameters of wrong types.
     * @param editor - the role of the user creating the player
     */
    public Response createPlayer(String editor, Object playerDetails) {
        Map<String, Object> params = mapper.convertValue(playerDetails, new TypeReference<>() {});

        return given()
                .spec(spec)
                .pathParam("editor", editor)
                .queryParams(params)
                .when()
                .get("/player/create/{editor}");
    }

    /**
     * GET: Uses POST with playerId in the body. Accepts arbitrary types for playerId (for negative tests).
     * @param playerId id of the player to get
     */
    public Response getPlayer(Object playerId) {
        var body = Map.of("playerId", playerId);
        return given()
                .spec(spec)
                .body(body)
                .when()
                .post("/player/get");
    }

    public Response getAllPlayers() {
        return given()
                .spec(spec)
                .when()
                .get("/player/get/all");
    }

    /**
     * UPDATE: Uses PATCH with path parameters and body.
     * @param editor login of the performing user
     * @param id ID of the player to be updated
     * @param body Partial player data for update
     */
    public Response updatePlayer(String editor, long id, Object body) {
        return given()
                .spec(spec)
                .pathParam("editor", editor)
                .pathParam("id", id)
                .body(body)
                .when()
                .patch("/player/update/{editor}/{id}");
    }

    /**
     * UPDATE: Uses PATCH with raw id in path (supports null or non-numeric ids for negative tests).
     * @param editor login of the performing user
     * @param id ID of the player to be updated (can be null or non-numeric)
     * @param body Partial player data for update
     */
    public Response updatePlayerWithRawId(String editor, Object id, Object body) {
        String idSegment = id == null ? "null" : String.valueOf(id);
        return given()
                .spec(spec)
                .pathParam("editor", editor)
                .pathParam("id", idSegment)
                .body(body)
                .when()
                .patch("/player/update/{editor}/{id}");
    }

    /**
     * DELETE: Uses DELETE with editor in path and playerId in body. Accepts arbitrary object for playerId.
     * @param editor login of the performing user
     * @param playerId id of the player to delete (can be non-Long for negative tests)
     */
    public Response deletePlayer(String editor, Object playerId) {
        var body = Map.of("playerId", playerId);

        return given()
                .spec(spec)
                .pathParam("editor", editor)
                .body(body)
                .when()
                .delete("/player/delete/{editor}");
    }

    /**
     * A helper method to convert response to a DTO and assert that the body is not empty.
     */
    public static <T> T as(Response response, Class<T> cls) {
        String body = response.asString();

        if (body == null || body.isBlank()) {
            throw new AssertionError(
                    String.format("Expected response body for %s, but got an empty string! Status code: %d",
                            cls.getSimpleName(), response.getStatusCode())
            );
        }

        return response.as(cls);
    }

}
