package com.example.prj_job_and_recruitment_exchange_system.repository;

import com.example.prj_job_and_recruitment_exchange_system.model.entity.CvOnline;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface CvOnlineRepository extends JpaRepository<CvOnline, Long> {
}
