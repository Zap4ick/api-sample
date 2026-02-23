package api;

import dto.PlayerDetailsDto;
import entities.Role;
import io.restassured.path.json.exception.JsonPathException;
import io.restassured.response.Response;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;
import org.testng.asserts.SoftAssert;
import utils.TestConfig;
import utils.TestDataGenerator;

import java.util.Map;

import static org.testng.Assert.assertEquals;

public class CreatePlayerTest extends BaseTest {

    private static final Logger logger = LoggerFactory.getLogger(CreatePlayerTest.class);

    // region Positive Tests

    @DataProvider(name = "creatorAndRoles")
    public Object[][] creatorAndRolesDataProvider() {
        return new Object[][]{
                {Role.SUPERVISOR, Role.ADMIN},
                {Role.ADMIN, Role.USER}
        };
    }

    @Test(description = "Positive: Supervisor/Admin can create admin/user players", dataProvider = "creatorAndRoles")
    public void privilegedUserCreatesPlayerWithRoleTest(Role creatorRole, Role roleToCreate) {
        log(logger, String.format("Step: Create a player with role %s as %s", roleToCreate, creatorRole));
        var creatorLogin = creatorRole == Role.SUPERVISOR ? TestConfig.getSupervisorLogin() : TestConfig.getAdminLogin();
        var playerDetails = TestDataGenerator.getRandomPlayerDetails(roleToCreate);
        var createResponse = createPlayerAndRegister(creatorLogin, playerDetails);

        log(logger, "Step: Assert player created with correct fields");
        var softAssert = new SoftAssert();
        softAssert.assertNotNull(createResponse.id(), "ID should not be null");
        assertCreatedPlayerFieldsMatch(softAssert, createResponse, playerDetails);
        softAssert.assertAll();
    }

    @DataProvider(name = "boundaryAges")
    public Object[][] boundaryAgesDataProvider() {
        return new Object[][]{
                {TestDataGenerator.MIN_AGE},
                {TestDataGenerator.MAX_AGE}
        };
    }

    @Test(description = "Positive: Supervisor creates player with boundary ages", dataProvider = "boundaryAges")
    public void privilegedUserCreatesPlayerWithBoundaryAgeTest(int age) {
        log(logger, String.format("Step: Create player with boundary age %d", age));
        var playerDetails = TestDataGenerator.getRandomPlayerDetails(Role.USER);
        var playerDetailsWithBoundaryAge = new PlayerDetailsDto(age, playerDetails.gender(), playerDetails.login(), playerDetails.password(), playerDetails.role(), playerDetails.screenName());

        var createResponse = createPlayerAndRegister(TestConfig.getSupervisorLogin(), playerDetailsWithBoundaryAge);

        log(logger, "Step: Assert player created with correct fields");
        var softAssert = new SoftAssert();
        softAssert.assertNotNull(createResponse.id(), "ID should not be null");
        assertCreatedPlayerFieldsMatch(softAssert, createResponse, playerDetailsWithBoundaryAge);
        softAssert.assertAll();
    }

    @DataProvider(name = "boundaryPasswordLengths")
    public Object[][] boundaryPasswordLengthsDataProvider() {
        return new Object[][]{
                {TestDataGenerator.MIN_PASSWORD_LENGTH},
                {TestDataGenerator.MAX_PASSWORD_LENGTH}
        };
    }

    @Test(description = "Positive: Supervisor creates player with boundary password lengths", dataProvider = "boundaryPasswordLengths")
    public void privilegedUserCreatesPlayerWithBoundaryPasswordTest(int passwordLength) {
        log(logger, String.format("Step: Create player with boundary password length %d", passwordLength));
        var playerDetails = TestDataGenerator.getRandomPlayerDetails(Role.USER);
        var password = TestDataGenerator.getRandomPassword(passwordLength);
        var request = new PlayerDetailsDto(playerDetails.age(), playerDetails.gender(), playerDetails.login(), password, playerDetails.role(), playerDetails.screenName());
        var createResponse = createPlayerAndRegister(TestConfig.getSupervisorLogin(), request);

        log(logger, "Step: Assert player created with correct fields");
        var softAssert = new SoftAssert();
        softAssert.assertNotNull(createResponse.id(), "ID should not be null");
        assertCreatedPlayerFieldsMatch(softAssert, createResponse, request);
        softAssert.assertAll();
    }

