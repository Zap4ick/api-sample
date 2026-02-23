package api;

import dto.*;
import io.qameta.allure.Allure;
import io.restassured.RestAssured;
import io.restassured.config.LogConfig;
import io.restassured.filter.log.LogDetail;
import io.restassured.parsing.Parser;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.testng.annotations.AfterMethod;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.BeforeSuite;
import org.testng.asserts.SoftAssert;
import utils.RestClient;
import utils.TestConfig;

import java.util.LinkedList;
import java.util.Queue;

import static utils.RestClient.as;

/**
 * Base test class to handle common configuration and cleanup.
 */
public abstract class BaseTest {

    protected RestClient restClient;

    protected final ThreadLocal<Queue<Long>> playersToDelete =
            ThreadLocal.withInitial(LinkedList::new);

    private final Logger log = LoggerFactory.getLogger(BaseTest.class);

    @BeforeSuite(alwaysRun = true)
    public void beforeSuite() {
        RestAssured.config = RestAssured.config().logConfig(
                LogConfig.logConfig().enableLoggingOfRequestAndResponseIfValidationFails(LogDetail.ALL)
        );
        RestAssured.defaultParser = Parser.JSON;

        log(log, "\uD83D\uDE80 Running tests! Base url is %s".formatted(TestConfig.getBaseUrl()));
    }

    @BeforeClass(alwaysRun = true)
    public void beforeClass() {
        restClient = new RestClient(TestConfig.getBaseUrl());
    }

    @AfterMethod(alwaysRun = true)
    public void tearDown() {
        while (!playersToDelete.get().isEmpty()) {
            Long playerId = playersToDelete.get().poll();
            log(log, "Removing player with id %d".formatted(playerId));
            restClient.deletePlayer(TestConfig.getSupervisorLogin(), playerId);
        }
    }

    // region Helper Methods

    /**
     * A helper method for positive scenarios.
     * It asserts success and handles automatic cleanup.
     */
    protected PlayerCreateResponseDto createPlayerAndRegister(String editor, PlayerDetailsDto request) {
        var response = restClient.createPlayer(editor, request);
        response.then().statusCode(200);
        long id = response.jsonPath().getLong("id");
        playersToDelete.get().add(id);
        return response.as(PlayerCreateResponseDto.class);
    }

    /**
     * A helper method for positive retrieve player scenarios.
     */
    protected PlayerGetByPlayerIdResponseDto getPlayer(Object playerId) {
        var response = restClient.getPlayer(playerId);
        response.then().statusCode(200);
        return as(response, PlayerGetByPlayerIdResponseDto.class);
    }

    /**
     * A helper method for positive delete scenarios.
     */
    protected void deletePlayer(String editor, Long playerId) {
        var response = restClient.deletePlayer(editor, playerId);
        response.then().statusCode(204);
    }

    /**
     * A helper method for positive update scenarios.
     */
    protected PlayerUpdateResponseDto updatePlayer(String editor, Long playerId, PlayerUpdateRequestDto request) {
        var response = restClient.updatePlayer(editor, playerId, request);
        response.then().statusCode(200);
        return as(response, PlayerUpdateResponseDto.class);
    }

    /**
     * A helper method for positive get all players scenarios.
     */
    protected PlayerGetAllResponseDto getAllPlayers() {
        var response = restClient.getAllPlayers();
        response.then().statusCode(200);
        return as(response, PlayerGetAllResponseDto.class);
    }

    /**
     * Assert all created player fields match the expected values.
     */
    protected void assertCreatedPlayerFieldsMatch(SoftAssert softAssert, PlayerCreateResponseDto actual, PlayerDetailsDto expected) {
        softAssert.assertEquals(actual.age(), expected.age(), "Age should match");
        softAssert.assertEquals(actual.login(), expected.login(), "Login should match");
        softAssert.assertEquals(actual.password(), expected.password(), "Password should match");
        softAssert.assertEquals(actual.getRoleAsEnum(), expected.role(), "Role should match");
        softAssert.assertEquals(actual.screenName(), expected.screenName(), "ScreenName should match");
        softAssert.assertEquals(actual.getGenderAsEnum(), expected.gender(), "Gender should match");
    }

    /**
     * Assert all retrieved player fields (except ID) match the expected values.
     */
    protected void assertRetrievedPlayerFieldsMatch(SoftAssert softAssert, PlayerGetByPlayerIdResponseDto actual, PlayerDetailsDto expected) {
        softAssert.assertEquals(actual.age(), expected.age(), "Player age should match");
        softAssert.assertEquals(actual.login(), expected.login(), "Player login should match");
        softAssert.assertEquals(actual.password(), expected.password(), "Player password should match");
        softAssert.assertEquals(actual.getRoleAsEnum(), expected.role(), "Player role should match");
        softAssert.assertEquals(actual.screenName(), expected.screenName(), "Player screenName should match");
        softAssert.assertEquals(actual.getGenderAsEnum(), expected.gender(), "Player gender should match");
    }

    protected void log(Logger logger, String message) {
        logger.info(message);
        Allure.addAttachment(message, "text/plain", "------");
    }

    // endregion
}
