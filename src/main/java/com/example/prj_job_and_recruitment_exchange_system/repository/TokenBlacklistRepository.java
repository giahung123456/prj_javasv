package com.example.prj_job_and_recruitment_exchange_system.repository;

import com.example.prj_job_and_recruitment_exchange_system.model.entity.TokenBlacklist;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface TokenBlacklistRepository extends JpaRepository<TokenBlacklist, Long> {
    // Đã đổi thành existsByTokenString để khớp với thuộc tính private String tokenString;
    boolean existsByTokenString(String tokenString);
}