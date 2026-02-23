package api;

import dto.PlayerDetailsDto;
import dto.PlayerGetByPlayerIdResponseDto;
import dto.PlayerUpdateRequestDto;
import dto.PlayerUpdateResponseDto;
import entities.Role;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;
import org.testng.asserts.SoftAssert;
import utils.TestConfig;
import utils.TestDataGenerator;

import java.util.Map;

import static org.testng.Assert.assertEquals;

public class UpdatePlayerTest extends BaseTest {

    private static final Logger logger = LoggerFactory.getLogger(UpdatePlayerTest.class);

    // region Positive Tests

    @DataProvider(name = "privilegedUserUpdatesOneField")
    public Object[][] privilegedUserUpdatesOneFieldDataProvider() {
        return new Object[][]{
                {Role.SUPERVISOR},
                {Role.ADMIN}
        };
    }

    @Test(description = "Positive: Supervisor/Admin updates one field of a player", dataProvider = "privilegedUserUpdatesOneField")
    public void privilegedUserUpdatesOneFieldTest(Role editorRole) {
        log(logger, "Step: Create a player");
        var editorLogin = editorRole == Role.SUPERVISOR ? TestConfig.getSupervisorLogin() : TestConfig.getAdminLogin();
        var playerDetails = TestDataGenerator.getRandomPlayerDetails(Role.USER);
        var playerCreateResponse = createPlayerAndRegister(TestConfig.getSupervisorLogin(), playerDetails);

        log(logger, String.format("Step: Update one field as %s", editorRole));
        var randomScreenName = TestDataGenerator.getRandomPlayerDetails().screenName();
        var updateRequest = new PlayerUpdateRequestDto(null, null, null, null, null, randomScreenName);
        var updateResponseDto = updatePlayer(editorLogin, playerCreateResponse.id(), updateRequest);

        log(logger, "Step: Assert update response contains updated fields");
        var softAssertResponse = new SoftAssert();
        softAssertResponse.assertEquals(updateResponseDto.screenName(), updateRequest.screenName(), "Response ScreenName should be updated");
        assertUpdateResponseUnchangedFields(softAssertResponse, updateResponseDto, playerDetails);
        softAssertResponse.assertAll();

        log(logger, "Step: Assert retrieved player matches update response");
        var updatedPlayer = getPlayer(playerCreateResponse.id());
        var softAssert = new SoftAssert();
        softAssert.assertEquals(updatedPlayer.id(), playerCreateResponse.id(), "Response id should remain unchanged");
        assertRetrievedPlayerMatchesUpdateResponse(softAssert, updatedPlayer, updateResponseDto);
        softAssert.assertAll();
    }

    @DataProvider(name = "privilegedUserUpdatesWithBoundaryValues")
    public Object[][] privilegedUserUpdatesWithBoundaryValuesDataProvider() {
        return new Object[][]{
                {Role.SUPERVISOR, TestDataGenerator.MIN_AGE, TestDataGenerator.MIN_PASSWORD_LENGTH},
                {Role.ADMIN, TestDataGenerator.MAX_AGE, TestDataGenerator.MAX_PASSWORD_LENGTH}
        };
    }

