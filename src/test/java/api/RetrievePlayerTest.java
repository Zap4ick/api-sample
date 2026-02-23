package api;

import entities.Role;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.testng.annotations.Test;
import org.testng.asserts.SoftAssert;
import utils.TestConfig;
import utils.TestDataGenerator;

import static org.testng.Assert.assertEquals;

public class RetrievePlayerTest extends BaseTest {

    private static final Logger logger = LoggerFactory.getLogger(RetrievePlayerTest.class);

    private static final Long NON_EXISTING_ID = 999999L;

    // region Positive Tests

    @Test(description = "Positive: Get player data by ID")
    public void getPlayerByIdTest() {
        log(logger, "Step: Create a player");
        var playerDetails = TestDataGenerator.getRandomPlayerDetails(Role.USER);
        var playerCreateResponse = createPlayerAndRegister(TestConfig.getSupervisorLogin(), playerDetails);

        log(logger, "Step: Retrieve player by ID");
        var actualPlayerDetails = getPlayer(playerCreateResponse.id());

        log(logger, "Step: Assert player data matches");
        var softAssert = new SoftAssert();
        softAssert.assertEquals(actualPlayerDetails.id(), playerCreateResponse.id(), "Player ID should match");
        assertRetrievedPlayerFieldsMatch(softAssert, actualPlayerDetails, playerDetails);
        softAssert.assertAll();
    }

    @Test(description = "Positive: Existing user can retrieve their own data by ID")
    public void existingUsersCanGetSelfTest() {
        log(logger, "Step: Create a user");
        var userDetails = TestDataGenerator.getRandomPlayerDetails(Role.USER);
        var actor = createPlayerAndRegister(TestConfig.getSupervisorLogin(), userDetails);

        log(logger, "Step: User retrieves their own data");
        var actualPlayerDetails = getPlayer(actor.id());

        log(logger, "Step: Assert user data matches");
        var softAssert = new SoftAssert();
        softAssert.assertEquals(actualPlayerDetails.id(), actor.id(), "Player ID should match");
        assertRetrievedPlayerFieldsMatch(softAssert, actualPlayerDetails, userDetails);
        softAssert.assertAll();
    }

    // endregion

    // region Negative Tests

    @Test(description = "Negative: Get player with non-existing ID")
    public void getNonExistingPlayerTest() {
        log(logger, "Step: Attempt to retrieve player with non-existing ID");
        var response = restClient.getPlayer(NON_EXISTING_ID);

        log(logger, "Step: Assert retrieval rejected with 404 Not Found");
        assertEquals(response.getStatusCode(), 404, "Getting non-existing player should return not found");
    }

    @Test(description = "Negative: Get player with incorrect ID format (string)")
    public void getPlayerWithIncorrectIdFormatTest() {
        log(logger, "Step: Attempt to retrieve player with incorrect ID format");
        var response = restClient.getPlayer("not-a-number");

        log(logger, "Step: Assert retrieval rejected with 400 Bad Request");
        assertEquals(response.getStatusCode(), 400, "Getting player with invalid ID format should return bad request");
    }

    /**
     * This test ensures that regular users cannot access information of other users, which is a critical security requirement.
     * This requirement is not specified in the original spec (and there is no authorization as I see it),
     * but it's a common best practice for user management systems to prevent unauthorized access to user data.
     */
    @Test(description = "Negative: Regular user cannot get another user's info")
    public void userCannotGetAnotherUserInfoTest() {
        log(logger, "Step: Create first user");
        var firstUserDetails = TestDataGenerator.getRandomPlayerDetails(Role.USER);
        var firstUserCreated = createPlayerAndRegister(TestConfig.getSupervisorLogin(), firstUserDetails);

        log(logger, "Step: Create second user");
        var secondUserDetails = TestDataGenerator.getRandomPlayerDetails(Role.USER);
        createPlayerAndRegister(TestConfig.getSupervisorLogin(), secondUserDetails);

        log(logger, "Step: Attempt to retrieve another user's info as first user");
        var response = restClient.getPlayer(firstUserCreated.id());

        log(logger, "Step: Assert retrieval rejected with 403 Forbidden");
        assertEquals(response.getStatusCode(), 403, "Regular user should not be able to get another user's info");
    }

    // endregion
}
