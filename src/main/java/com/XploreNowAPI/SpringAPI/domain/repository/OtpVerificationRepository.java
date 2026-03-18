package com.XploreNowAPI.SpringAPI.domain.repository;

import com.XploreNowAPI.SpringAPI.domain.model.entity.OtpVerification;
import com.XploreNowAPI.SpringAPI.domain.model.enumtype.OtpPurpose;
import com.XploreNowAPI.SpringAPI.domain.model.enumtype.OtpStatus;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface OtpVerificationRepository extends JpaRepository<OtpVerification, Long> {

    Optional<OtpVerification> findTopByEmailIgnoreCaseAndPurposeAndStatusOrderByCreatedAtDesc(
            String email,
            OtpPurpose purpose,
            OtpStatus status
    );

    List<OtpVerification> findByEmailIgnoreCaseAndPurposeAndStatus(
            String email,
            OtpPurpose purpose,
            OtpStatus status
    );
}
