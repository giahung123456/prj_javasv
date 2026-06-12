package com.example.prj_job_and_recruitment_exchange_system.service.impl;

import com.example.prj_job_and_recruitment_exchange_system.model.entity.RoleEnum;
import com.example.prj_job_and_recruitment_exchange_system.model.entity.User;
import com.example.prj_job_and_recruitment_exchange_system.model.entity.UserLoginDTO;
import com.example.prj_job_and_recruitment_exchange_system.model.entity.UserOtp;
import com.example.prj_job_and_recruitment_exchange_system.model.request.ChangePasswordRequest;
import com.example.prj_job_and_recruitment_exchange_system.model.request.ForgotPasswordRequest;
import com.example.prj_job_and_recruitment_exchange_system.model.request.ResetPasswordRequest;
import com.example.prj_job_and_recruitment_exchange_system.model.request.UserDTO;
import com.example.prj_job_and_recruitment_exchange_system.model.response.JWTResponse;
import com.example.prj_job_and_recruitment_exchange_system.repository.UserOtpRepository;
import com.example.prj_job_and_recruitment_exchange_system.repository.UserRepository;
import com.example.prj_job_and_recruitment_exchange_system.security.jwt.JWTProvider;
import com.example.prj_job_and_recruitment_exchange_system.security.principal.CustomUserDetails;
import com.example.prj_job_and_recruitment_exchange_system.service.UserService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Random;

