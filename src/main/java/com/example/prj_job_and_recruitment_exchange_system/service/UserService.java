package com.example.prj_job_and_recruitment_exchange_system.service;
// Trong file UserService.java (Interface)


import com.example.prj_job_and_recruitment_exchange_system.model.entity.RoleEnum;
import com.example.prj_job_and_recruitment_exchange_system.model.entity.User;
import com.example.prj_job_and_recruitment_exchange_system.model.entity.UserLoginDTO;
import com.example.prj_job_and_recruitment_exchange_system.model.request.*;
import com.example.prj_job_and_recruitment_exchange_system.model.response.JWTResponse;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface UserService {
    User registerUser(UserDTO userDTO);

    // Đồng bộ lại tham số truyền vào ở Interface cho khớp với Impl
    Page<User> getAllUsers(String search, RoleEnum role, Pageable pageable);
    // THÊM HÀM NÀY:
    JWTResponse login(UserLoginDTO userLoginDTO);
    void changePassword(ChangePasswordRequest request);
    void processForgotPassword(ForgotPasswordRequest request);
    void resetPassword(ResetPasswordRequest request);
    User getUserById(Long id);

    /**
     * CREATE: Admin chủ động tạo tài khoản mới cho nhân viên/người dùng
     */
    User createUserByAdmin(UserAdminRequest request);

    /**
     * UPDATE: Admin sửa thông tin, đổi role hoặc khóa/mở khóa tài khoản
     */
    User updateUserByAdmin(Long id, UserAdminRequest request);

    /**
     * DELETE: Admin vô hiệu hóa tài khoản (Khóa/Xóa mềm)
     */
    void deleteUserByAdmin(Long id);
}