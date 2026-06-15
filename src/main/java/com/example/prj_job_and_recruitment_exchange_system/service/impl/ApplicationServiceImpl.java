package com.example.prj_job_and_recruitment_exchange_system.service.impl;

import com.example.prj_job_and_recruitment_exchange_system.model.entity.Application;
import com.example.prj_job_and_recruitment_exchange_system.model.request.UpdateStatusRequest;
import com.example.prj_job_and_recruitment_exchange_system.repository.ApplicationRepository;
import com.example.prj_job_and_recruitment_exchange_system.service.ApplicationService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class ApplicationServiceImpl implements ApplicationService {

    //  CHỈ GIỮ LẠI DUY NHẤT REPOSITORY
    private final ApplicationRepository applicationRepository;

    @Override
    @Transactional
    public void updateApplicationStatus(UpdateStatusRequest request) {
        // 1. Tìm hồ sơ ứng tuyển (Application) theo ID từ Database
        Application application = applicationRepository.findById(request.getApplicationId())
                .orElseThrow(() -> new RuntimeException("Không tìm thấy hồ sơ ứng tuyển này trên hệ thống!"));

        // 2. Cập nhật trạng thái mới từ Enum và lời nhắn phản hồi thực tế
        application.setStatus(request.getStatus());
        application.setFeedback(request.getFeedback());

        // 3. Lưu trực tiếp xuống Database bảng applications
        applicationRepository.save(application);
    }
}