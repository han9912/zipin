package io.github.han9912.zipin.application.service;

import io.github.han9912.zipin.application.dto.ApplicationRequest;
import io.github.han9912.zipin.application.entity.Application;
import io.github.han9912.zipin.application.entity.ApplicationStatus;

import java.util.List;

public interface ApplicationService {
    Application apply(Long userId, ApplicationRequest req);
    List<Application> getMyApplications(Long userId);
    List<Application> getByJob(Long jobId);
    Application updateStatus(Long id, ApplicationStatus status);
}
