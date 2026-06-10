package com.example.prj_job_and_recruitment_exchange_system.model.response;


import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class JWTResponse {
    private String token;
    private String type = "Bearer";
    private String email;
    private String role; // Trả về role để Frontend tiện phân hướng màn hình hiển thị

    public JWTResponse(String token, String email, String role) {
        this.token = token;
        this.email = email;
        this.role = role;
    }
}