    @Test(description = "Positive: Supervisor/Admin updates multiple fields with boundary values", dataProvider = "privilegedUserUpdatesWithBoundaryValues")
    public void privilegedUserUpdatesMultipleFieldsWithBoundaryValuesTest(Role editorRole, int boundaryAge, int passwordLength) {
        log(logger, "Step: Create a player");
        var editorLogin = editorRole == Role.SUPERVISOR ? TestConfig.getSupervisorLogin() : TestConfig.getAdminLogin();
        var playerDetails = TestDataGenerator.getRandomPlayerDetails(Role.USER);
        var playerCreateResponse = createPlayerAndRegister(TestConfig.getSupervisorLogin(), playerDetails);

        log(logger, String.format("Step: Update multiple fields with boundary values as %s (age: %d, password length: %d)", editorRole, boundaryAge, passwordLength));
        var randomUpdate = TestDataGenerator.getRandomPlayerDetails(Role.ADMIN);
        var updateRequest = new PlayerUpdateRequestDto(boundaryAge, randomUpdate.gender(), null, TestDataGenerator.getRandomPassword(passwordLength), randomUpdate.role(), randomUpdate.screenName());
        var updateResponseDto = updatePlayer(editorLogin, playerCreateResponse.id(), updateRequest);

        log(logger, "Step: Assert update response contains updated fields with boundary values");
        var softAssertResponse = new SoftAssert();
        softAssertResponse.assertEquals(updateResponseDto.age().intValue(), boundaryAge, "Response Age should match boundary value");
        softAssertResponse.assertEquals(updateResponseDto.screenName(), updateRequest.screenName(), "Response ScreenName should be updated");
        softAssertResponse.assertEquals(updateResponseDto.getRoleAsEnum(), updateRequest.role(), "Response Role should be updated");
        softAssertResponse.assertAll();

        log(logger, "Step: Assert retrieved player matches update response");
        var updatedPlayer = getPlayer(playerCreateResponse.id());
        var softAssert = new SoftAssert();
        softAssert.assertEquals(updatedPlayer.age().intValue(), boundaryAge, "Age should match boundary value");
        softAssert.assertEquals(updatedPlayer.screenName(), updateRequest.screenName(), "ScreenName should be updated");
        softAssert.assertEquals(updatedPlayer.getRoleAsEnum(), updateRequest.role(), "Role should be updated");
        softAssert.assertAll();
    }

    @Test(description = "Positive: User updates one field of himself")
    public void userUpdatesOwnOneFieldTest() {
        log(logger, "Step: Create a user");
        var userDetails = TestDataGenerator.getRandomPlayerDetails(Role.USER);
        var userCreated = createPlayerAndRegister(TestConfig.getSupervisorLogin(), userDetails);

        log(logger, "Step: Update one field of the user");
        var randomScreenName = TestDataGenerator.getRandomPlayerDetails().screenName();
        var updateRequest = new PlayerUpdateRequestDto(null, null, null, null, null, randomScreenName);
        var updateResponse = updatePlayer(userCreated.login(), userCreated.id(), updateRequest);

        log(logger, "Step: Assert update response contains updated fields");
        var softAssertResponse = new SoftAssert();
        softAssertResponse.assertEquals(updateResponse.screenName(), updateRequest.screenName(), "Response ScreenName should be updated");
        assertUpdateResponseUnchangedFields(softAssertResponse, updateResponse, userDetails);
        softAssertResponse.assertAll();

        log(logger, "Step: Assert retrieved user matches update response");
        var updatedUser = getPlayer(userCreated.id());
        var softAssert = new SoftAssert();
        softAssert.assertEquals(updatedUser.id(), userCreated.id(), "Response id should remain unchanged");
        assertRetrievedPlayerMatchesUpdateResponse(softAssert, updatedUser, updateResponse);
        softAssert.assertAll();
    }

    @Test(description = "Positive: User updates multiple fields of himself")
    public void userUpdatesOwnMultipleFieldsTest() {
        log(logger, "Step: Create a user");
        var userDetails = TestDataGenerator.getRandomPlayerDetails(Role.USER);
        var userCreated = createPlayerAndRegister(TestConfig.getSupervisorLogin(), userDetails);

        log(logger, "Step: Update multiple fields of the user");
        var randomPlayerForUpdate = TestDataGenerator.getRandomPlayerDetails(Role.USER);
        var updateRequest = new PlayerUpdateRequestDto(randomPlayerForUpdate.age(), randomPlayerForUpdate.gender(), null, TestDataGenerator.getRandomPassword(TestDataGenerator.DEFAULT_PASSWORD_LENGTH), null, randomPlayerForUpdate.screenName());
        var updateResponseDto = updatePlayer(userCreated.login(), userCreated.id(), updateRequest);

        log(logger, "Step: Assert update response contains updated fields");
        var softAssertResponse = new SoftAssert();
        softAssertResponse.assertEquals(updateResponseDto.screenName(), updateRequest.screenName(), "Response ScreenName should be updated");
        softAssertResponse.assertEquals(updateResponseDto.age().intValue(), updateRequest.age().intValue(), "Response Age should be updated");
        softAssertResponse.assertEquals(updateResponseDto.getGenderAsEnum(), updateRequest.gender(), "Response Gender should be updated");
        softAssertResponse.assertEquals(updateResponseDto.login(), userDetails.login(), "Response Login should remain unchanged");
        softAssertResponse.assertEquals(updateResponseDto.getRoleAsEnum(), userDetails.role(), "Response Role should remain unchanged");
        softAssertResponse.assertAll();

        log(logger, "Step: Assert retrieved user matches update response");
        var updatedUser = getPlayer(userCreated.id());
        var softAssert = new SoftAssert();
        softAssert.assertEquals(updatedUser.screenName(), updateRequest.screenName(), "ScreenName should be updated");
        softAssert.assertEquals(updatedUser.age().intValue(), updateRequest.age().intValue(), "Age should be updated");
        softAssert.assertEquals(updatedUser.getGenderAsEnum(), updateRequest.gender(), "Gender should be updated");
        softAssert.assertEquals(updatedUser.password(), updateRequest.password(), "Password should be updated");
        softAssert.assertEquals(updatedUser.login(), userDetails.login(), "Login should remain unchanged");
        softAssert.assertEquals(updatedUser.getRoleAsEnum(), userDetails.role(), "Role should remain unchanged");
        softAssert.assertAll();
    }

