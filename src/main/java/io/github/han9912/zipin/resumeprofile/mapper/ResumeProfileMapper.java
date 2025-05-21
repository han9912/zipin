package io.github.han9912.zipin.resumeprofile.mapper;

import io.github.han9912.zipin.resumeprofile.entity.ResumeProfile;

import java.util.Optional;

public interface ResumeProfileMapper {
    Optional<ResumeProfile> findByUserId(Long userId);
    int insert(ResumeProfile profile);
    void delete(Long userId);
}
