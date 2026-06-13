package com.example.prj_job_and_recruitment_exchange_system.controller;


import com.example.prj_job_and_recruitment_exchange_system.model.entity.JobPosting;
import com.example.prj_job_and_recruitment_exchange_system.model.entity.JobStatusEnum;
import com.example.prj_job_and_recruitment_exchange_system.model.entity.RoleEnum;
import com.example.prj_job_and_recruitment_exchange_system.model.entity.User;
import com.example.prj_job_and_recruitment_exchange_system.model.request.UserAdminRequest;
import com.example.prj_job_and_recruitment_exchange_system.model.response.ApiDataResonse;
import com.example.prj_job_and_recruitment_exchange_system.service.JobPostingService;
import com.example.prj_job_and_recruitment_exchange_system.service.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/Job_Re_Ex/admin")
@RequiredArgsConstructor
public class AdminController {

    private final UserService userService;
    private final JobPostingService jobPostingService;

    // 1. LẤY DANH SÁCH & TÌM KIẾM USER (EMPLOYER HOẶC CANDIDATE)
    // Ví dụ: /admin/users?role=EMPLOYER&search=congtyA
    @GetMapping("/users")
    public ResponseEntity<ApiDataResonse<Page<User>>> getBatchUsers(
            @RequestParam(required = false) RoleEnum role,
            @RequestParam(required = false) String search,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size
    ) {
        Pageable pageable = PageRequest.of(page, size, Sort.by("id").descending());
        Page<User> users = userService.getAllUsers(search, role, pageable);

        return ResponseEntity.ok(new ApiDataResonse<>(
                true,
                "Lấy danh sách người dùng thành công",
                users,
                null,
                HttpStatus.OK
        ));
    }

    // 2. XEM TOÀN BỘ TIN ĐĂNG TRÊN HỆ THỐNG (Để chuẩn bị duyệt)
    // Ví dụ: /admin/jobs?status=PENDING_APPROVAL
    @GetMapping("/jobs")
    public ResponseEntity<ApiDataResonse<Page<JobPosting>>> getJobsForAdmin(
            @RequestParam(required = false) String search,
            @RequestParam(required = false) JobStatusEnum status,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size
    ) {
        Pageable pageable = PageRequest.of(page, size, Sort.by("id").descending());
        Page<JobPosting> jobs = jobPostingService.getJobsForAdmin(search, status, pageable);

        return ResponseEntity.ok(new ApiDataResonse<>(
                true,
                "Lấy danh sách bài đăng cho Admin thành công",
                jobs,
                null,
                HttpStatus.OK
        ));
    }

    // 3. DUYỆT TIN ĐĂNG (Chuyển trạng thái sang APPROVED hoặc REJECTED)
    // Ví dụ gửi PUT: /admin/jobs/5/status?status=APPROVED
    @PutMapping("/jobs/{id}/status")
    public ResponseEntity<ApiDataResonse<JobPosting>> approveJob(
            @PathVariable Long id,
            @RequestParam JobStatusEnum status
    ) {
        JobPosting updatedJob = jobPostingService.approveJobPosting(id, status);
        String message = status == JobStatusEnum.APPROVED ? "Đã duyệt bài đăng thành công!" : "Đã từ chối bài đăng!";

        return ResponseEntity.ok(new ApiDataResonse<>(
                true,
                message,
                updatedJob,
                null,
                HttpStatus.OK
        ));
    }
    // 2. GET BY ID - Xem chi tiết 1 người dùng
    @GetMapping("/{id}")
    public ResponseEntity<User> getUserById(@PathVariable Long id) {
        return ResponseEntity.ok(userService.getUserById(id));
    }

    // 3. CREATE - Admin thêm mới tài khoản
    @PostMapping
    public ResponseEntity<User> createUser(@RequestBody UserAdminRequest request) {
        return ResponseEntity.ok(userService.createUserByAdmin(request));
    }

    // 4. UPDATE - Admin sửa thông tin hoặc Khóa/Mở khóa tài khoản
    @PutMapping("/{id}")
    public ResponseEntity<User> updateUser(@PathVariable Long id, @RequestBody UserAdminRequest request) {
        return ResponseEntity.ok(userService.updateUserByAdmin(id, request));
    }

    // 5. DELETE - Vô hiệu hóa tài khoản
    @DeleteMapping("/{id}")
    public ResponseEntity<String> deleteUser(@PathVariable Long id) {
        userService.deleteUserByAdmin(id);
        return ResponseEntity.ok("Đã vô hiệu hóa (xóa mềm) tài khoản người dùng thành công!");
    }
}