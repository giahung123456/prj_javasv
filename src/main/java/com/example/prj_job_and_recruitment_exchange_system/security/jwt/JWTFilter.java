package com.example.prj_job_and_recruitment_exchange_system.security.jwt;

import com.example.prj_job_and_recruitment_exchange_system.repository.TokenBlacklistRepository;
import com.example.prj_job_and_recruitment_exchange_system.security.principal.CustomUserDetails;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;


import java.io.IOException;

@Component
@RequiredArgsConstructor
public class JWTFilter extends OncePerRequestFilter {
    private final JWTProvider jwtProvider;
    private final UserDetailsService userDetailsService;
    private final TokenBlacklistRepository tokenBlacklistRepository;
    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain) throws ServletException, IOException {
        String token = getTokenFromRequest(request);
        // THÊM ĐOẠN CHECK BLACKLIST NÀY:
        if (token != null && tokenBlacklistRepository.existsByTokenString(token)) {
            response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
            response.setContentType("application/json");
            response.setCharacterEncoding("UTF-8");
            response.getWriter().write("{\"error\": 401, \"message\":\"Tài khoản chưa được xác thực hoặc token đã đăng xuất\",\"path\":\"" + request.getServletPath() + "\"}");
            return; // Chặn đứng tại đây luôn
        }
        if(token!=null && jwtProvider.validateToken(token)){
            String username = jwtProvider.getUsernameFromToken(token);
            CustomUserDetails userDetails = (CustomUserDetails) userDetailsService.loadUserByUsername(username);

            Authentication authentication = new UsernamePasswordAuthenticationToken(userDetails,null, userDetails.getAuthorities());
            SecurityContextHolder.getContext().setAuthentication(authentication);
        }
        filterChain.doFilter(request, response);
    }

    private String getTokenFromRequest(HttpServletRequest request) {
        String authorization = request.getHeader("Authorization");
        if(authorization!=null && authorization.startsWith("Bearer ")){
            return authorization.substring(7).trim();  // "Bearer " -> 7 kí tự
        }
        return null;
    }
    // THÊM HÀM NÀY vào lớp JWTFilter.java để ép buộc bỏ qua kiểm tra token với API Auth
    @Override
    protected boolean shouldNotFilter(HttpServletRequest request) throws ServletException {
        String path = request.getServletPath();
        // Bỏ qua tất cả các request đi vào cụm API đăng ký / đăng nhập công khai
        return path.startsWith("/api/v1/Job_Re_Ex/auth");
    }
}
