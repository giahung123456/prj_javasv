package com.example.prj_job_and_recruitment_exchange_system.service.impl;

import com.example.prj_job_and_recruitment_exchange_system.model.entity.RoleEnum;
import com.example.prj_job_and_recruitment_exchange_system.model.entity.User;
import com.example.prj_job_and_recruitment_exchange_system.model.entity.UserLoginDTO;
import com.example.prj_job_and_recruitment_exchange_system.model.entity.UserOtp;
import com.example.prj_job_and_recruitment_exchange_system.model.request.*;
import com.example.prj_job_and_recruitment_exchange_system.model.response.JWTResponse;
import com.example.prj_job_and_recruitment_exchange_system.repository.UserOtpRepository;
import com.example.prj_job_and_recruitment_exchange_system.repository.UserRepository;
import com.example.prj_job_and_recruitment_exchange_system.security.jwt.JWTProvider;
import com.example.prj_job_and_recruitment_exchange_system.security.principal.CustomUserDetails;
import com.example.prj_job_and_recruitment_exchange_system.service.RefreshTokenService;
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
    private final RefreshTokenService refreshTokenService;

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
            Authentication authentication = authenticationManager.authenticate(
                    new UsernamePasswordAuthenticationToken(userLoginDTO.getEmail(), userLoginDTO.getPassword())
            );

            // 2. Nếu không xảy ra lỗi, lấy ra đối tượng thông tin User chi tiết đã xác thực thành công
            CustomUserDetails userDetails = (CustomUserDetails) authentication.getPrincipal();

            // 3. Kiểm tra xem tài khoản có bị khóa (isActive = false) hay không
            if (!userDetails.isEnabled()) {
                throw new RuntimeException("Tài khoản của bạn đã bị khóa bởi Ban quản trị!");
            }

            // 4. Tiến hành sinh chuỗi Access Token (Token ngắn hạn)
            String token = jwtProvider.generateToken(userDetails);

            // 🔥 5. ĐÃ BỔ SUNG: Sinh Refresh Token và lưu thẳng xuống Database bảng refresh_token
            // Lưu ý: Đảm bảo bạn đã khai báo private final RefreshTokenService refreshTokenService; ở đầu class UserServiceImpl
            var refreshTokenEntity = refreshTokenService.createRefreshToken(userDetails.getEmail());

            // 6. Lấy ra chuỗi Role thuần để trả về cho Client tiện xử lý giao diện
            String roleName = userDetails.getAuthorities().iterator().next().getAuthority();

            // 🔥 7. ĐÃ SỬA: Trả về Constructor 4 tham số chứa cả CẶP TOKEN giống như cấu trúc mới của JWTResponse
            // Thứ tự truyền vào: token (AccessToken), refreshToken, email, roleName
            return new JWTResponse(token, refreshTokenEntity.getToken(), userDetails.getEmail(), roleName);

        } catch (AuthenticationException e) {
            // Bắt các lỗi sai mật khẩu, sai email tài khoản
            throw new RuntimeException("Email hoặc mật khẩu không chính xác!");
        }
    }

    /**
     * LUỒNG 1: ĐỔI MẬT KHẨU (Authenticated - Yêu cầu Token)
     * ĐÃ SỬA: Ép kiểu và lấy Email sạch từ CustomUserDetails thay vì gọi .getName()
     */
    @Override
    @Transactional
    public void changePassword(ChangePasswordRequest request) {
        // 1. Lấy thông tin Principal bảo mật từ SecurityContextHolder
        Object principal = SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        String currentEmail;

        if (principal instanceof CustomUserDetails) {
            currentEmail = ((CustomUserDetails) principal).getEmail(); // Lấy trường Email thô chuẩn xác
        } else {
            currentEmail = principal.toString();
        }

        // 2. Truy vấn User từ Database theo email vừa bóc tách
        User user = userRepository.findByEmail(currentEmail)
                .orElseThrow(() -> new RuntimeException("Không tìm thấy thông tin tài khoản đang đăng nhập!"));

        // 3. Kiểm tra mật khẩu cũ gửi lên với mã hash trong DB
        if (!passwordEncoder.matches(request.getOldPassword(), user.getPasswordHash())) {
            throw new RuntimeException("Mật khẩu hiện tại không chính xác!");
        }

        // 4. Kiểm tra mật khẩu mới trùng khớp xác nhận
        if (!request.getNewPassword().equals(request.getConfirmPassword())) {
            throw new RuntimeException("Mật khẩu mới và xác nhận mật khẩu không trùng khớp!");
        }

        // 5. Mã hóa mật khẩu mới và cập nhật
        user.setPasswordHash(passwordEncoder.encode(request.getNewPassword()));
        userRepository.save(user);
        log.info("Tài khoản {} đã đổi mật khẩu thành công.", currentEmail);
    }

    /**
     * LUỒNG 2.1: YÊU CẦU QUÊN MẬT KHẨU (Public - Không cần Token)
     */
    @Override
    @Transactional
    public void processForgotPassword(ForgotPasswordRequest request) {
        // 1. Kiểm tra email có tồn tại không (Đã thêm hàm existsByEmail vào UserRepository trước đó)
        if (!userRepository.existsByEmail(request.getEmail())) {
            throw new RuntimeException("Email này không tồn tại trên hệ thống RikkeiMall!");
        }

        // 2. Dọn dẹp mã OTP cũ của email này tránh rác DB
        userOtpRepository.deleteByEmail(request.getEmail());

        // 3. Sinh mã OTP ngẫu nhiên 6 chữ số từ 000000 -> 999999
        String otp = String.format("%06d", new Random().nextInt(999999));

        // 4. Lưu mã OTP tạm thời có giá trị sử dụng trong vòng 5 phút
        UserOtp userOtp = UserOtp.builder()
                .email(request.getEmail())
                .otpCode(otp)
                .expiryTime(LocalDateTime.now().plusMinutes(5))
                .build();
        userOtpRepository.save(userOtp);

        log.info("[FORGOT PASSWORD OTP] - Mã khôi phục của tài khoản {} là: {}", request.getEmail(), otp);
    }

    /**
     * LUỒNG 2.2: XÁC THỰC OTP VÀ ĐẶT LẠI MẬT KHẨU MỚI (Public - Không cần Token)
     */
    @Override
    @Transactional
    public void resetPassword(ResetPasswordRequest request) {
        // Tìm thực thể OTP khớp chính xác mã code gửi lên từ Client
        UserOtp userOtp = userOtpRepository.findAll().stream()
                .filter(otp -> otp.getOtpCode().equals(request.getOtpCode()))
                .findFirst()
                .orElseThrow(() -> new RuntimeException("Mã OTP không chính xác!"));

        // 1. Kiểm tra thời hạn hiệu lực của mã
        if (userOtp.getExpiryTime().isBefore(LocalDateTime.now())) {
            userOtpRepository.delete(userOtp);
            throw new RuntimeException("Mã OTP này đã hết hạn sử dụng! Vui lòng yêu cầu gửi lại mã mới.");
        }

        // 2. Tìm User sở hữu email đi kèm với mã OTP đó
        User user = userRepository.findByEmail(userOtp.getEmail())
                .orElseThrow(() -> new RuntimeException("Tài khoản liên kết với mã OTP này không còn tồn tại!"));

        // 3. Đổi mật khẩu mới sau khi đã băm BCrypt
        user.setPasswordHash(passwordEncoder.encode(request.getNewPassword()));
        userRepository.save(user);

        // 4. Tiêu hủy mã OTP ngay sau khi thực hiện khôi phục thành công
        userOtpRepository.delete(userOtp);
        log.info("Tài khoản {} đã khôi phục mật khẩu thành công qua mã OTP.", user.getEmail());
    }
