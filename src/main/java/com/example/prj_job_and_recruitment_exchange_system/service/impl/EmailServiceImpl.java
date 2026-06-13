//package com.example.prj_job_and_recruitment_exchange_system.service.impl;
//
//import com.example.prj_job_and_recruitment_exchange_system.service.EmailService;
//import lombok.RequiredArgsConstructor;
//import org.springframework.mail.SimpleMailMessage;
//import org.springframework.mail.JavaMailSender;
//import org.springframework.stereotype.Service;
//
//@Service
//@RequiredArgsConstructor
//public class EmailServiceImpl implements EmailService {
//
//    private final JavaMailSender mailSender;
//
//    @Override
//    public void sendSimpleEmail(String toEmail, String subject, String content) {
//        SimpleMailMessage message = new SimpleMailMessage();
//        message.setFrom("your-email@gmail.com"); // Thay bằng Email gửi đi của bạn
//        message.setTo(toEmail);                  // Email nhận (Ứng viên)
//        message.setSubject(subject);             // Tiêu đề thư
//        message.setText(content);                // Nội dung phản hồi kết quả
//
//        mailSender.send(message);
//    }
//}