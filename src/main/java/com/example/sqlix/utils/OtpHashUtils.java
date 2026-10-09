package com.example.sqlix.utils;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.util.HexFormat;
import java.util.UUID;

@Component
public class OtpHashUtils {

    private final byte[] secret;

    public OtpHashUtils(@Value("${app.otp-secret}") String secret) {
        if (secret.length() < 32) {
            throw new IllegalArgumentException(
                    "OTP secret must contain at least 32 characters"
            );
        }
        this.secret = secret.getBytes(StandardCharsets.UTF_8);
    }

    public String hash(
            UUID userId,
            UUID tokenId,
            String purpose,
            String otp
    ) {
        try {
            Mac mac = Mac.getInstance("HmacSHA256");

            mac.init(new SecretKeySpec(
                    secret, "HmacSHA256"
            ));

            String data = userId + ":" + tokenId
                    + ":" + purpose + ":" + otp;

            byte[] result = mac.doFinal(
                    data.getBytes(StandardCharsets.UTF_8)
            );

            return HexFormat.of().formatHex(result);

        } catch (Exception e) {
            throw new IllegalStateException(
                    "Failed to hash OTP", e
            );
        }
    }

    public boolean matches(
            UUID userId,
            UUID tokenId,
            String purpose,
            String otp,
            String storedHash
    ) {
        byte[] expected = hash(
                userId, tokenId, purpose, otp
        ).getBytes(StandardCharsets.UTF_8);

        byte[] actual = storedHash.getBytes(
                StandardCharsets.UTF_8
        );

        return MessageDigest.isEqual(expected, actual);
    }
}
