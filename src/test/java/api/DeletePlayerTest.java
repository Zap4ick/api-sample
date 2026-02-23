package api;

import entities.Role;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;
import utils.TestConfig;
import utils.TestDataGenerator;

import static org.testng.Assert.assertEquals;

public class DeletePlayerTest extends BaseTest {

    private static final Logger logger = LoggerFactory.getLogger(DeletePlayerTest.class);

    // region Positive Tests

    @DataProvider(name = "deletersAndRolesToDelete")
    public Object[][] deletersAndRolesToDeleteDataProvider() {
        return new Object[][]{
                {Role.SUPERVISOR, Role.USER},
                {Role.ADMIN, Role.ADMIN}
        };
    }

    @Test(description = "Positive: Supervisor and Admin can delete players with different roles", dataProvider = "deletersAndRolesToDelete")
    public void supervisorAndAdminCanDeletePlayerTest(Role deleterRole, Role roleToDelete) {
        log(logger, String.format("Step: Create a player with role %s", roleToDelete));
        var deleterLogin = deleterRole == Role.SUPERVISOR ? TestConfig.getSupervisorLogin() : TestConfig.getAdminLogin();
        var playerDetails = TestDataGenerator.getRandomPlayerDetails(roleToDelete);
        var playerCreateResponse = createPlayerAndRegister(TestConfig.getSupervisorLogin(), playerDetails);

        log(logger, String.format("Step: Delete player as %s", deleterRole));
        var deleteResponse = restClient.deletePlayer(deleterLogin, playerCreateResponse.id());

        log(logger, "Step: Assert player deleted with status 204");
        assertEquals(deleteResponse.getStatusCode(), 204, "Delete should return 204 No Content");

        log(logger, "Step: Assert player no longer exists");
        var getResponse = restClient.getPlayer(playerCreateResponse.id());
        assertEquals(getResponse.getStatusCode(), 404, "Get should return not found after delete");

        playersToDelete.get().remove(playerCreateResponse.id());
    }

    @Test(description = "Positive: Admin user can delete itself")
    public void adminCanDeleteSelfTest() {
        log(logger, "Step: Create an admin user");
        var adminDetails = TestDataGenerator.getRandomPlayerDetails(Role.ADMIN);
        var adminCreated = createPlayerAndRegister(TestConfig.getSupervisorLogin(), adminDetails);

        log(logger, "Step: Admin deletes itself");
        var deleteResponse = restClient.deletePlayer(adminCreated.login(), adminCreated.id());

        log(logger, "Step: Assert admin deleted with status 204");
        assertEquals(deleteResponse.getStatusCode(), 204, "Delete should return 204 No Content");

        log(logger, "Step: Assert admin no longer exists");
        var getResponse = restClient.getPlayer(adminCreated.id());
        assertEquals(getResponse.getStatusCode(), 404, "Admin should not exist after self-delete");

        playersToDelete.get().remove(adminCreated.id());
    }

    // endregion

    // region Negative Tests

    @DataProvider(name = "protectedUserDeleteCases")
    public Object[][] protectedUserDeleteCasesDataProvider() {
        return new Object[][]{
                {Role.USER, Role.ADMIN},
                {Role.ADMIN, Role.ADMIN}
        };
    }

    @Test(description = "Negative: Regular user/admin cannot delete protected admin user", dataProvider = "protectedUserDeleteCases")
    public void cannotDeleteProtectedAdminTest(Role actorRole, Role protectedRole) {
        log(logger, String.format("Step: Create a %s actor user", actorRole));
        var actorDetails = TestDataGenerator.getRandomPlayerDetails(actorRole);
        var actorCreated = createPlayerAndRegister(TestConfig.getSupervisorLogin(), actorDetails);

        log(logger, String.format("Step: Create a %s user", protectedRole));
        var protectedDetails = TestDataGenerator.getRandomPlayerDetails(protectedRole);
        var protectedCreated = createPlayerAndRegister(TestConfig.getSupervisorLogin(), protectedDetails);

        log(logger, String.format("Step: Attempt to delete %s user as %s", protectedRole, actorRole));
        var deleteResponse = restClient.deletePlayer(actorCreated.login(), protectedCreated.id());

        log(logger, "Step: Assert deletion rejected with 403 Forbidden");
        assertEquals(deleteResponse.getStatusCode(), 403,
                String.format("Delete should be forbidden for %s on %s user", actorRole, protectedRole));

        log(logger, String.format("Step: Assert %s user still exists", protectedRole));
        var getResponse = restClient.getPlayer(protectedCreated.id());
        assertEquals(getResponse.getStatusCode(), 200,
                String.format("%s user should still exist after forbidden delete attempt", protectedRole));
    }

