package com.example.prj_job_and_recruitment_exchange_system.model.entity;

import jakarta.persistence.*;
import lombok.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "applications")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Application {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(columnDefinition = "TEXT")
    private String coverLetter;

    // THAY ĐỔI TẠI ĐÂY: Liên kết trực tiếp tới CV Online vừa tạo thông qua ID
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "cv_online_id", nullable = false)
    private CvOnline cvOnline;

    @Column(nullable = false)
    private LocalDateTime appliedAt;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private ApplicationStatusEnum status;

    // Candidate nộp hồ sơ
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "candidate_id", nullable = false)
    private User candidate;

    // Chứa hồ sơ (Thuộc về bài đăng tuyển dụng nào)
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "job_posting_id", nullable = false)
    private JobPosting jobPosting;
}