    // endregion

    // region Negative Tests

    @DataProvider(name = "invalidAgeProvider")
    public Object[][] invalidAgesDataProvider() {
        return new Object[][]{
                {TestDataGenerator.MIN_AGE - 1, String.format("Boundary value: exactly %d (should be > %d)", TestDataGenerator.MIN_AGE - 1, TestDataGenerator.MIN_AGE - 1)},
                {TestDataGenerator.MAX_AGE + 1, String.format("Boundary value: exactly %d (should be < %d)", TestDataGenerator.MAX_AGE + 1, TestDataGenerator.MAX_AGE + 1)}
        };
    }

    @Test(dataProvider = "invalidAgeProvider", description = "Negative: Age boundary constraints")
    public void createPlayerWithInvalidAgeTest(int age, String description) {
        log(logger, String.format("Step: Attempt to create player with invalid age %d", age));
        var playerDetails = TestDataGenerator.getRandomPlayerDetails(Role.USER);
        var invalidPlayer = new PlayerDetailsDto(age, playerDetails.gender(), playerDetails.login(), playerDetails.password(), playerDetails.role(), playerDetails.screenName());
        var response = restClient.createPlayer(TestConfig.getSupervisorLogin(), invalidPlayer);
        tryAddToDeletingQueue(response);

        log(logger, String.format("Step: Assert creation rejected for %s", description));
        assertEquals(response.getStatusCode(), 400, String.format("Failed for: %s", description));
    }

    @Test(description = "Negative: Regular user cannot create other players")
    public void userCannotCreateOtherPlayersTest() {
        log(logger, "Step: Create a user");
        var playerDetails = TestDataGenerator.getRandomPlayerDetails(Role.USER);
        createPlayerAndRegister(TestConfig.getSupervisorLogin(), playerDetails);

        log(logger, "Step: Attempt to create another player as regular user");
        var newPlayerRequest = TestDataGenerator.getRandomPlayerDetails(Role.USER);
        var response = restClient.createPlayer(playerDetails.login(), newPlayerRequest);
        tryAddToDeletingQueue(response);

        log(logger, "Step: Assert creation rejected with 403 Forbidden");
        assertEquals(response.getStatusCode(), 403, "Regular user should not have permissions to create players");
    }

    @DataProvider(name = "invalidPasswordProvider")
    public Object[][] invalidPasswordsDataProvider() {
        return new Object[][]{
                {"onlyletters", "Password without digits"},
                {TestDataGenerator.getRandomPassword(TestDataGenerator.MIN_PASSWORD_LENGTH - 1), String.format("Password too short (%d)", TestDataGenerator.MIN_PASSWORD_LENGTH - 1)},
                {TestDataGenerator.getRandomPassword(TestDataGenerator.MAX_PASSWORD_LENGTH + 1), String.format("Password too long (%d)", TestDataGenerator.MAX_PASSWORD_LENGTH + 1)}
        };
    }

    @Test(dataProvider = "invalidPasswordProvider", description = "Negative: Password validation (digits and length)")
    public void invalidPasswordsTest(String password, String reason) {
        log(logger, String.format("Step: Attempt to create player with invalid password: %s", reason));
        var playerDetails = TestDataGenerator.getRandomPlayerDetails(Role.USER);
        var invalidPlayer = new PlayerDetailsDto(playerDetails.age(), playerDetails.gender(), playerDetails.login(), password, playerDetails.role(), playerDetails.screenName());
        var response = restClient.createPlayer(TestConfig.getSupervisorLogin(), invalidPlayer);
        tryAddToDeletingQueue(response);

        log(logger, "Step: Assert creation rejected");
        assertEquals(response.getStatusCode(), 400, String.format("Invalid password should be rejected: %s", reason));
    }