    @Test(description = "Positive: Supervisor updates user with empty body does not change fields")
    public void supervisorEmptyUpdateDoesNotChangeFieldsTest() {
        log(logger, "Step: Create a player");
        var playerDetails = TestDataGenerator.getRandomPlayerDetails(Role.USER);
        var playerCreateResponse = createPlayerAndRegister(TestConfig.getSupervisorLogin(), playerDetails);

        log(logger, "Step: Update with empty body");
        var emptyUpdateRequest = new PlayerUpdateRequestDto(null, null, null, null, null, null);
        var updateResponseDto = updatePlayer(TestConfig.getSupervisorLogin(), playerCreateResponse.id(), emptyUpdateRequest);

        log(logger, "Step: Assert retrieved player matches update response");
        var updatedPlayer = getPlayer(playerCreateResponse.id());
        var softAssert = new SoftAssert();
        assertRetrievedPlayerMatchesUpdateResponse(softAssert, updatedPlayer, updateResponseDto);
        softAssert.assertAll();
    }

    // endregion

    // region Negative Tests

    @Test(description = "Negative: Attempt to update player login")
    public void updateLoginNotAllowedTest() {
        log(logger, "Step: Create a player");
        var playerDetails = TestDataGenerator.getRandomPlayerDetails(Role.USER);
        var playerCreateResponse = createPlayerAndRegister(TestConfig.getSupervisorLogin(), playerDetails);

        log(logger, "Step: Attempt to update login");
        var updateRequest = new PlayerUpdateRequestDto(null, null, "newLogin", null, null, null);
        var response = restClient.updatePlayer(TestConfig.getSupervisorLogin(), playerCreateResponse.id(), updateRequest);

        log(logger, "Step: Assert update rejected");
        assertEquals(response.getStatusCode(), 400, "Updating login should be rejected");
    }

    @Test(description = "Negative: Regular user cannot update another player's data")
    public void userCannotUpdateAnotherPlayerTest() {
        log(logger, "Step: Create first user");
        var firstUserDetails = TestDataGenerator.getRandomPlayerDetails(Role.USER);
        var firstUserCreated = createPlayerAndRegister(TestConfig.getSupervisorLogin(), firstUserDetails);

        log(logger, "Step: Create second user");
        var secondUserDetails = TestDataGenerator.getRandomPlayerDetails(Role.USER);
        var secondUserCreated = createPlayerAndRegister(TestConfig.getSupervisorLogin(), secondUserDetails);

        log(logger, "Step: Attempt to update another player's data");
        var randomScreenName = TestDataGenerator.getRandomPlayerDetails().screenName();
        var updateRequest = new PlayerUpdateRequestDto(null, null, null, null, null, randomScreenName);
        var response = restClient.updatePlayer(firstUserCreated.login(), secondUserCreated.id(), updateRequest);

        log(logger, "Step: Assert update rejected");
        assertEquals(response.getStatusCode(), 403, "Regular user should not update another player's data");
    }

