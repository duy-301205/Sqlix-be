package com.example.sqlix.utils;

import java.security.SecureRandom;

public class OtpUtils {

    private static final SecureRandom RANDOM = new SecureRandom();

    private OtpUtils() {
    }

    public static String generateOtp() {
        return String.format(
                "%06d",
                RANDOM.nextInt(1_000_000)
        );
    }
}
