package com.example.prj_job_and_recruitment_exchange_system.service;



import com.example.prj_job_and_recruitment_exchange_system.model.entity.JobPosting;
import com.example.prj_job_and_recruitment_exchange_system.model.entity.JobStatusEnum;
import com.example.prj_job_and_recruitment_exchange_system.model.request.JobPostingDTO;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface JobPostingService {
    JobPosting createJobPosting(JobPostingDTO dto, String currentEmployerEmail);

    // Hàm lấy danh sách cho Admin (Tìm kiếm tiêu đề, lọc theo trạng thái bài đăng)
    Page<JobPosting> getJobsForAdmin(String search, JobStatusEnum status, Pageable pageable);

    // Hàm lấy danh sách cho Employer (Xem tin của chính mình dựa vào Email nhà tuyển dụng)
    Page<JobPosting> getEmployerJobs(String currentEmployerEmail, JobStatusEnum status, Pageable pageable);

}