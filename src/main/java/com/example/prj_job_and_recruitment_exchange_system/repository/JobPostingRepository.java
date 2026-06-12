package com.example.prj_job_and_recruitment_exchange_system.repository;

import com.example.prj_job_and_recruitment_exchange_system.model.entity.JobPosting;
import com.example.prj_job_and_recruitment_exchange_system.model.entity.JobStatusEnum;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface JobPostingRepository extends JpaRepository<JobPosting, Long> {

    // ================= DÀNH CHO EMPLOYER =================

    // Lấy danh sách tin đăng của CHÍNH nhà tuyển dụng đó (phân trang)
    Page<JobPosting> findByEmployerId(Long employerId, Pageable pageable);

    // Lấy danh sách tin đăng của nhà tuyển dụng theo trạng thái
    Page<JobPosting> findByEmployerIdAndStatus(Long employerId, JobStatusEnum status, Pageable pageable);


    // ================= DÀNH CHO ADMIN (THÊM MỚI VÀO ĐÂY) =================

    // 1. Tìm kiếm theo tiêu đề bài đăng chứa từ khóa VÀ lọc theo trạng thái duyệt
    Page<JobPosting> findByTitleContainingIgnoreCaseAndStatus(String title, JobStatusEnum status, Pageable pageable);

    // 2. Tìm kiếm theo tiêu đề bài đăng chứa từ khóa (Không quan tâm trạng thái)
    Page<JobPosting> findByTitleContainingIgnoreCase(String title, Pageable pageable);

}