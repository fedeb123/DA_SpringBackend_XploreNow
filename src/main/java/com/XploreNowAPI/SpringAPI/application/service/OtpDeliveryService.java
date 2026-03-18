package com.XploreNowAPI.SpringAPI.application.service;

import com.XploreNowAPI.SpringAPI.domain.model.enumtype.OtpPurpose;

public interface OtpDeliveryService {

    void sendOtp(String email, String code, OtpPurpose purpose);
}
