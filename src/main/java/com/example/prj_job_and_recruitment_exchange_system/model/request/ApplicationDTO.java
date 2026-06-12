package com.example.prj_job_and_recruitment_exchange_system.model.request;

import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class ApplicationDTO {

    @NotNull(message = "ID bài đăng không được để trống")
    private Long jobPostingId;

    @NotNull(message = "ID của CV không được để trống")
    private Long cvOnlineId; // ĐÃ ĐỔI: cvPdfId -> cvOnlineId cho đúng bản chất CV Online dạng chữ

    private String coverLetter;
}