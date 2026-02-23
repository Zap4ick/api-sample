package dto;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import entities.Gender;
import entities.Role;
import org.slf4j.Logger;

/**
 * Player Item in getAll response
 */
@JsonInclude(JsonInclude.Include.NON_NULL)
public record PlayerItemDto(
    @JsonProperty("age")
    Integer age,

    @JsonProperty("gender")
    String gender,

    @JsonProperty("id")
    Long id,

    @JsonProperty("role")
    String role,

    @JsonProperty("screenName")
    String screenName
) {

    private static final Logger logger = org.slf4j.LoggerFactory.getLogger(PlayerItemDto.class);

    /**
     * Get gender as enum. Returns null if gender string is invalid.
     */
    public Gender getGenderAsEnum() {
        if (gender == null) return null;
        try {
            return Gender.valueOf(gender.toUpperCase());
        } catch (IllegalArgumentException e) {
            logger.warn("Invalid gender string: {}. {}", gender, e.getMessage());
            return null;
        }
    }

    /**
     * Get role as enum. Returns null if role string is invalid.
     */
    public Role getRoleAsEnum() {
        if (role == null) return null;
        try {
            return Role.valueOf(role.toUpperCase());
        } catch (IllegalArgumentException e) {
            logger.warn("Invalid role string: {}. {}", role, e.getMessage());
            return null;
        }
    }
}

