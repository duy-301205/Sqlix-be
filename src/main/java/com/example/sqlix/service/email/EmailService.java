package com.example.sqlix.service.email;

import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class EmailService {

    private final JavaMailSender mailSender;

    @Value("${spring.mail.username}")
    private String fromEmail;

    public void sendEmail(
            String to,
            String subject,
            String content
    ) {
        SimpleMailMessage message = new SimpleMailMessage();

        message.setFrom(fromEmail);
        message.setTo(to);
        message.setSubject(subject);
        message.setText(content);

        mailSender.send(message);
    }

    public void sendVerificationOtp(String toEmail, String otp) {
        String content = """
                Xin chào!
                
                Mã OTP xác minh tài khoản SQLix của bạn là:
                
                %s
                
                Mã có hiệu lực trong 5 phút.
                Không chia sẻ mã này với bất kỳ ai.
                
                SQLix Team
                """.formatted(otp);

        sendEmail(
                toEmail,
                "SQLix - Mã xác minh email",
                content
        );
    }

    public void sendPasswordResetOtp(
            String email,
            String otp
    ) {
        String content = """
                Xin chào!

                Bạn đã yêu cầu đặt lại mật khẩu SQLix.

                Mã OTP của bạn là:

                %s

                Mã có hiệu lực trong 5 phút.
                Nếu không yêu cầu, hãy bỏ qua email này.

                SQLix Team
                """.formatted(otp);

        sendEmail(
                email,
                "SQLix - Đặt lại mật khẩu",
                content
        );
    }
}
