package com.example.prj_job_and_recruitment_exchange_system.model.request;

import com.example.prj_job_and_recruitment_exchange_system.model.entity.RoleEnum;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@NoArgsConstructor
@AllArgsConstructor
@Data
@Builder

public class UserDTO {
    @NotBlank(message = "Email không được để trống")
    @Email(message = "Email không đúng định dạng")
    private String email;

    @NotBlank(message = "Mật khẩu không được để trống")
    @Size(min = 6, message = "Mật khẩu phải có ít nhất 6 ký tự")
    private String password;
    @NotNull(message = "Vui lòng chọn vai trò tài khoản (CANDIDATE hoặc EMPLOYER)")
    private String role; // Hứng chuỗi role từ client gửi lên: "CANDIDATE" hoặc "EMPLOYER"
}
