package com.example.prj_job_and_recruitment_exchange_system.service.impl;

import com.example.prj_job_and_recruitment_exchange_system.model.entity.RefreshToken;
import com.example.prj_job_and_recruitment_exchange_system.model.request.RefreshTokenRequest;
import com.example.prj_job_and_recruitment_exchange_system.model.response.JWTResponse; // Khớp class của bạn
import com.example.prj_job_and_recruitment_exchange_system.repository.RefreshTokenRepository;
import com.example.prj_job_and_recruitment_exchange_system.security.jwt.JWTProvider;
import com.example.prj_job_and_recruitment_exchange_system.security.principal.CustomUserDetails;
import com.example.prj_job_and_recruitment_exchange_system.service.RefreshTokenService;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class RefreshTokenServiceImpl implements RefreshTokenService {

    @Value("${jwt-refresh-expire:604800000}")
    private Long refreshExpirationMs;

    private final RefreshTokenRepository refreshTokenRepository;
    private final JWTProvider jwtProvider;
    private final UserDetailsService userDetailsService;

    @Override
    public RefreshToken createRefreshToken(String username) {
        RefreshToken refreshToken = RefreshToken.builder()
                .username(username)
                .token(UUID.randomUUID().toString())
                .expiryDate(Instant.now().plusMillis(refreshExpirationMs))
                .revoked(false)
                .build();
        return refreshTokenRepository.save(refreshToken);
    }

    @Override
    @Transactional
    public JWTResponse refreshAccessToken(RefreshTokenRequest request) {
        String requestToken = request.getRefreshToken();

        // 1. Tìm Token trong DB
        RefreshToken tokenFromDb = refreshTokenRepository.findByToken(requestToken)
                .orElseThrow(() -> new RuntimeException("Refresh Token không tồn tại trên hệ thống!"));

        // 2. Bảo mật: Nếu token đã từng bị thu hồi -> Xóa sạch các phiên đăng nhập cũ của user này
        if (tokenFromDb.isRevoked()) {
            refreshTokenRepository.deleteByUsername(tokenFromDb.getUsername());
            throw new RuntimeException("CẢNH BÁO: Refresh Token này đã từng được sử dụng! Nghi vấn rò rỉ, yêu cầu đăng nhập lại.");
        }

        // 3. Kiểm tra hết hạn
        if (tokenFromDb.getExpiryDate().isBefore(Instant.now())) {
            refreshTokenRepository.delete(tokenFromDb);
            throw new RuntimeException("Refresh Token đã hết hạn tự nhiên. Vui lòng đăng nhập lại!");
        }

        // 4. Xoay vòng Token: Vô hiệu hóa token vừa dùng
        tokenFromDb.setRevoked(true);
        refreshTokenRepository.save(tokenFromDb);

        // 5. Lấy thông tin user để sinh Access Token mới
        String email = tokenFromDb.getUsername();
        CustomUserDetails userDetails = (CustomUserDetails) userDetailsService.loadUserByUsername(email);

        String newAccessToken = jwtProvider.generateToken(userDetails);

        // Trích xuất tên quyền hạn (Ví dụ: ROLE_CANDIDATE) để trả về cho Frontend tiện điều hướng
        String roleName = userDetails.getAuthorities().iterator().next().getAuthority();

        // 6. Trả về đúng Constructor tùy biến của bạn: JWTResponse(token, email, role)
        return new JWTResponse(newAccessToken, email, roleName);
    }
}