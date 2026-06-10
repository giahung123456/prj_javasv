package com.example.prj_job_and_recruitment_exchange_system.controller;

import com.example.prj_job_and_recruitment_exchange_system.model.entity.JobPosting;
import com.example.prj_job_and_recruitment_exchange_system.model.entity.JobStatusEnum;
import com.example.prj_job_and_recruitment_exchange_system.model.response.ApiDataResonse;
import com.example.prj_job_and_recruitment_exchange_system.service.JobPostingService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/Job_Re_Ex/admin/jobs")
@RequiredArgsConstructor
public class AdminJobController {
    private final JobPostingService jobPostingService;

    // GET: Danh sách tin tuyển dụng kèm bộ lọc trạng thái (ví dụ: ?status=PENDING_APPROVAL)
    @GetMapping
    public ResponseEntity<ApiDataResonse<Page<JobPosting>>> getAdminJobs(
            @RequestParam(defaultValue = "") String search,
            @RequestParam(required = false) JobStatusEnum status,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(defaultValue = "id") String sortBy,
            @RequestParam(defaultValue = "desc") String direction
    ) {
        Sort sort = direction.equalsIgnoreCase("asc") ? Sort.by(sortBy).ascending() : Sort.by(sortBy).descending();
        Pageable pageable = PageRequest.of(page, size, sort);

        // ĐÃ SỬA: Gọi đúng hàm quản lý tin đăng của JobPostingService (Ví dụ: getJobsForAdmin hoặc đặt theo tên hàm trong Service Interface của bạn)
        // Trong AdminJobController.java, sửa lại dòng gọi Service:
        Page<JobPosting> jobs = jobPostingService.getJobsForAdmin(search, status, pageable);
        return ResponseEntity.ok(new ApiDataResonse<>(true, "Lấy danh sách tin tuyển dụng thành công", jobs, null, HttpStatus.OK));
    }
}