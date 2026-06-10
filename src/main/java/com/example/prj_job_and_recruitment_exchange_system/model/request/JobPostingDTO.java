package com.example.prj_job_and_recruitment_exchange_system.model.request;


import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class JobPostingDTO {
    @NotBlank(message = "Tiêu đề tuyển dụng không được để trống")
    private String title;

    @NotBlank(message = "Mô tả công việc không được để trống")
    private String description;

    @NotBlank(message = "Yêu cầu công việc không được để trống")
    private String requirements;

    private BigDecimal salary;

    @NotBlank(message = "Địa điểm làm việc không được để trống")
    private String location;
}