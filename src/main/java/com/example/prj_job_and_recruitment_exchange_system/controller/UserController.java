package com.example.prj_job_and_recruitment_exchange_system.controller;

import com.example.prj_job_and_recruitment_exchange_system.model.request.ChangePasswordRequest;
import com.example.prj_job_and_recruitment_exchange_system.model.response.ApiDataResonse;
import com.example.prj_job_and_recruitment_exchange_system.service.UserService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/Job_Re_Ex/users")
@RequiredArgsConstructor
public class UserController {

    private final UserService userService;

    // POST /api/v1/Job_Re_Ex/users/change-password
    @PostMapping("/change-password")
    public ResponseEntity<ApiDataResonse<Object>> changePassword(@Valid @RequestBody ChangePasswordRequest request) {
        userService.changePassword(request);
        return ResponseEntity.ok(new ApiDataResonse<>(
                true, "Thay đổi mật khẩu tài khoản thành công!", null, null, HttpStatus.OK
        ));
    }
}