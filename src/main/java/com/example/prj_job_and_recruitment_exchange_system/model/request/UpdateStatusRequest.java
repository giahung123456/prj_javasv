package com.example.prj_job_and_recruitment_exchange_system.model.request;

import com.example.prj_job_and_recruitment_exchange_system.model.entity.ApplicationStatusEnum;
import lombok.Data;

@Data
public class UpdateStatusRequest {
    private Long applicationId;          // ID của hồ sơ ứng tuyển
    private ApplicationStatusEnum status; // Trạng thái mới (REVIEWING, INTERVIEWING, v.v.)
    private String feedback;             // Lời nhắn, lịch hẹn hoặc lý do từ chối
}