package com.example.prj_job_and_recruitment_exchange_system.repository;

import com.example.prj_job_and_recruitment_exchange_system.model.entity.UserOtp;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.Optional;

@Repository
public interface UserOtpRepository extends JpaRepository<UserOtp, Long> {
    Optional<UserOtp> findByEmailAndOtpCode(String email, String otpCode);
    void deleteByEmail(String email);
}