package de.brickforceaurora.restv1.util;

import java.security.SecureRandom;
import java.util.Random;
import java.util.UUID;
import java.util.function.Predicate;

public final class RandomUtil {

    private static final char[] CODE_CHARS = "abcdefghijklmnopqrstuvwxyzABCDEFGHIJKLMNOPQRSTUVWXYZ0123456789-#+/".toCharArray();

    private RandomUtil() {
        throw new UnsupportedOperationException();
    }

    public static String generateCode(SecureRandom seedGen, Predicate<String> codeExists, UUID id, int length) {
        long seed;
        Random random = new Random();
        String code;
        while (true) {
            seed = seedGen.nextLong();
            seed ^= id.getLeastSignificantBits() << 16;
            seed ^= id.getMostSignificantBits() >> 16;
            random.setSeed(seed);
            StringBuilder builder = new StringBuilder();
            for (int i = 0; i < length; i++) {
                builder.append(CODE_CHARS[random.nextInt(CODE_CHARS.length)]);
            }
            code = builder.toString();
            if (!codeExists.test(code)) {
                break;
            }
        }
        return code;
    }

}
