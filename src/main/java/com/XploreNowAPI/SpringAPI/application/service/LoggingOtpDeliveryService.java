package com.XploreNowAPI.SpringAPI.application.service;

import com.XploreNowAPI.SpringAPI.domain.model.enumtype.OtpPurpose;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

@Slf4j
@Service
public class LoggingOtpDeliveryService implements OtpDeliveryService {

    private final JavaMailSender mailSender;

    @Value("${xplorenow.otp.expiration-minutes:10}")
    private long otpExpirationMinutes;

    @Value("${xplorenow.mail.otp.enabled:true}")
    private boolean otpMailEnabled;

    @Value("${spring.mail.username:}")
    private String smtpUsername;

    @Value("${spring.mail.password:}")
    private String smtpPassword;

    @Value("${xplorenow.mail.otp.from:}")
    private String otpMailFrom;

    public LoggingOtpDeliveryService(JavaMailSender mailSender) {
        this.mailSender = mailSender;
    }

    @Override
    public void sendOtp(String email, String code, OtpPurpose purpose) {
        log.info("[OTP-DELIVERY] purpose={}, email={}, code={}", purpose, email, code);

        if (!otpMailEnabled) {
            log.info("[OTP-DELIVERY] mail disabled by config for email={}", email);
            return;
        }

        if (!StringUtils.hasText(smtpUsername) || !StringUtils.hasText(smtpPassword)) {
            log.warn("[OTP-DELIVERY] SMTP credentials not configured. OTP only logged for email={}", email);
            return;
        }

        SimpleMailMessage message = new SimpleMailMessage();
        message.setTo(email);
        if (StringUtils.hasText(otpMailFrom)) {
            message.setFrom(otpMailFrom);
        }
        message.setSubject(buildSubject(purpose));
        message.setText(buildBody(code, purpose));

        try {
            mailSender.send(message);
            log.info("[OTP-DELIVERY] OTP email sent successfully to {}", email);
        } catch (Exception exception) {
            log.error("[OTP-DELIVERY] Failed to send OTP email to {}", email, exception);
        }
    }

    private String buildSubject(OtpPurpose purpose) {
        return switch (purpose) {
            case LOGIN -> "XploreNow - Codigo OTP de ingreso";
            case ACCESS_RECOVERY -> "XploreNow - Codigo OTP de recuperacion";
            case CHANGE_EMAIL -> "XploreNow - Codigo OTP para cambio de email";
            case CHANGE_PASSWORD -> "XploreNow - Codigo OTP para cambiar la contrasena";
        };
    }

    private String buildBody(String code, OtpPurpose purpose) {
        String context = switch (purpose) {
            case LOGIN -> "ingresar a tu cuenta";
            case ACCESS_RECOVERY -> "recuperar el acceso a tu cuenta";
            case CHANGE_EMAIL -> "confirmar el cambio de correo";
            case CHANGE_PASSWORD -> "cambiar la contrasena";
        };

        return "Tu codigo OTP de XploreNow es: " + code + "\n\n"
                + "Este codigo se usa para " + context + " y vence en "
                + otpExpirationMinutes + " minutos.\n\n"
                + "Si no solicitaste este codigo, ignora este mensaje.";
    }
}