    @Test(description = "Negative: Update non-existing player id")
    public void updateNonExistingPlayerIdTest() {
        log(logger, "Step: Attempt to update non-existing player id");
        var randomScreenName = TestDataGenerator.getRandomPlayerDetails().screenName();
        var updateRequest = new PlayerUpdateRequestDto(null, null, null, null, null, randomScreenName);
        var response = restClient.updatePlayer(TestConfig.getSupervisorLogin(), 99999999L, updateRequest);

        log(logger, "Step: Assert update rejected");
        assertEquals(response.getStatusCode(), 404, "Non-existing player id should be rejected");
    }

    @Test(description = "Negative: Update null player id")
    public void updateNullPlayerIdTest() {
        log(logger, "Step: Attempt to update with null player id");
        var randomScreenName = TestDataGenerator.getRandomPlayerDetails().screenName();
        var updateRequest = new PlayerUpdateRequestDto(null, null, null, null, null, randomScreenName);
        var response = restClient.updatePlayerWithRawId(TestConfig.getSupervisorLogin(), null, updateRequest);

        log(logger, "Step: Assert update rejected");
        assertEquals(response.getStatusCode(), 400, "Null player id should be rejected");
    }

    @Test(description = "Negative: Invalid data type in update (age as string)")
    public void updateWithInvalidAgeTypeTest() {
        log(logger, "Step: Create a player");
        var playerDetails = TestDataGenerator.getRandomPlayerDetails(Role.USER);
        var playerCreateResponse = createPlayerAndRegister(TestConfig.getSupervisorLogin(), playerDetails);

        log(logger, "Step: Attempt to update with invalid age type");
        var updateBody = Map.of("age", "not-a-number");
        var response = restClient.updatePlayer(TestConfig.getSupervisorLogin(), playerCreateResponse.id(), updateBody);

        log(logger, "Step: Assert update rejected");
        assertEquals(response.getStatusCode(), 400, "Invalid age type should be rejected");
    }

    @DataProvider(name = "invalidUpdateBoundaries")
    public Object[][] invalidUpdateBoundariesDataProvider() {
        return new Object[][]{
                {new PlayerUpdateRequestDto(TestDataGenerator.MIN_AGE - 1, null, null, null, null, null), String.format("Age %d (too low)", TestDataGenerator.MIN_AGE - 1)},
                {new PlayerUpdateRequestDto(TestDataGenerator.MAX_AGE + 1, null, null, null, null, null), String.format("Age %d (too high)", TestDataGenerator.MAX_AGE + 1)},
                {new PlayerUpdateRequestDto(null, null, null, TestDataGenerator.getRandomPassword(TestDataGenerator.MIN_PASSWORD_LENGTH - 1), null, null), String.format("Password %d chars (too short)", TestDataGenerator.MIN_PASSWORD_LENGTH - 1)},
                {new PlayerUpdateRequestDto(null, null, null, TestDataGenerator.getRandomPassword(TestDataGenerator.MAX_PASSWORD_LENGTH + 1), null, null), String.format("Password %d chars (too long)", TestDataGenerator.MAX_PASSWORD_LENGTH + 1)},
                {new PlayerUpdateRequestDto(null, null, null, "onlyletters", null, null), "Password without digits"}
        };
    }

    @Test(description = "Negative: Update with boundary violations", dataProvider = "invalidUpdateBoundaries")
    public void updateWithBoundaryViolationsTest(PlayerUpdateRequestDto updateRequest, String testCase) {
        log(logger, "Step: Create a player");
        var playerDetails = TestDataGenerator.getRandomPlayerDetails(Role.USER);
        var playerCreateResponse = createPlayerAndRegister(TestConfig.getSupervisorLogin(), playerDetails);

        log(logger, String.format("Step: Attempt to update with boundary violations: %s", testCase));
        var response = restClient.updatePlayer(TestConfig.getSupervisorLogin(), playerCreateResponse.id(), updateRequest);

        log(logger, "Step: Assert update rejected");
        assertEquals(response.getStatusCode(), 400, String.format("Out-of-bound update should be rejected for: %s", testCase));
    }

