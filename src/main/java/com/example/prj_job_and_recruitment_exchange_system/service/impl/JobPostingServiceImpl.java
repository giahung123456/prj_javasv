package com.example.prj_job_and_recruitment_exchange_system.service.impl;

import com.example.prj_job_and_recruitment_exchange_system.model.entity.JobPosting;
import com.example.prj_job_and_recruitment_exchange_system.model.entity.JobStatusEnum;
import com.example.prj_job_and_recruitment_exchange_system.model.entity.User;
import com.example.prj_job_and_recruitment_exchange_system.model.request.JobPostingDTO;
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
}