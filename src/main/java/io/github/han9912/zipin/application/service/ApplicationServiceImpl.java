package io.github.han9912.zipin.application.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.github.han9912.zipin.application.dto.ApplicationRequest;
import io.github.han9912.zipin.application.entity.Application;
import io.github.han9912.zipin.application.entity.ApplicationStatus;
import io.github.han9912.zipin.application.repository.ApplicationRepository;
import io.github.han9912.zipin.common.service.NotificationService;
import io.github.han9912.zipin.job.service.HotJobService;
import io.github.han9912.zipin.resume.repository.ResumeRepository;
import io.github.han9912.zipin.resumeprofile.entity.ResumeProfile;
import io.github.han9912.zipin.resumeprofile.repository.ResumeProfileRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class ApplicationServiceImpl implements ApplicationService {
    @Autowired
    ApplicationRepository repo;
    @Autowired
    ResumeRepository resumeRepo;
    @Autowired
    ResumeProfileRepository profileRepo;
    @Autowired
    ObjectMapper mapper;
    @Autowired
    NotificationService notificationService;
    @Autowired
    HotJobService hotJobService;

    public Application apply(Long userId, ApplicationRequest req) {
        Application application = new Application();
        application.setUserId(userId);
        application.setJobId(req.jobId);
        application.setResumeId(req.resumeId);

        // 保存结构化快照（JSON）
        ResumeProfile profile = profileRepo.findByUserId(userId).orElse(null);
        if (profile != null) {
            try {
                application.setProfileSnapshot(mapper.writeValueAsString(profile));
            } catch (JsonProcessingException e) {
                throw new RuntimeException("简历快照保存失败", e);
            }
        }
        hotJobService.incrementHotScore(req.jobId, 3);
        notificationService.sendApplicationConfirmation(userId, req.jobId);
        return repo.save(application);
    }

    public List<Application> getMyApplications(Long userId) {
        return repo.findByUserId(userId);
    }

    public List<Application> getByJob(Long jobId) {
        return repo.findByJobId(jobId);
    }

    public Application updateStatus(Long id, ApplicationStatus status) {
        Application a = repo.findById(id).orElseThrow();
        a.setStatus(status);
        return repo.save(a);
    }
}