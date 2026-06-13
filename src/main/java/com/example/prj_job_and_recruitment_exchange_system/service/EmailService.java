package com.example.prj_job_and_recruitment_exchange_system.service;

public interface EmailService {
    void sendSimpleEmail(String toEmail, String subject, String content);
}