package com.example.prj_job_and_recruitment_exchange_system.repository;

import com.example.prj_job_and_recruitment_exchange_system.model.entity.RoleEnum;
import com.example.prj_job_and_recruitment_exchange_system.model.entity.User;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface UserRepository extends JpaRepository<User, Long> {
    Optional<User> findByEmail(String email);
    // Tìm kiếm theo email và phân trang
    Page<User> findByEmailContainingIgnoreCase(String email, Pageable pageable);

    // Tìm kiếm kết hợp lọc theo Role
    Page<User> findByEmailContainingIgnoreCaseAndRole(String email, RoleEnum role, Pageable pageable);
}
