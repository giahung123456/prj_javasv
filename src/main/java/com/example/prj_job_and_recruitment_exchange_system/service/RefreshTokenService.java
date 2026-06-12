package com.example.prj_job_and_recruitment_exchange_system.service;

import com.example.prj_job_and_recruitment_exchange_system.model.entity.RefreshToken;
import com.example.prj_job_and_recruitment_exchange_system.model.request.RefreshTokenRequest;
import com.example.prj_job_and_recruitment_exchange_system.model.response.JWTResponse;
// DTO chứa AccessToken và RefreshToken mới

public interface RefreshTokenService {
    RefreshToken createRefreshToken(String username);
    JWTResponse refreshAccessToken(RefreshTokenRequest request);
}