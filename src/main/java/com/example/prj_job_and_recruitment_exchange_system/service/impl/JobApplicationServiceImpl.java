package com.example.prj_job_and_recruitment_exchange_system.service.impl;

import com.example.prj_job_and_recruitment_exchange_system.model.entity.ApplicationStatusEnum;

import com.example.prj_job_and_recruitment_exchange_system.model.request.UpdateStatusRequest;
// Thay bằng EmailService của bạn
import com.example.prj_job_and_recruitment_exchange_system.repository.JobApplicationRepository;
import com.example.prj_job_and_recruitment_exchange_system.service.JobApplicationService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class JobApplicationServiceImpl implements JobApplicationService {

    private final JobApplicationRepository jobApplicationRepository;
    private final EmailService emailService;

    @Override
    @Transactional
    public void updateApplicationStatus(UpdateStatusRequest request) {
        // 1. Tìm hồ sơ ứng tuyển theo ID
        JobApplication application = jobApplicationRepository.findById(request.getApplicationId())
                .orElseThrow(() -> new RuntimeException("Không tìm thấy hồ sơ ứng tuyển trên hệ thống!"));

        // 2. Cập nhật trạng thái từ Enum của bạn và lời nhắn phản hồi
        application.setStatus(request.getStatus());
        application.setFeedback(request.getFeedback()); // Đảm bảo Entity JobApplication của bạn đã có trường này
        jobApplicationRepository.save(application);

        // 3. Lấy thông tin để gửi Email (Điều chỉnh getter theo đúng thuộc tính Entity của bạn)
        String candidateEmail = application.getCandidate().getEmail();
        String jobTitle = application.getJobPosting().getTitle();
        String companyName = application.getJobPosting().getEmployer().getCompanyName();

        // 4. Kích hoạt gửi mail tự động
        sendResponseEmailToCandidate(candidateEmail, jobTitle, companyName, request.getStatus(), request.getFeedback());
    }

    private void sendResponseEmailToCandidate(String toEmail, String jobTitle, String company, ApplicationStatusEnum status, String feedback) {
        String subject = "";
        String content = "";

        switch (status) {
            case REVIEWING:
                subject = "[" + company + "] Hồ sơ ứng tuyển vị trí " + jobTitle + " đang được xem xét";
                content = "Chào bạn,\nNhà tuyển dụng từ " + company + " đang tiến hành đánh giá chi tiết hồ sơ của bạn.\nLời nhắn: " + feedback;
                break;

            case INTERVIEWING:
                subject = "[" + company + "] Mời tham dự phỏng vấn vị trí " + jobTitle;
                content = "Chào bạn,\nChúc mừng bạn đã vượt qua vòng lọc hồ sơ! Chúng tôi trân trọng mời bạn tham gia buổi phỏng vấn.\nThông tin chi tiết lịch hẹn:\n" + feedback;
                break;

            case ACCEPTED:
                subject = "[" + company + "] Thông báo trúng tuyển vị trí " + jobTitle;
                content = "Chào bạn,\nChúng tôi rất vui mừng thông báo rằng bạn đã được tiếp nhận vào làm việc. Chi tiết về Offer và thủ tục nhận việc:\n" + feedback;
                break;

            case REJECTED:
                subject = "[" + company + "] Thư phản hồi kết quả ứng tuyển - Vị trí " + jobTitle;
                content = "Chào bạn,\nCảm ơn bạn đã dành thời gian quan tâm đến cơ hội việc làm tại công ty chúng tôi. Rất tiếc hồ sơ của bạn chưa phù hợp với các tiêu chí tuyển dụng hiện tại.\nLý do phản hồi: " + feedback;
                break;

            default:
                return; // PENDING thì không cần gửi email phản hồi
        }

        // Gọi hàm gửi mail thực tế từ dự án của bạn
        emailService.sendSimpleEmail(toEmail, subject, content);
    }
}