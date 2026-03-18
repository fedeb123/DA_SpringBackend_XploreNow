package com.XploreNowAPI.SpringAPI.application.service;

import com.XploreNowAPI.SpringAPI.domain.model.enumtype.OtpPurpose;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

@Slf4j
@Service
public class LoggingOtpDeliveryService implements OtpDeliveryService {

    @Override
    public void sendOtp(String email, String code, OtpPurpose purpose) {
        log.info("[OTP-DELIVERY] purpose={}, email={}, code={}", purpose, email, code);
    }
}