    @Test(description = "Negative: Gender must be 'male' or 'female'")
    public void createPlayerWithInvalidGenderTest() {
        log(logger, "Step: Attempt to create player with invalid gender");
        var playerDetails = TestDataGenerator.getRandomPlayerDetails(Role.USER);
        var params = Map.of(
                "age", playerDetails.age(),
                "login", playerDetails.login(),
                "password", playerDetails.password(),
                "role", playerDetails.role().getValue(),
                "screenName", playerDetails.screenName(),
                "gender", "nonbinary"
        );

        var response = restClient.createPlayer(TestConfig.getSupervisorLogin(), params);
        tryAddToDeletingQueue(response);

        log(logger, "Step: Assert creation rejected");
        assertEquals(response.getStatusCode(), 400, "Invalid gender should be rejected");
    }

    @Test(description = "Negative: Cannot create a player with duplicate login")
    public void cannotCreatePlayerWithDuplicateLoginTest() {
        log(logger, "Step: Create first player");
        var firstPlayerDetails = TestDataGenerator.getRandomPlayerDetails(Role.USER);
        var playerCreateResponse = createPlayerAndRegister(TestConfig.getSupervisorLogin(), firstPlayerDetails);

        log(logger, "Step: Attempt to create second player with duplicate login");
        var secondPlayerDetails = TestDataGenerator.getRandomPlayerDetails(Role.USER);
        var secondPlayerDetailsWithSameLogin = new PlayerDetailsDto(secondPlayerDetails.age(), firstPlayerDetails.gender(), firstPlayerDetails.login(), secondPlayerDetails.password(), secondPlayerDetails.role(), secondPlayerDetails.screenName());
        var response = restClient.createPlayer(TestConfig.getSupervisorLogin(), secondPlayerDetailsWithSameLogin);
        tryAddToDeletingQueue(response);

        log(logger, "Step: Assert creation rejected");
        assertEquals(response.getStatusCode(), 400, "Second create with duplicate login should be rejected");

        log(logger, "Step: Assert first player data unchanged");
        var actualPlayer = getPlayer(playerCreateResponse.id());
        var softAssert = new SoftAssert();
        softAssert.assertEquals(actualPlayer.id(), playerCreateResponse.id(), "Player ID should not change");
        assertRetrievedPlayerFieldsMatch(softAssert, actualPlayer, firstPlayerDetails);
        softAssert.assertAll();
    }

    @Test(description = "Negative: Cannot create a player with duplicate screenName")
    public void cannotCreatePlayerWithDuplicateScreenNameTest() {
        log(logger, "Step: Create first player");
        var playerDetails = TestDataGenerator.getRandomPlayerDetails(Role.USER);
        var playerCreateResponse = createPlayerAndRegister(TestConfig.getSupervisorLogin(), playerDetails);

        log(logger, "Step: Attempt to create second player with duplicate screenName");
        var secondPlayerDetails = TestDataGenerator.getRandomPlayerDetails(Role.USER);
        var secondPlayerDetailsWithSameScreenName = new PlayerDetailsDto(secondPlayerDetails.age(), secondPlayerDetails.gender(), secondPlayerDetails.login(), secondPlayerDetails.password(), secondPlayerDetails.role(), playerDetails.screenName());

        var response = restClient.createPlayer(TestConfig.getSupervisorLogin(), secondPlayerDetailsWithSameScreenName);
        tryAddToDeletingQueue(response);

        log(logger, "Step: Assert creation rejected");
        assertEquals(response.getStatusCode(), 400, "Second create with duplicate screenName should be rejected");

        log(logger, "Step: Assert first player data unchanged");
        var actualPlayer = getPlayer(playerCreateResponse.id());
        var softAssert = new SoftAssert();
        softAssert.assertEquals(actualPlayer.id(), playerCreateResponse.id(), "Player ID should not change");
        assertRetrievedPlayerFieldsMatch(softAssert, actualPlayer, playerDetails);
        softAssert.assertAll();
    }

    // endregion

    // region methods

    private void tryAddToDeletingQueue(Response response) {
        try {
            long createdId = response.jsonPath().getLong("id");
            playersToDelete.get().add(createdId);
        } catch (IllegalArgumentException | JsonPathException e) {
            logger.debug("Tried to extract ID from failed create response");
        }
    }

    // endregion
}
