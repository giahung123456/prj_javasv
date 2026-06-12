package com.example.prj_job_and_recruitment_exchange_system.model.entity;

import jakarta.persistence.*;
import lombok.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "candidate_cvs")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CvOnline {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String cvName; // Tiêu đề CV (Ví dụ: CV Thực tập sinh Java)

    @Column(columnDefinition = "TEXT")
    private String fullName; // Họ và tên ứng viên

    private String phone;

    private String email;

    @Column(columnDefinition = "TEXT")
    private String education; // Thông tin học vấn (Trường đại học, chuyên ngành...)

    @Column(columnDefinition = "TEXT")
    private String experience; // Kinh nghiệm làm việc (Các dự án đã làm...)

    @Column(columnDefinition = "TEXT")
    private String skills; // Kỹ năng (Java, Spring Boot, MySQL...)

    private LocalDateTime createdAt;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "candidate_id", nullable = false)
    private User candidate;
}