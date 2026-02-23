package dto;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import entities.Gender;
import entities.Role;

@JsonInclude(JsonInclude.Include.NON_NULL)
public record PlayerUpdateRequestDto(
    @JsonProperty("age")
    Integer age,

    @JsonProperty("gender")
    Gender gender,

    @JsonProperty("login")
    String login,

    @JsonProperty("password")
    String password,

    @JsonProperty("role")
    Role role,

    @JsonProperty("screenName")
    String screenName
) {
}
