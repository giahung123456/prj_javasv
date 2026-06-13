package com.example.prj_job_and_recruitment_exchange_system.model.request;

import com.example.prj_job_and_recruitment_exchange_system.model.entity.RoleEnum;
import jakarta.validation.constraints.NotNull;
import lombok.Data;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;

@Data
public class UserAdminRequest {
    @NotBlank(message = "Email không được để trống")
    @Email(message = "Email không đúng định dạng")
    private String email;

    private String password; // Nếu điền thì đổi/tạo mới, để trống thì giữ nguyên mật khẩu cũ

    @NotNull(message = "Vui lòng chọn vai trò (Role)")
    private RoleEnum role;

    @NotNull(message = "Vui lòng chọn trạng thái kích hoạt")
    private Boolean isActive; // Trạng thái tài khoản (true = hoạt động, false = bị khóa)
}