package com.haru.api.user.service;

import java.security.SecureRandom;
import org.springframework.stereotype.Component;

@Component
public class RandomBuddyCodeGenerator implements BuddyCodeGenerator {

    private static final char[] CODE_CHARS = "ABCDEFGHJKLMNPQRSTUVWXYZ23456789".toCharArray();
    private static final int CODE_LENGTH = 8;

    private final SecureRandom secureRandom = new SecureRandom();

    @Override
    public String generate() {
        StringBuilder builder = new StringBuilder(CODE_LENGTH);
        for (int index = 0; index < CODE_LENGTH; index++) {
            builder.append(CODE_CHARS[secureRandom.nextInt(CODE_CHARS.length)]);
        }
        return builder.toString();
    }
}
