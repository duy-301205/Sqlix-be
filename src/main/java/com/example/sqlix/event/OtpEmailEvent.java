package com.example.sqlix.event;

public record OtpEmailEvent(
        String email,
        String otp,
        String purpose) {
}
