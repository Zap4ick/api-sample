package entities;

import com.fasterxml.jackson.annotation.JsonValue;

public enum Role {
    SUPERVISOR("supervisor"),
    ADMIN("admin"),
    USER("user");

    private final String value;

    Role(String value) {
        this.value = value;
    }

    @JsonValue
    public String getValue() {
        return value;
    }
}