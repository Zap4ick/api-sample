package entities;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonValue;

public enum Gender {
    MALE("male"),
    FEMALE("female");

    private final String value;

    Gender(String value) {
        this.value = value;
    }

    @JsonValue
    public String getValue() {
        return value;
    }

    @JsonCreator
    public static Gender fromValue(String value) {
        if (value == null) return null;
        for (Gender g : Gender.values()) {
            if (g.value.equalsIgnoreCase(value)) {
                return g;
            }
        }

        throw new IllegalArgumentException(String.format("Unexpected gender value: '%s'", value));
    }
}
