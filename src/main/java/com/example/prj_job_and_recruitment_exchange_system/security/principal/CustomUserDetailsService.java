package com.example.prj_job_and_recruitment_exchange_system.security.principal;

import com.example.prj_job_and_recruitment_exchange_system.model.entity.User;
import com.example.prj_job_and_recruitment_exchange_system.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

import java.util.Collections;
import java.util.List;

@Service
@RequiredArgsConstructor
public class CustomUserDetailsService implements UserDetailsService {

    private final UserRepository userRepository;

    @Override
    public UserDetails loadUserByUsername(String email) throws UsernameNotFoundException {
        // 1. Tìm user theo email, nếu không thấy thì ném ngoại lệ chuẩn của Spring Security
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new UsernameNotFoundException("Không tồn tại người dùng với email: " + email));

        // 2. Chuyển đổi Single RoleEnum thành cấu trúc Authority của Spring Security
        // Thêm tiền tố "ROLE_" là chuẩn cấu hình phân quyền của Spring Security (ví dụ: ROLE_ADMIN)
        List<GrantedAuthority> authorities = Collections.singletonList(
                new SimpleGrantedAuthority(user.getRole().name())
        );

        // 3. Build và trả về CustomUserDetails (Lưu ý gán đúng các trường đang có ở entity User)
        return CustomUserDetails.builder()
                .username(user.getEmail())          // Dùng email làm username đăng nhập
                .password(user.getPasswordHash())   // Khớp với trường passwordHash
                .email(user.getEmail())
                .enabled(user.getIsActive())        // Khớp với trường isActive
                .authorities(authorities)
                .build();
    }
}