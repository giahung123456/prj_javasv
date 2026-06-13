package com.example.prj_job_and_recruitment_exchange_system.service;

import com.example.prj_job_and_recruitment_exchange_system.model.request.UpdateStatusRequest;

public interface JobApplicationService {
    void updateApplicationStatus(UpdateStatusRequest request);
}
