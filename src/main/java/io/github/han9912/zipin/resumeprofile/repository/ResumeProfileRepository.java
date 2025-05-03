package io.github.han9912.zipin.resumeprofile.repository;

import io.github.han9912.zipin.resumeprofile.entity.ResumeProfile;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface ResumeProfileRepository extends JpaRepository<ResumeProfile, Long> {
    Optional<ResumeProfile> findByUserId(Long userId);
}
