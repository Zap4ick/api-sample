package api;

import dto.PlayerItemDto;
import entities.Gender;
import entities.Role;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.testng.annotations.Test;
import org.testng.asserts.SoftAssert;
import utils.TestConfig;
import utils.TestDataGenerator;

import java.net.HttpURLConnection;

import static org.testng.Assert.*;

public class GetAllPlayersTest extends BaseTest {

    private static final Logger logger = LoggerFactory.getLogger(GetAllPlayersTest.class);
    private static final int MAX_PLAYERS_VALIDATION_LIMIT = 100;

    // region Positive Tests

    @Test(description = "Positive: Get all players and verify presence of a newly created one")
    public void getAllPlayersContainsNewlyCreatedPlayerTest() {
        log(logger, "Step: Create a new player");
        var playerDetails = TestDataGenerator.getRandomPlayerDetails(Role.USER);
        var playerCreateResponse = createPlayerAndRegister(TestConfig.getSupervisorLogin(), playerDetails);

        log(logger, "Step: Get all players from the system");
        var allPlayersResponse = getAllPlayers();

        log(logger, "Step: Assert players list is not null and not empty");
        assertNotNull(allPlayersResponse.players(), "Players list should not be null");
        assertFalse(allPlayersResponse.players().isEmpty(), "Players list should not be empty");

        log(logger, "Step: Find newly created player in the list");
        var createdPlayerInList = allPlayersResponse.players().stream()
                .filter(p -> p.id().equals(playerCreateResponse.id()))
                .findFirst();

        log(logger, "Step: Assert newly created player is present");
        assertTrue(createdPlayerInList.isPresent(), "Newly created player should be present in the list");

        log(logger, "Step: Assert all player details match");
        PlayerItemDto playerItem = createdPlayerInList.get();
        assertEquals(playerItem.id(), playerCreateResponse.id(), "Player ID should match");
        assertEquals(playerItem.age(), playerDetails.age(), "Player age should match");
        assertEquals(playerItem.getRoleAsEnum(), playerDetails.role(), "Player role should match");
        assertEquals(playerItem.getGenderAsEnum(), playerDetails.gender(), "Player gender should match");
        assertEquals(playerItem.screenName(), playerDetails.screenName(), "Player screenName should match");
    }

    @Test(description = "Sanity: Default system users are present in the list", groups = {"sanity"})
    public void defaultSystemUsersArePresentTest() {
        log(logger, "Step: Get all players from the system");
        var allPlayersResponse = getAllPlayers();

        log(logger, "Step: Assert players list is not null");
        assertNotNull(allPlayersResponse.players(), "Players list should not be null");

        log(logger, "Step: Assert supervisor and admin are present with correct roles");
        var softAssert = new SoftAssert();
        softAssert.assertTrue(allPlayersResponse.players().stream()
                .anyMatch(p -> p.id().equals(TestConfig.getSupervisorId())),
                "Supervisor should be present in the list");
        softAssert.assertTrue(allPlayersResponse.players().stream()
                .anyMatch(p -> p.id().equals(TestConfig.getAdminId())),
                "Admin should be present in the list");
        softAssert.assertAll();
    }

    @Test(description = "Positive: All players can be parsed correctly with all fields populated in correct boundaries")
    public void playersSchemaIntegrityInGlobalListTest() {
        log(logger, "Step: Get all players from the system");
        var allPlayersResponse = getAllPlayers();

        log(logger, "Step: Assert players list is not null and not empty");
        assertNotNull(allPlayersResponse.players(), "Players list should not be null");
        assertFalse(allPlayersResponse.players().isEmpty(), "Players list should not be empty");

        log(logger, "Step: Validate each player has correct schema and boundaries");
        var softAssert = new SoftAssert();
        int validatedCount = 0;

        for (PlayerItemDto player : allPlayersResponse.players()) {
            if (validatedCount++ >= MAX_PLAYERS_VALIDATION_LIMIT) {
                break;
            }
            var playerId = player.id();

            softAssert.assertNotNull(player.id(), String.format("Player %d: ID should not be null", playerId));
            softAssert.assertNotNull(player.age(), String.format("Player %d: age should not be null", playerId));
            softAssert.assertNotNull(player.gender(), String.format("Player %d: gender should not be null", playerId));
            softAssert.assertNotNull(player.screenName(), String.format("Player %d: screenName should not be null", playerId));

            softAssert.assertTrue(player.age() >= TestDataGenerator.MIN_AGE, String.format("Player %d: age %d should be >= %d", playerId, player.age(), TestDataGenerator.MIN_AGE));
            softAssert.assertTrue(player.age() <= TestDataGenerator.MAX_AGE, String.format("Player %d: age %d should be <= %d", playerId, player.age(), TestDataGenerator.MAX_AGE));

            Gender genderEnum = player.getGenderAsEnum();
            softAssert.assertNotNull(genderEnum, String.format("Player %d: gender %s should be parseable to MALE or FEMALE enum", playerId, player.gender()));
            softAssert.assertTrue(
                    genderEnum == Gender.MALE || genderEnum == Gender.FEMALE,
                    String.format("Player %d: gender %s should be MALE or FEMALE", playerId, genderEnum)
            );

            Role roleEnum = player.getRoleAsEnum();
            softAssert.assertTrue(
                    roleEnum == Role.SUPERVISOR || roleEnum == Role.ADMIN || roleEnum == Role.USER,
                    String.format("Player %d: role %s should be SUPERVISOR, ADMIN, or USER", playerId, roleEnum)
            );
        }

        softAssert.assertAll();
    }

    // endregion

    // region Negative Tests

    @Test(description = "Negative: Regular user cannot retrieve the list of all players")
    public void regularUserCannotGetAllPlayersTest() {
        log(logger, "Step: Create a regular user");
        var userDetails = TestDataGenerator.getRandomPlayerDetails(Role.USER);
        createPlayerAndRegister(TestConfig.getSupervisorLogin(), userDetails);

        log(logger, "Step: Attempt to retrieve all players as regular user");
        var response = restClient.getAllPlayers();

        log(logger, "Step: Assert access is forbidden");
        assertEquals(response.getStatusCode(), HttpURLConnection.HTTP_FORBIDDEN, "Regular user should not be able to retrieve all players");
    }

    // endregion
}
