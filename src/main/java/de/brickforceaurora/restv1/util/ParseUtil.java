package de.brickforceaurora.restv1.util;

import java.util.UUID;

public final class ParseUtil {

    private ParseUtil() {
        throw new UnsupportedOperationException();
    }

    public static UUID uuidFromString(String value) {
        if (value == null) {
            throw new NullPointerException("null");
        }
        if (value.contains("-")) {
            if (value.length() != 36) {
                throw new IllegalArgumentException("UUIDs that have the '-' seperator have to be 36 characters long.");
            }
            return UUID.fromString(value);
        }
        if (value.length() != 32) {
            throw new IllegalArgumentException("UUIDs that don't have the '-' seperator have to be 32 characters long.");
        }
        return UUID.fromString(value.substring(0, 8) + "-" + value.substring(8, 12) + "-" + value.substring(12, 16) + "-"
            + value.substring(16, 20) + "-" + value.substring(20, 32));
    }
}
