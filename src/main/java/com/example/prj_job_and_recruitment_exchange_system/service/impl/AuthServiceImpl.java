package com.example.prj_job_and_recruitment_exchange_system.service.impl;

import com.example.prj_job_and_recruitment_exchange_system.model.entity.TokenBlacklist;
import com.example.prj_job_and_recruitment_exchange_system.model.entity.User;
import com.example.prj_job_and_recruitment_exchange_system.repository.TokenBlacklistRepository;
import com.example.prj_job_and_recruitment_exchange_system.repository.UserRepository;
import com.example.prj_job_and_recruitment_exchange_system.security.jwt.JWTProvider;
import com.example.prj_job_and_recruitment_exchange_system.service.AuthService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;
import java.time.LocalDateTime;

@Service
@RequiredArgsConstructor
public class AuthServiceImpl implements AuthService {

    private final TokenBlacklistRepository tokenBlacklistRepository;
    private final UserRepository userRepository;
    private final JWTProvider jwtProvider;

    @Override
    public void logout(String authHeader) {
        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            throw new RuntimeException("Token không hợp lệ!");
        }

        String token = authHeader.substring(7).trim();

        if (tokenBlacklistRepository.existsByTokenString(token)) {
            throw new RuntimeException("Token này đã đăng xuất rồi!");
        }

        String email = jwtProvider.getUsernameFromToken(token);
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new UsernameNotFoundException("Không tìm thấy tài khoản người dùng"));

        TokenBlacklist blacklist = TokenBlacklist.builder()
                .tokenString(token)
                .revokedAt(LocalDateTime.now())
                .user(user)
                .build();

        tokenBlacklistRepository.save(blacklist);
    }
}