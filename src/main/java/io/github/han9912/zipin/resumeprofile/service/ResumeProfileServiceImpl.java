package io.github.han9912.zipin.resumeprofile.service;

import io.github.han9912.zipin.resumeprofile.dto.ResumeProfileRequest;
import io.github.han9912.zipin.resumeprofile.entity.ResumeProfile;
import io.github.han9912.zipin.resumeprofile.repository.ResumeProfileRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@SuppressWarnings("unused")
@Service
public class ResumeProfileServiceImpl implements ResumeProfileService {
    @Autowired
    ResumeProfileRepository repo;

    public ResumeProfile save(Long userId, ResumeProfileRequest req) {
        ResumeProfile profile = repo.findByUserId(userId).orElse(new ResumeProfile());
        profile.setUserId(userId);
        profile.setTitle(req.title);
        profile.setEducation(req.education);
        profile.setExperience(req.experience);
        profile.setSkills(req.skills);
        return repo.save(profile);
    }

    public ResumeProfile getByUser(Long userId) {
        return repo.findByUserId(userId).orElse(null);
    }
}
