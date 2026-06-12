package com.example.prj_job_and_recruitment_exchange_system.controller;

import com.example.prj_job_and_recruitment_exchange_system.model.entity.User;
import com.example.prj_job_and_recruitment_exchange_system.model.entity.UserLoginDTO;
import com.example.prj_job_and_recruitment_exchange_system.model.request.UserDTO;
import com.example.prj_job_and_recruitment_exchange_system.model.response.ApiDataResonse;
import com.example.prj_job_and_recruitment_exchange_system.model.response.JWTResponse;
import com.example.prj_job_and_recruitment_exchange_system.service.AuthService;
import com.example.prj_job_and_recruitment_exchange_system.service.UserService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;




@RestController
@RequestMapping("/api/v1/Job_Re_Ex/auth")
@RequiredArgsConstructor
public class AuthController {
    private final UserService userService;
    private final AuthService authService;
@PostMapping("/register")
public ResponseEntity<ApiDataResonse<User>> register(@Valid @RequestBody UserDTO userDTO) {
    User registeredUser = userService.registerUser(userDTO);

    return new ResponseEntity<>(new ApiDataResonse<>(
            true,
            "Đăng ký tài khoản thành công!",
            registeredUser,
            null,
            HttpStatus.CREATED
    ), HttpStatus.CREATED);
}
    @PostMapping("/login")
    public ResponseEntity<ApiDataResonse<JWTResponse>> login(@Valid @RequestBody UserLoginDTO userLoginDTO) {
        JWTResponse jwtResponse = userService.login(userLoginDTO);

        return ResponseEntity.ok(new ApiDataResonse<>(
                true,
                "Đăng nhập hệ thống thành công!",
                jwtResponse,
                null,
                HttpStatus.OK
        ));
    }

    @PostMapping("/logout")
    public ResponseEntity<ApiDataResonse<Object>> logout(@RequestHeader("Authorization") String authHeader) {
        authService.logout(authHeader);
        return ResponseEntity.ok(new ApiDataResonse<>(
                true,
                "Đăng xuất thành công",
                null,
                null,
                HttpStatus.OK
        ));
    }

}
