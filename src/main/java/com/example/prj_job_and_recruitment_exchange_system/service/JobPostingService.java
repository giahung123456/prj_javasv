package com.example.prj_job_and_recruitment_exchange_system.service;



import com.example.prj_job_and_recruitment_exchange_system.model.entity.Application;
import com.example.prj_job_and_recruitment_exchange_system.model.entity.CvOnline;
import com.example.prj_job_and_recruitment_exchange_system.model.entity.JobPosting;
import com.example.prj_job_and_recruitment_exchange_system.model.entity.JobStatusEnum;
import com.example.prj_job_and_recruitment_exchange_system.model.request.ApplicationDTO;
import com.example.prj_job_and_recruitment_exchange_system.model.request.CvOnlineDTO;
import com.example.prj_job_and_recruitment_exchange_system.model.request.JobPostingDTO;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.web.multipart.MultipartFile;

public interface JobPostingService {
    JobPosting createJobPosting(JobPostingDTO dto, String currentEmployerEmail);

    // Hàm lấy danh sách cho Admin (Tìm kiếm tiêu đề, lọc theo trạng thái bài đăng)
    Page<JobPosting> getJobsForAdmin(String search, JobStatusEnum status, Pageable pageable);

    // Hàm lấy danh sách cho Employer (Xem tin của chính mình dựa vào Email nhà tuyển dụng)
    Page<JobPosting> getEmployerJobs(String currentEmployerEmail, JobStatusEnum status, Pageable pageable);
    JobPosting approveJobPosting(Long jobId, JobStatusEnum status);
    // Tìm kiếm bài đăng công khai cho Candidate
    Page<JobPosting> searchJobsForCandidate(String search, Pageable pageable);
    CvOnline createCvOnline(CvOnlineDTO dto, String currentCandidateEmail);
    // Thêm hàm này vào interface JobPostingService.java của bạn
    Application applyJob(ApplicationDTO dto, String currentCandidateEmail);
}