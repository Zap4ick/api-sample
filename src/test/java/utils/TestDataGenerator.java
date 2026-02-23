package utils;

import dto.PlayerDetailsDto;
import entities.Gender;
import entities.Role;

import java.util.Arrays;
import java.util.concurrent.ThreadLocalRandom;

public class TestDataGenerator {

    // Age boundaries
    public static final int MIN_AGE = 17;
    public static final int MAX_AGE = 59;

    // Password boundaries
    public static final int MIN_PASSWORD_LENGTH = 7;
    public static final int MAX_PASSWORD_LENGTH = 15;
    public static final int DEFAULT_PASSWORD_LENGTH = 10;

    // Password character set
    private static final String PASSWORD_CHARS = "abcdefghijklmnopqrstuvwxyzABCDEFGHIJKLMNOPQRSTUVWXYZ0123456789";

    public static String getRandomLogin() {
        return "user_" + System.currentTimeMillis();
    }

    public static String getRandomPassword() {
        return getRandomPassword(DEFAULT_PASSWORD_LENGTH);
    }

    public static String getRandomPassword(int length) {
        if (length <= 0) length = DEFAULT_PASSWORD_LENGTH;
        StringBuilder sb = new StringBuilder(length);
        ThreadLocalRandom rnd = ThreadLocalRandom.current();
        for (int i = 0; i < length; i++) {
            sb.append(PASSWORD_CHARS.charAt(rnd.nextInt(PASSWORD_CHARS.length())));
        }
        return sb.toString();
    }

    /**
     * Generate a random PlayerDetailsDto with the provided role.
     */
    public static PlayerDetailsDto getRandomPlayerDetails(Role role) {
        String login = getRandomLogin();
        String password = getRandomPassword();
        int age = ThreadLocalRandom.current().nextInt(MIN_AGE, MAX_AGE);
        String screenName = "screenName_" + System.currentTimeMillis();
        Gender gender = Arrays.stream(Gender.values()).findAny().orElseThrow(() -> new RuntimeException("No genders"));
        return new PlayerDetailsDto(age, gender, login, password, role, screenName);
    }

    /**
     * Generate a random PlayerDetailsDto with Role.USER by default.
     */
    public static PlayerDetailsDto getRandomPlayerDetails() {
        return getRandomPlayerDetails(Role.USER);
    }

}
