package com.example.prj_job_and_recruitment_exchange_system.controller;

import com.example.prj_job_and_recruitment_exchange_system.model.request.UpdateStatusRequest;
import com.example.prj_job_and_recruitment_exchange_system.service.ApplicationService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/employer/applications")
@RequiredArgsConstructor
public class EmployerApplicationController {

    private final ApplicationService applicationService;

    @PutMapping("/update-status")
    public ResponseEntity<String> updateStatus(@RequestBody UpdateStatusRequest request) {
        applicationService.updateApplicationStatus(request);
        return ResponseEntity.ok("Cập nhật trạng thái hồ sơ ứng tuyển và gửi mail phản hồi thành công!");
    }
}