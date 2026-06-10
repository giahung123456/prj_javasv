package com.example.prj_job_and_recruitment_exchange_system.security.jwt;

import com.example.prj_job_and_recruitment_exchange_system.security.principal.CustomUserDetails;
import io.jsonwebtoken.*;
import io.jsonwebtoken.security.Keys;
import io.jsonwebtoken.security.SignatureException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.util.Date;

@Slf4j
@Component
public class JWTProvider {
    @Value("${jwt-secret}")
    private String jwtSecret;
    @Value("${jwt-expire}")
    private Long jwtExpired;

    // ĐÃ SỬA: Đổi tham số từ String username thành CustomUserDetails userDetails
    public String generateToken(CustomUserDetails userDetails){
        try{
            SecretKey key = Keys.hmacShaKeyFor(jwtSecret.getBytes(StandardCharsets.UTF_8));
            Date now = new Date();
            Date expire = new Date(now.getTime() + jwtExpired);

            // Trích xuất quyền hạn hiện tại (Ví dụ: ROLE_EMPLOYER hoặc ROLE_ADMIN)
            String authority = userDetails.getAuthorities().iterator().next().getAuthority();

            return Jwts.builder()
                    .subject(userDetails.getUsername()) // Lưu email làm Subject chính
                    .claim("role", authority)           // ĐÃ THÊM: Đóng gói quyền hạn vào Claim tùy biến để filter lấy ra dùng
                    .signWith(key)
                    .issuedAt(now)
                    .expiration(expire)
                    .compact();
        } catch (Exception e) {
            log.error("Lỗi trong quá trình sinh mã Token JWT: {}", e.getMessage());
            throw new RuntimeException(e);
        }
    }

    public boolean validateToken(String token){
        try{
            SecretKey key = Keys.hmacShaKeyFor(jwtSecret.getBytes(StandardCharsets.UTF_8));
            Jwts.parser().verifyWith(key).build().parseSignedClaims(token);
            return true;
        } catch (UnsupportedJwtException e) {
            log.info("Hệ thống không hỗ trợ jwt");
            throw new RuntimeException("Hệ thống không hỗ trợ jwt ",e);
        } catch (MalformedJwtException e) {
            log.info("Chuỗi jwt không đúng");
            throw new RuntimeException("Chuỗi jwt không đúng", e);
        } catch (ExpiredJwtException e){
            log.info("Chuỗi jwt hết hạn");
            throw new RuntimeException("Chuỗi jwt hết hạn ",e);
        }catch (SignatureException e){
            log.info("Sai chữ ký JWT");
            throw new RuntimeException("Sai chữ ký JWT ",e);
        }catch (IllegalArgumentException e){
            log.info("Chuỗi JWT rỗng");
            throw new RuntimeException("Chuỗi jwt rỗng ",e);
        }catch (JwtException e) {
            log.info("JWT không hợp lệ");
            throw new RuntimeException("JWT không hợp lệ", e);
        }
    }

    public String getUsernameFromToken(String token){
        try{
            SecretKey key = Keys.hmacShaKeyFor(jwtSecret.getBytes(StandardCharsets.UTF_8));
            return Jwts.parser().verifyWith(key).build().parseSignedClaims(token).getPayload().getSubject();
        } catch (Exception e) {
            throw new RuntimeException("Không lấy được username từ chuỗi token");
        }
    }
}