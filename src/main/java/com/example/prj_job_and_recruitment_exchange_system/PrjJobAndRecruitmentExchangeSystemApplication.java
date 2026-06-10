package com.example.prj_job_and_recruitment_exchange_system;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;

@SpringBootApplication
public class PrjJobAndRecruitmentExchangeSystemApplication {

    public static void main(String[] args) {
        SpringApplication.run(PrjJobAndRecruitmentExchangeSystemApplication.class, args);
    }
    @Bean
    public PasswordEncoder passwordEncoder(){
        return new BCryptPasswordEncoder();
    }

}