    @DataProvider(name = "protectedUserDeleteSupervisorCases")
    public Object[][] protectedUserDeleteSupervisorCasesDataProvider() {
        return new Object[][]{
                {Role.USER},
                {Role.ADMIN}
        };
    }

    @Test(description = "Negative: Regular user/admin cannot delete supervisor", dataProvider = "protectedUserDeleteSupervisorCases")
    public void cannotDeleteProtectedSupervisorTest(Role actorRole) {
        log(logger, String.format("Step: Create a %s actor user", actorRole));
        var actorDetails = TestDataGenerator.getRandomPlayerDetails(actorRole);
        var actorCreated = createPlayerAndRegister(TestConfig.getSupervisorLogin(), actorDetails);

        log(logger, "Step: Use supervisor id from config");
        Long supervisorId = TestConfig.getSupervisorId();

        log(logger, String.format("Step: Attempt to delete supervisor as %s", actorRole));
        var deleteResponse = restClient.deletePlayer(actorCreated.login(), supervisorId);

        log(logger, "Step: Assert deletion rejected with 403 Forbidden");
        assertEquals(deleteResponse.getStatusCode(), 403,
                String.format("Delete should be forbidden for %s on supervisor user", actorRole));

        log(logger, "Step: Assert supervisor still exists");
        var getResponse = restClient.getPlayer(supervisorId);
        assertEquals(getResponse.getStatusCode(), 200, "Supervisor should still exist after forbidden delete attempt");
    }

    @Test(description = "Negative: Delete a player that does not exist")
    public void deleteNonExistingPlayerTest() {
        log(logger, "Step: Attempt to delete non-existing player");
        var nonExistingId = 99999999L;
        var deleteResponse = restClient.deletePlayer(TestConfig.getSupervisorLogin(), nonExistingId);

        log(logger, "Step: Assert deletion rejected with 404 Not Found");
        assertEquals(deleteResponse.getStatusCode(), 404, "Deleting non-existing player should return not found");
    }

    @Test(description = "Negative: Send invalid data type for playerId")
    public void deleteWithInvalidPlayerIdTypeTest() {
        log(logger, "Step: Attempt to delete with invalid player ID type");
        var deleteResponse = restClient.deletePlayer(TestConfig.getSupervisorLogin(), "not-a-number");

        log(logger, "Step: Assert deletion rejected with 400 Bad Request");
        assertEquals(deleteResponse.getStatusCode(), 400, "Invalid playerId type should be rejected");
    }

    @Test(description = "Negative: Regular user cannot delete themselves")
    public void userCannotDeleteSelfTest() {
        log(logger, "Step: Create a user");
        var userDetails = TestDataGenerator.getRandomPlayerDetails(Role.USER);
        var userCreated = createPlayerAndRegister(TestConfig.getSupervisorLogin(), userDetails);

        log(logger, "Step: Attempt user self-deletion");
        var deleteResponse = restClient.deletePlayer(userCreated.login(), userCreated.id());

        log(logger, "Step: Assert self-deletion rejected with 403 Forbidden");
        assertEquals(deleteResponse.getStatusCode(), 403, String.format("Self-delete should be forbidden for role: %s", Role.USER));

        log(logger, "Step: Assert user still exists");
        var getResponse = restClient.getPlayer(userCreated.id());
        assertEquals(getResponse.getStatusCode(), 200, String.format("User should still exist after forbidden self-delete role: %s", Role.USER));
    }

    @Test(description = "Negative: Supervisor cannot delete themselves")
    public void supervisorCannotDeleteSelfTest() {
        log(logger, "Step: Attempt supervisor self-deletion");
        var supervisorId = TestConfig.getSupervisorId();
        var supervisorLogin = TestConfig.getSupervisorLogin();
        var deleteResponse = restClient.deletePlayer(supervisorLogin, supervisorId);

        log(logger, "Step: Assert self-deletion rejected with 403 Forbidden");
        assertEquals(deleteResponse.getStatusCode(), 403, String.format("Self-delete should be forbidden for role: %s", Role.SUPERVISOR));

        log(logger, "Step: Assert supervisor still exists");
        var getResponse = restClient.getPlayer(supervisorId);
        assertEquals(getResponse.getStatusCode(), 200, String.format("Supervisor should still exist after forbidden self-delete role: %s", Role.SUPERVISOR));
    }

    // endregion
}
