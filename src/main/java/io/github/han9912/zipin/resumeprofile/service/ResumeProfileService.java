package io.github.han9912.zipin.resumeprofile.service;

import io.github.han9912.zipin.resumeprofile.dto.ResumeProfileRequest;
import io.github.han9912.zipin.resumeprofile.entity.ResumeProfile;

public interface ResumeProfileService {
    ResumeProfile save(Long userId, ResumeProfileRequest req);
    ResumeProfile getByUser(Long userId);
}