    @Test(description = "Negative: Update player with invalid gender")
    public void updateWithInvalidGenderTest() {
        log(logger, "Step: Create a player");
        var playerDetails = TestDataGenerator.getRandomPlayerDetails(Role.USER);
        var playerCreateResponse = createPlayerAndRegister(TestConfig.getSupervisorLogin(), playerDetails);

        log(logger, "Step: Attempt to update with invalid gender");
        var updateBody = Map.of("gender", "nonbinary");
        var response = restClient.updatePlayer(TestConfig.getSupervisorLogin(), playerCreateResponse.id(), updateBody);

        log(logger, "Step: Assert update rejected");
        assertEquals(response.getStatusCode(), 400, "Invalid gender should be rejected");
    }

    @Test(description = "Negative: Update player with non-updatable field (id) in request")
    public void updateWithNonUpdatableFieldTest() {
        log(logger, "Step: Create a player");
        var playerDetails = TestDataGenerator.getRandomPlayerDetails(Role.USER);
        var playerCreateResponse = createPlayerAndRegister(TestConfig.getSupervisorLogin(), playerDetails);

        log(logger, "Step: Attempt to update with non-updatable field (id)");
        var updateBody = Map.of("id", 99999999L);
        var response = restClient.updatePlayer(TestConfig.getSupervisorLogin(), playerCreateResponse.id(), updateBody);

        log(logger, "Step: Assert update does not cause error code");
        assertEquals(response.getStatusCode(), 200, "Update with non-updatable field should be handled gracefully");

        log(logger, "Step: Assert retrieved player data unchanged");
        var updatedPlayer = getPlayer(playerCreateResponse.id());
        var softAssert = new SoftAssert();
        softAssert.assertEquals(updatedPlayer.id(), playerCreateResponse.id(), "Player ID should not change");
        softAssert.assertEquals(updatedPlayer.age(), playerDetails.age(), "Player age should not change");
        softAssert.assertEquals(updatedPlayer.getRoleAsEnum(), playerDetails.role(), "Player role should not change");
        softAssert.assertEquals(updatedPlayer.getGenderAsEnum(), playerDetails.gender(), "Player gender should not change");
        softAssert.assertEquals(updatedPlayer.screenName(), playerDetails.screenName(), "Player screenName should not change");
        softAssert.assertEquals(updatedPlayer.login(), playerDetails.login(), "Player login should not change");
        softAssert.assertEquals(updatedPlayer.password(), playerDetails.password(), "Player password should not change");
        softAssert.assertAll();
    }

    @Test(description = "Negative: Cannot update player with duplicate login from another player")
    public void updateWithDuplicateLoginTest() {
        log(logger, "Step: Create first player");
        var firstPlayerDetails = TestDataGenerator.getRandomPlayerDetails(Role.USER);
        createPlayerAndRegister(TestConfig.getSupervisorLogin(), firstPlayerDetails);

        log(logger, "Step: Create second player");
        var secondPlayerDetails = TestDataGenerator.getRandomPlayerDetails(Role.USER);
        var secondPlayer = createPlayerAndRegister(TestConfig.getSupervisorLogin(), secondPlayerDetails);

        log(logger, "Step: Attempt to update with duplicate login");
        var updateRequest = new PlayerUpdateRequestDto(null, null, firstPlayerDetails.login(), null, null, null);
        var response = restClient.updatePlayer(TestConfig.getSupervisorLogin(), secondPlayer.id(), updateRequest);

        log(logger, "Step: Assert update rejected");
        assertEquals(response.getStatusCode(), 400, "Update with duplicate login should be rejected");

        log(logger, "Step: Assert second player data unchanged");
        var updatedSecondPlayer = getPlayer(secondPlayer.id());
        var softAssert = new SoftAssert();
        softAssert.assertEquals(updatedSecondPlayer.login(), secondPlayerDetails.login(), "Player login should not change");
        softAssert.assertEquals(updatedSecondPlayer.screenName(), secondPlayerDetails.screenName(), "Player screenName should not change");
        softAssert.assertAll();
    }

