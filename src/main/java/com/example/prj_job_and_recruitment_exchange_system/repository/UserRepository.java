package com.example.prj_job_and_recruitment_exchange_system.repository;

import com.example.prj_job_and_recruitment_exchange_system.model.entity.RoleEnum;
import com.example.prj_job_and_recruitment_exchange_system.model.entity.User;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;
@Repository
public interface UserRepository extends JpaRepository<User, Long> {
    Optional<User> findByEmail(String email);
    // Tìm kiếm theo email và phân trang
    Page<User> findByEmailContainingIgnoreCase(String email, Pageable pageable);

    // Tìm kiếm kết hợp lọc theo Role
    Page<User> findByEmailContainingIgnoreCaseAndRole(String email, RoleEnum role, Pageable pageable);
    // THÊM DÒNG NÀY ĐỂ HẾT LỖI ĐỎ:
    boolean existsByEmail(String email);
    // 🔥 THÊM CÁC HÀM NÀY ĐỂ PHỤC VỤ TRANG QUẢN TRỊ ADMIN:
    Page<User> findByEmailContainingAndRole(String email, RoleEnum role, Pageable pageable);
    Page<User> findByRole(RoleEnum role, Pageable pageable);
    Page<User> findByEmailContaining(String email, Pageable pageable);
}