@Service
@RequiredArgsConstructor
@Slf4j
public class UserServiceImpl implements UserService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final UserOtpRepository userOtpRepository;
    private final AuthenticationManager authenticationManager; // Tiêm Bean để hỗ trợ kiểm tra tài khoản
    private final JWTProvider jwtProvider;
    @Override
    public User registerUser(UserDTO userDTO) {
        // 1. Kiểm tra trùng lặp email
        if (userRepository.findByEmail(userDTO.getEmail()).isPresent()) {
            log.error("Đăng ký thất bại: Email {} đã tồn tại", userDTO.getEmail());
            throw new RuntimeException("Email này đã được sử dụng bởi tài khoản khác!");
        }

        // 2. Chuẩn hóa chuỗi role truyền lên (viết hoa, xóa khoảng trắng)
        String inputRole = userDTO.getRole().toUpperCase().trim();

        // CHẶN TUYỆT ĐỐI: Không cho phép đăng ký quyền ADMIN
        if (inputRole.contains("ADMIN")) {
            log.warn("Cảnh báo: Có hành vi cố tình đăng ký tài khoản ADMIN với email: {}", userDTO.getEmail());
            throw new RuntimeException("Hệ thống không cho phép tự đăng ký tài khoản Quản trị viên (ADMIN)!");
        }

        // 3. Xác định RoleEnum hợp lệ (Chỉ chấp nhận CANDIDATE hoặc EMPLOYER)
        RoleEnum finalRole;
        if (inputRole.equals("EMPLOYER")) {
            finalRole = RoleEnum.EMPLOYER;
        } else if (inputRole.equals("CANDIDATE")) {
            finalRole = RoleEnum.CANDIDATE;
        } else {
            throw new RuntimeException("Vai trò đăng ký không hợp lệ! Chỉ chấp nhận CANDIDATE hoặc EMPLOYER.");
        }

        // 4. Map dữ liệu và gán Role đã qua kiểm duyệt
        User user = User.builder()
                .email(userDTO.getEmail())
                .passwordHash(passwordEncoder.encode(userDTO.getPassword()))
                .role(finalRole) // Gán role an toàn tại đây
                .isActive(true)  // Tự động kích hoạt tài khoản
                .build();

        log.info("Đăng ký tài khoản thành công! Email: {}, Vai trò: {}", user.getEmail(), finalRole);

        // 5. Lưu xuống cơ sở dữ liệu
        return userRepository.save(user);
    }


    @Override
    public Page<User> getAllUsers(String search, RoleEnum role, Pageable pageable) {
        String searchKey = (search == null) ? "" : search.trim();
        // Nếu Admin truyền vào Role cụ thể (EMPLOYER hoặc CANDIDATE) thì lọc theo Role đó
        if (role != null) {
            return userRepository.findByEmailContainingIgnoreCaseAndRole(searchKey, role, pageable);
        }
        // Nếu không truyền role thì tìm kiếm toàn bộ hệ thống
        return userRepository.findByEmailContainingIgnoreCase(searchKey, pageable);
    }

    @Override
    public JWTResponse login(UserLoginDTO userLoginDTO) {
        try {
            // 1. Thực hiện xác thực email và password thông qua AuthenticationManager
            // Spring Security sẽ tự kết nối sang CustomUserDetailsService để load user lên so sánh mật khẩu
            Authentication authentication = authenticationManager.authenticate(
                    new UsernamePasswordAuthenticationToken(userLoginDTO.getEmail(), userLoginDTO.getPassword())
            );

            // 2. Nếu không xảy ra lỗi, lấy ra đối tượng thông tin User chi tiết đã xác thực thành công
            CustomUserDetails userDetails = (CustomUserDetails) authentication.getPrincipal();

            // 3. Kiểm tra xem tài khoản có bị khóa (isActive = false) hay không
            if (!userDetails.isEnabled()) {
                throw new RuntimeException("Tài khoản của bạn đã bị khóa bởi Ban quản trị!");
            }

            // 4. Tiến hành sinh chuỗi Token
            String token = jwtProvider.generateToken(userDetails);

            // Lấy ra chuỗi Role thuần để trả về cho Client tiện xử lý giao diện
            String roleName = userDetails.getAuthorities().iterator().next().getAuthority();

            return new JWTResponse(token, userDetails.getEmail(), roleName);

        } catch (AuthenticationException e) {
            // Bắt các lỗi sai mật khẩu, sai email tài khoản
            throw new RuntimeException("Email hoặc mật khẩu không chính xác!");
        }
    }
    /**
     * LUỒNG 1: ĐỔI MẬT KHẨU (Authenticated - Yêu cầu Token)
     */
    @Override
    @Transactional
    public void changePassword(ChangePasswordRequest request) {
        // 1. Lấy email từ SecurityContextHolder (Do JWTFilter đã nạp vào sau khi xác thực thành công)
        String currentEmail = SecurityContextHolder.getContext().getAuthentication().getName();

        User user = userRepository.findByEmail(currentEmail)
                .orElseThrow(() -> new RuntimeException("Không tìm thấy thông tin tài khoản đang đăng nhập!"));

        // 2. Kiểm tra mật khẩu cũ (So khớp password thô và passwordHash trong DB)
        if (!passwordEncoder.matches(request.getOldPassword(), user.getPasswordHash())) {
            throw new RuntimeException("Mật khẩu hiện tại không chính xác!");
        }

        // 3. Kiểm tra mật khẩu mới trùng khớp xác nhận
        if (!request.getNewPassword().equals(request.getConfirmPassword())) {
            throw new RuntimeException("Mật khẩu mới và xác nhận mật khẩu không trùng khớp!");
        }

        // 4. Mã hóa mật khẩu mới và cập nhật vào trường passwordHash của bạn
        user.setPasswordHash(passwordEncoder.encode(request.getNewPassword()));
        userRepository.save(user);
    }

    /**
     * LUỒNG 2.1: YÊU CẦU QUÊN MẬT KHẨU (Public - Không cần Token)
     */
    @Override
    @Transactional
    public void processForgotPassword(ForgotPasswordRequest request) {
        // 1. Kiểm tra email có tồn tại trong hệ thống không
        if (!userRepository.existsByEmail(request.getEmail())) {
            throw new RuntimeException("Email này không tồn tại trên hệ thống RikkeiMall!");
        }

        // 2. Dọn dẹp mã OTP cũ của email này (nếu có) trước khi tạo mã mới
        userOtpRepository.deleteByEmail(request.getEmail());

        // 3. Sinh mã OTP ngẫu nhiên 6 chữ số
        String otp = String.format("%06d", new Random().nextInt(999999));

        // 4. Lưu mã OTP vào DB với thời gian hết hạn là 5 phút
        UserOtp userOtp = UserOtp.builder()
                .email(request.getEmail())
                .otpCode(otp)
                .expiryTime(LocalDateTime.now().plusMinutes(5))
                .build();
        userOtpRepository.save(userOtp);

        // 5. Log ra màn hình Console để lấy mã test (Hoặc nhúng JavaMailSender để gửi mail ở đây)
        log.info(" [FORGOT PASSWORD OTP] - Mã khôi phục của tài khoản {} là: {}", request.getEmail(), otp);
    }

    /**
     * LUỒNG 2.2: XÁC THỰC OTP VÀ ĐẶT LẠI MẬT KHẨU MỚI (Public - Không cần Token)
     */
    @Override
    @Transactional
    public void resetPassword(ResetPasswordRequest request) {
        // Do màn hình quên mật khẩu thường thực hiện theo chuỗi: Nhập email -> Nhận OTP -> Nhập OTP + Pass mới cùng lúc.
        // Ta cần tìm thực thể OTP dựa trên mã OTP gửi lên
        UserOtp userOtp = userOtpRepository.findAll().stream()
                .filter(otp -> otp.getOtpCode().equals(request.getOtpCode()))
                .findFirst()
                .orElseThrow(() -> new RuntimeException("Mã OTP không chính xác!"));

        // 1. Kiểm tra thời hạn OTP
        if (userOtp.getExpiryTime().isBefore(LocalDateTime.now())) {
            userOtpRepository.delete(userOtp);
            throw new RuntimeException("Mã OTP này đã hết hạn sử dụng! Vui lòng yêu cầu gửi lại mã mới.");
        }

        // 2. Tìm User sở hữu email gắn liền với mã OTP đó
        User user = userRepository.findByEmail(userOtp.getEmail())
                .orElseThrow(() -> new RuntimeException("Tài khoản liên kết với mã OTP này không còn tồn tại!"));

        // 3. Đổi mật khẩu mới
        user.setPasswordHash(passwordEncoder.encode(request.getNewPassword()));
        userRepository.save(user);

        // 4. Xóa OTP khỏi database sau khi đã đặt lại mật khẩu thành công
        userOtpRepository.delete(userOtp);
    }

}