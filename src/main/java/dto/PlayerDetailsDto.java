package dto;


import entities.Gender;
import entities.Role;

/**
 * DTO for player creation. Will be used as query parameters in the create player endpoint.
 */
public record PlayerDetailsDto(
        Integer age,
        Gender gender,
        String login,
        String password,
        Role role,
        String screenName
) {
}


