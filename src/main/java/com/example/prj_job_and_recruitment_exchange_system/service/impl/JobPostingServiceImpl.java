package com.example.prj_job_and_recruitment_exchange_system.service.impl;

import com.example.prj_job_and_recruitment_exchange_system.model.entity.*;
import com.example.prj_job_and_recruitment_exchange_system.model.request.ApplicationDTO;
import com.example.prj_job_and_recruitment_exchange_system.model.request.CvOnlineDTO;
import com.example.prj_job_and_recruitment_exchange_system.model.request.JobPostingDTO;
import com.example.prj_job_and_recruitment_exchange_system.repository.ApplicationRepository;
import com.example.prj_job_and_recruitment_exchange_system.repository.CvOnlineRepository;
import com.example.prj_job_and_recruitment_exchange_system.repository.JobPostingRepository;
import com.example.prj_job_and_recruitment_exchange_system.repository.UserRepository;
import com.example.prj_job_and_recruitment_exchange_system.service.JobPostingService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;

@Service
@RequiredArgsConstructor
public class JobPostingServiceImpl implements JobPostingService {
    private final JobPostingRepository jobPostingRepository;
    private final UserRepository userRepository;
    private final ApplicationRepository applicationRepository;
    private final CvOnlineRepository cvOnlineRepository;

    @Override
    public JobPosting createJobPosting(JobPostingDTO dto, String currentEmployerEmail) {
        User employer = userRepository.findByEmail(currentEmployerEmail)
                .orElseThrow(() -> new UsernameNotFoundException("Không tìm thấy thông tin tài khoản tuyển dụng"));

        JobPosting job = JobPosting.builder()
                .title(dto.getTitle())
                .description(dto.getDescription())
                .salaryRange(dto.getSalary() != null ? dto.getSalary().toString() : "Thỏa thuận")
                .employer(employer)
                .status(JobStatusEnum.PENDING_APPROVAL)
                .build();

        return jobPostingRepository.save(job);
    }

    @Override
    public Page<JobPosting> getJobsForAdmin(String search, JobStatusEnum status, Pageable pageable) {
        // Nếu admin chọn lọc cụ thể một trạng thái nào đó
        if (status != null) {
            return jobPostingRepository.findByTitleContainingIgnoreCaseAndStatus(search, status, pageable);
        }
        return jobPostingRepository.findByTitleContainingIgnoreCase(search, pageable);
    }

    @Override
    public Page<JobPosting> getEmployerJobs(String currentEmployerEmail, JobStatusEnum status, Pageable pageable) {
        User employer = userRepository.findByEmail(currentEmployerEmail)
                .orElseThrow(() -> new UsernameNotFoundException("Không tìm thấy tài khoản nhà tuyển dụng"));

        if (status != null) {
            return jobPostingRepository.findByEmployerIdAndStatus(employer.getId(), status, pageable);
        }
        return jobPostingRepository.findByEmployerId(employer.getId(), pageable);
    }
    @Override
    public JobPosting approveJobPosting(Long jobId, JobStatusEnum status) {
        // 1. Tìm bài đăng theo ID
        JobPosting job = jobPostingRepository.findById(jobId)
                .orElseThrow(() -> new RuntimeException("Không tìm thấy bài đăng tuyển dụng với ID: " + jobId));

        // 2. Cập nhật trạng thái mới (APPROVED hoặc REJECTED)
        job.setStatus(status);

        // 3. Lưu lại vào DB
        return jobPostingRepository.save(job);
    }
    @Override
    public Page<JobPosting> searchJobsForCandidate(String search, Pageable pageable) {
        String searchKey = (search == null) ? "" : search.trim();
        // Ứng viên chỉ được phép tìm kiếm các bài viết đã có trạng thái APPROVED (Đã duyệt)
        return jobPostingRepository.findByTitleContainingIgnoreCaseAndStatus(searchKey, JobStatusEnum.APPROVED, pageable);
    }

    @Override
    public CvOnline createCvOnline(CvOnlineDTO dto, String currentCandidateEmail) {
        User candidate = userRepository.findByEmail(currentCandidateEmail)
                .orElseThrow(() -> new UsernameNotFoundException("Không tìm thấy thông tin tài khoản ứng viên"));

        CvOnline cv = CvOnline.builder()
                .cvName(dto.getCvName())
                .fullName(dto.getFullName())
                .phone(dto.getPhone())
                .email(dto.getEmail())
                .education(dto.getEducation())
                .experience(dto.getExperience())
                .skills(dto.getSkills())
                .createdAt(LocalDateTime.now())
                .candidate(candidate)
                .build();

        return cvOnlineRepository.save(cv);
    }
    @Override
    public Application applyJob(ApplicationDTO dto, String currentCandidateEmail) {
        // 1. Tìm thông tin ứng viên từ email trong JWT Token
        User candidate = userRepository.findByEmail(currentCandidateEmail)
                .orElseThrow(() -> new UsernameNotFoundException("Không tìm thấy thông tin tài khoản ứng viên"));

        // 2. Tìm bài đăng tuyển dụng ứng viên muốn nộp vào
        JobPosting job = jobPostingRepository.findById(dto.getJobPostingId())
                .orElseThrow(() -> new RuntimeException("Bài đăng tuyển dụng này không tồn tại hoặc đã bị xóa!"));

        // 3. Bảo mật: Chỉ cho phép nộp vào các bài tuyển dụng đã được Admin duyệt (APPROVED)
        if (job.getStatus() != JobStatusEnum.APPROVED) {
            throw new RuntimeException("Bài đăng tuyển dụng này hiện đang đóng hoặc chưa được duyệt, không thể nộp hồ sơ!");
        }

        // Trong JobPostingServiceImpl.java (Hàm applyJob)
// Sửa dto.getCvPdfId() thành dto.getCvOnlineId() cho khớp với DTO mới:
        CvOnline cv = cvOnlineRepository.findById(dto.getCvOnlineId())
                .orElseThrow(() -> new RuntimeException("Không tìm thấy thông tin hồ sơ CV tương ứng trên hệ thống!"));
        // 5. Bảo mật: Kiểm tra xem CV này có đúng là của Candidate đang đăng nhập sở hữu hay không
        if (!cv.getCandidate().getId().equals(candidate.getId())) {
            throw new RuntimeException("Bạn không có quyền sử dụng hồ sơ CV này để ứng tuyển!");
        }

        // 6. Kiểm tra xem Candidate này đã nộp đơn vào bài đăng này trước đó chưa (Tránh spam hồ sơ)
        if (applicationRepository.existsByCandidateIdAndJobPostingId(candidate.getId(), job.getId())) {
            throw new RuntimeException("Bạn đã nộp hồ sơ ứng tuyển vào công việc này rồi, vui lòng không nộp lại!");
        }

        // 7. Khởi tạo đối tượng Application và lưu xuống Cơ sở dữ liệu
        Application application = Application.builder()
                .coverLetter(dto.getCoverLetter())
                .cvOnline(cv) // Gán thực thể CV Online vào đơn ứng tuyển
                .appliedAt(LocalDateTime.now())
                .status(ApplicationStatusEnum.PENDING) // Trạng thái ban đầu luôn là Chờ duyệt
                .candidate(candidate)
                .jobPosting(job)
                .build();

        return applicationRepository.save(application);
    }
}