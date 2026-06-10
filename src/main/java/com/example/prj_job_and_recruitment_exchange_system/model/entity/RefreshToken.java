package com.example.prj_job_and_recruitment_exchange_system.model.entity;

import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.Id;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.Instant;

@Entity
@AllArgsConstructor
@NoArgsConstructor
@Data
@Builder
public class RefreshToken {
    @Id
    @GeneratedValue
    private Long id;
    private String token;
    private String username;
    private Instant expiryDate;
    private boolean revoked;
}