// Đảm bảo đã inject thêm PasswordEncoder ở đầu class:
// private final PasswordEncoder passwordEncoder;




    @Override
    public User getUserById(Long id) {
        return userRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Không tìm thấy người dùng với ID: " + id));
    }

    @Override
    @Transactional
    public User createUserByAdmin(UserAdminRequest request) {
        // Kiểm tra trùng lặp email trùng khớp với thuộc tính unique của DB
        if (userRepository.existsByEmail(request.getEmail())) {
            throw new RuntimeException("Email này đã tồn tại trên hệ thống!");
        }

        if (request.getPassword() == null || request.getPassword().trim().isEmpty()) {
            throw new RuntimeException("Mật khẩu tạo mới tài khoản không được để trống!");
        }

        // Tạo thực thể bằng Builder tương ứng với các thuộc tính trong file User.java của bạn
        User newUser = User.builder()
                .email(request.getEmail())
                .passwordHash(passwordEncoder.encode(request.getPassword())) // Khớp trường passwordHash
                .role(request.getRole())
                .isActive(request.getIsActive())
                .build();

        return userRepository.save(newUser);
    }

    @Override
    @Transactional
    public User updateUserByAdmin(Long id, UserAdminRequest request) {
        User existingUser = userRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Không tìm thấy tài khoản người dùng để cập nhật!"));

        // Kiểm tra nếu admin đổi email của user sang một email khác đã tồn tại
        if (!existingUser.getEmail().equals(request.getEmail()) && userRepository.existsByEmail(request.getEmail())) {
            throw new RuntimeException("Email mới muốn cập nhật đã được sử dụng bởi tài khoản khác!");
        }

        existingUser.setEmail(request.getEmail());
        existingUser.setRole(request.getRole());
        existingUser.setIsActive(request.getIsActive()); // Khớp trường isActive

        // Nếu admin nhập mật khẩu mới vào ô Text thì mới tiến hành băm mật khẩu gán lại
        if (request.getPassword() != null && !request.getPassword().trim().isEmpty()) {
            existingUser.setPasswordHash(passwordEncoder.encode(request.getPassword()));
        }

        return userRepository.save(existingUser);
    }

    @Override
    @Transactional
    public void deleteUserByAdmin(Long id) {
        User existingUser = userRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Không tìm thấy tài khoản người dùng cần xóa!"));

        // Vì User liên kết CascadeType.ALL dạng Composition với JobPosting và Application,
        // Thay vì xóa cứng gây lỗi hoặc mất dữ liệu lịch sử hệ thống, ta thực hiện khóa tài khoản (Xóa mềm)
        existingUser.setIsActive(false);
        userRepository.save(existingUser);
    }
}
