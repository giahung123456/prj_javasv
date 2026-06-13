package com.example.prj_job_and_recruitment_exchange_system.aspect;

import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Pointcut;
import org.springframework.stereotype.Component;

@Aspect
@Component
@Slf4j // Sử dụng Log của Lombok để ghi log chuyên nghiệp thay vì System.out.println
public class PerformanceLoggingAspect {

    /**
     * Định nghĩa Pointcut: Quét tất cả các class và các hàm nằm trong package service.impl
     */
    @Pointcut("execution(* com.example.prj_job_and_recruitment_exchange_system.service.impl..*.*(..))")
    public void serviceImplMethods() {}

    /**
     * Thiết lập Advice Around: Chạy trước và sau khi hàm thực tế được thực thi
     */
    @Around("serviceImplMethods()")
    public Object logExecutionTime(ProceedingJoinPoint joinPoint) throws Throwable {
        // 1. Lấy tên Class và tên Hàm đang chạy để ghi log cho rõ ràng
        String className = joinPoint.getTarget().getClass().getSimpleName();
        String methodName = joinPoint.getSignature().getName();

        // 2. Ghi lại thời gian bắt đầu chạy hàm
        long startTime = System.currentTimeMillis();

        // 3. Cho phép hàm thực tế của bạn chạy tiếp tục
        Object result;
        try {
            result = joinPoint.proceed();
        } catch (Throwable throwable) {
            // Nếu hàm bị lỗi (ném Exception), vẫn tính toán thời gian chạy đến lúc sập
            long elapsedTime = System.currentTimeMillis() - startTime;
            log.error("[PERF] Hàm {}.{}() CHẠY THẤT BẠI sau {} ms. Lỗi: {}",
                    className, methodName, elapsedTime, throwable.getMessage());
            throw throwable; // Ném lại lỗi cho Controller xử lý
        }

        // 4. Tính toán tổng thời gian thực hiện (Thời gian hiện tại - Thời gian bắt đầu)
        long elapsedTime = System.currentTimeMillis() - startTime;

        // 5. In kết quả ra màn hình Console với tiền tố [PERF] để dễ lọc log
        log.info("[PERF] Hàm {}.{}() đã hoàn thành thành công trong {} ms", className, methodName, elapsedTime);

        return result;
    }
}