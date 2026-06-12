package com.example.prj_job_and_recruitment_exchange_system.controller;

import com.example.prj_job_and_recruitment_exchange_system.model.entity.Application;
import com.example.prj_job_and_recruitment_exchange_system.model.entity.CvOnline;
import com.example.prj_job_and_recruitment_exchange_system.model.entity.JobPosting;
import com.example.prj_job_and_recruitment_exchange_system.model.request.ApplicationDTO;
import com.example.prj_job_and_recruitment_exchange_system.model.request.CvOnlineDTO;
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
import org.springframework.web.multipart.MultipartFile;

@RestController
@RequestMapping("/api/v1/Job_Re_Ex/candidate")
@RequiredArgsConstructor
public class CandidateController {

    private final JobPostingService jobPostingService;

    // 1. API Tìm kiếm và hiển thị danh sách việc làm (Chỉ hiển thị bài đã APPROVED)
    // Ví dụ: GET /api/v1/Job_Re_Ex/candidate/jobs?search=Java
    @GetMapping("/jobs")
    public ResponseEntity<ApiDataResonse<Page<JobPosting>>> searchJobs(
            @RequestParam(required = false) String search,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size
    ) {
        Pageable pageable = PageRequest.of(page, size, Sort.by("id").descending());
        Page<JobPosting> jobs = jobPostingService.searchJobsForCandidate(search, pageable);

        return ResponseEntity.ok(new ApiDataResonse<>(
                true,
                "Lấy danh sách việc làm phù hợp thành công",
                jobs,
                null,
                HttpStatus.OK
        ));
    }
    // API TẠO HỒ SƠ CV ONLINE BẰNG JSON
// POST /api/v1/Job_Re_Ex/candidate/cv-online
    @PostMapping("/cv-online")
    public ResponseEntity<ApiDataResonse<CvOnline>> createCvOnline(
            @Valid @RequestBody CvOnlineDTO cvOnlineDTO,
            Authentication authentication
    ) {
        String currentEmail = authentication.getName();
        CvOnline savedCv = jobPostingService.createCvOnline(cvOnlineDTO, currentEmail);

        return ResponseEntity.ok(new ApiDataResonse<>(
                true,
                "Tạo hồ sơ CV trực tuyến bằng dữ liệu JSON thành công!",
                savedCv, // Trả về thông tin kèm theo ID để dùng cho việc nộp đơn
                null,
                HttpStatus.OK
        ));
    }
    // 3. API NỘP HỒ SƠ ỨNG TUYỂN (Dùng JSON thuần 100%)
// POST http://localhost:8080/api/v1/Job_Re_Ex/candidate/apply
    @PostMapping("/apply")
    public ResponseEntity<ApiDataResonse<Application>> applyJob(
            @Valid @RequestBody ApplicationDTO applicationDTO,
            Authentication authentication
    ) {
        String currentEmail = authentication.getName(); // Lấy email candidate từ Token
        Application submission = jobPostingService.applyJob(applicationDTO, currentEmail);

        return new ResponseEntity<>(new ApiDataResonse<>(
                true,
                "Nộp đơn ứng tuyển thành công! Hồ sơ trực tuyến của bạn đã được chuyển tới Nhà tuyển dụng.",
                submission,
                null,
                HttpStatus.CREATED
        ), HttpStatus.CREATED);
    }

}