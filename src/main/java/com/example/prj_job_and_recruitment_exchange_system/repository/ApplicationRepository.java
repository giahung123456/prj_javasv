package com.example.prj_job_and_recruitment_exchange_system.repository;

import com.example.prj_job_and_recruitment_exchange_system.model.entity.Application;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;


@Repository
public interface ApplicationRepository extends JpaRepository<Application, Long> {
    // Kiểm tra xem Candidate này đã nộp đơn vào bài tuyển dụng này trước đó chưa (Tránh nộp trùng)
    boolean existsByCandidateIdAndJobPostingId(Long candidateId, Long jobPostingId);
}