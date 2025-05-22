package io.github.han9912.zipin.job.entity;

import io.github.han9912.zipin.job.dto.JobRequest;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;

@Setter
@Getter
public class Job {
    private Long id;

    private Long recruiterId;

    private String title;

    private String description;

    private String location;

    private JobStatus status = JobStatus.OPEN;

    private LocalDateTime createdAt = LocalDateTime.now();

    public static Job fromRequest(JobRequest req, Long recruiterId) {
        Job job = new Job();
        job.setTitle(req.title);
        job.setDescription(req.description);
        job.setLocation(req.location);
        job.setRecruiterId(recruiterId);
        job.setStatus(JobStatus.OPEN);
        return job;
    }
}
