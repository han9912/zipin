package io.github.han9912.zipin.job.service;

import io.github.han9912.zipin.job.dto.JobRequest;
import io.github.han9912.zipin.job.dto.JobResponse;

import java.util.List;

public interface JobService {
    JobResponse createJob(JobRequest req, Long recruiterId);
    List<JobResponse> searchJobs(String keyword);
    JobResponse getJob(Long id);
    JobResponse updateJob(Long id, JobRequest req, Long recruiterId);
    void deleteJob(Long id, Long recruiterId);
}
