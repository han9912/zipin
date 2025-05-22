package io.github.han9912.zipin.application.mapper;

import io.github.han9912.zipin.application.entity.Application;

import java.util.List;
import java.util.Optional;

public interface ApplicationMapper {
    int insert(Application application);
    Optional<Application> findById(Long id);
    List<Application> findByUserId(Long userId);
    List<Application> findByJobId(Long jobId);
}