    @Test(description = "Negative: Cannot update player with duplicate screenName from another player")
    public void updateWithDuplicateScreenNameTest() {
        log(logger, "Step: Create first player");
        var firstPlayerDetails = TestDataGenerator.getRandomPlayerDetails(Role.USER);
        createPlayerAndRegister(TestConfig.getSupervisorLogin(), firstPlayerDetails);

        log(logger, "Step: Create second player");
        var secondPlayerDetails = TestDataGenerator.getRandomPlayerDetails(Role.USER);
        var secondPlayer = createPlayerAndRegister(TestConfig.getSupervisorLogin(), secondPlayerDetails);

        log(logger, "Step: Attempt to update with duplicate screenName");
        var updateRequest = new PlayerUpdateRequestDto(null, null, null, null, null, firstPlayerDetails.screenName());
        var response = restClient.updatePlayer(TestConfig.getSupervisorLogin(), secondPlayer.id(), updateRequest);

        log(logger, "Step: Assert update rejected");
        assertEquals(response.getStatusCode(), 400, "Update with duplicate screenName should be rejected");

        log(logger, "Step: Assert second player data unchanged");
        var updatedSecondPlayer = getPlayer(secondPlayer.id());
        var softAssert = new SoftAssert();
        softAssert.assertEquals(updatedSecondPlayer.login(), secondPlayerDetails.login(), "Player login should not change");
        softAssert.assertEquals(updatedSecondPlayer.screenName(), secondPlayerDetails.screenName(), "Player screenName should not change");
        softAssert.assertAll();
    }

    @DataProvider(name = "unprivilegedRolesForRoleChange")
    public Object[][] unprivilegedRolesForRoleChangeDataProvider() {
        return new Object[][]{
                {Role.USER, Role.ADMIN},
                {Role.ADMIN, Role.SUPERVISOR}
        };
    }

    @Test(description = "Negative: Unprivileged users cannot change their own role", dataProvider = "unprivilegedRolesForRoleChange")
    public void nonPrivilegedUsersCannotChangeOwnRoleTest(Role currentRole, Role targetRole) {
        log(logger, String.format("Step: Create a %s user", currentRole));
        var userDetails = TestDataGenerator.getRandomPlayerDetails(currentRole);
        var userCreated = createPlayerAndRegister(TestConfig.getSupervisorLogin(), userDetails);

        log(logger, String.format("Step: Attempt to elevate %s role to %s", currentRole, targetRole));
        var updateRequest = new PlayerUpdateRequestDto(null, null, null, null, targetRole, null);
        var response = restClient.updatePlayer(userCreated.login(), userCreated.id(), updateRequest);

        log(logger, "Step: Assert update rejected");
        assertEquals(response.getStatusCode(), 403, String.format("Role %s should not be able to change their own role to %s", currentRole, targetRole));

        log(logger, String.format("Step: Assert %s role remains unchanged", currentRole));
        var updatedUser = getPlayer(userCreated.id());
        var softAssert = new SoftAssert();
        softAssert.assertEquals(updatedUser.getRoleAsEnum(), currentRole, String.format("%s role should remain %s after failed elevation attempt", currentRole, currentRole));
        softAssert.assertAll();
    }

    // endregion

    // region Helper Methods

    /**
     * Assert that retrieved player matches update response (all fields including ID).
     */
    private void assertRetrievedPlayerMatchesUpdateResponse(SoftAssert softAssert, PlayerGetByPlayerIdResponseDto actual, PlayerUpdateResponseDto expected) {
        softAssert.assertEquals(actual.id(), expected.id(), "ID should match");
        softAssert.assertEquals(actual.login(), expected.login(), "Login should match");
        softAssert.assertEquals(actual.age(), expected.age(), "Age should match");
        softAssert.assertEquals(actual.getRoleAsEnum(), expected.getRoleAsEnum(), "Role should match");
        softAssert.assertEquals(actual.getGenderAsEnum(), expected.getGenderAsEnum(), "Gender should match");
        softAssert.assertEquals(actual.screenName(), expected.screenName(), "ScreenName should match");
    }

    /**
     * Assert update response unchanged fields match original data (all fields except ID and updated fields = screenName).
     */
    private void assertUpdateResponseUnchangedFields(SoftAssert softAssert, PlayerUpdateResponseDto actual, PlayerDetailsDto original) {
        softAssert.assertEquals(actual.login(), original.login(), "Response Login should remain unchanged");
        softAssert.assertEquals(actual.age(), original.age(), "Response Age should remain unchanged");
        softAssert.assertEquals(actual.getRoleAsEnum(), original.role(), "Response Role should remain unchanged");
        softAssert.assertEquals(actual.getGenderAsEnum(), original.gender(), "Response Gender should remain unchanged");
    }

    // endregion
}
