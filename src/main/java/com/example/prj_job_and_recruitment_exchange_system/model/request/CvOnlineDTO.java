package com.example.prj_job_and_recruitment_exchange_system.model.request;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class CvOnlineDTO {

    @NotBlank(message = "Tiêu đề CV không được để trống")
    private String cvName;

    @NotBlank(message = "Họ tên không được để trống")
    private String fullName;

    private String phone;

    private String email;

    private String education;

    private String experience;

    private String skills;
}