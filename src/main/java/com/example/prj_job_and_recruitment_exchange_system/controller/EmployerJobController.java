package com.example.prj_job_and_recruitment_exchange_system.controller;

import com.example.prj_job_and_recruitment_exchange_system.model.entity.JobPosting;
import com.example.prj_job_and_recruitment_exchange_system.model.entity.JobStatusEnum;
import com.example.prj_job_and_recruitment_exchange_system.model.request.JobPostingDTO;

import com.example.prj_job_and_recruitment_exchange_system.model.response.ApiDataResonse;
import com.example.prj_job_and_recruitment_exchange_system.service.JobPostingService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/Job_Re_Ex/employer/jobs")
@RequiredArgsConstructor
public class EmployerJobController {
    private final JobPostingService jobPostingService;

    // POST: Nhà tuyển dụng đăng tin tuyển dụng mới
    @PostMapping
    public ResponseEntity<ApiDataResonse<JobPosting>> postJob(
            @Valid @RequestBody JobPostingDTO jobPostingDTO,
            Authentication authentication // Lấy thông tin user đăng nhập trực tiếp từ context bảo mật
    ) {
        String currentEmail = authentication.getName(); // Lấy email trong chuỗi JWT
        JobPosting createdJob = jobPostingService.createJobPosting(jobPostingDTO, currentEmail);

        return new ResponseEntity<>(new ApiDataResonse<>(
                true,
                "Đăng tin tuyển dụng thành công. Vui lòng chờ Ban quản trị phê duyệt!",
                createdJob,
                null,
                HttpStatus.CREATED
        ), HttpStatus.CREATED);
    }

    // GET: Nhà tuyển dụng tự xem lại lịch sử các tin đăng của mình (Phân trang + Lọc trạng thái)
    @GetMapping
    public ResponseEntity<ApiDataResonse<Page<JobPosting>>> getMyJobs(
            @RequestParam(required = false) JobStatusEnum status,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            Authentication authentication
    ) {
        String currentEmail = authentication.getName();
        Pageable pageable = PageRequest.of(page, size, Sort.by("createdAt").descending());
// Trong EmployerJobController.java, tìm đến hàm getMyJobs và sửa dòng gọi Service:
        Page<JobPosting> jobs = jobPostingService.getEmployerJobs(currentEmail, status, pageable);
        return ResponseEntity.ok(new ApiDataResonse<>(true, "Lấy danh sách tin đã đăng thành công", jobs, null, HttpStatus.OK));
    }
}