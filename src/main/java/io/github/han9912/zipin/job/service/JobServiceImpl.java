package io.github.han9912.zipin.job.service;

import io.github.han9912.zipin.job.dto.JobRequest;
import io.github.han9912.zipin.job.dto.JobResponse;
import io.github.han9912.zipin.job.entity.Job;
import io.github.han9912.zipin.job.repository.JobRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class JobServiceImpl implements JobService{
    @Autowired
    JobRepository repo;

    public JobResponse createJob(JobRequest req, Long recruiterId) {
        Job job = Job.fromRequest(req, recruiterId);
        return toResponse(repo.save(job));
    }

    public List<JobResponse> searchJobs(String keyword){
        return repo.search(keyword).stream().map(this::toResponse).toList();
    }

    public JobResponse getJob(Long id){
        return toResponse(repo.findById(id).orElseThrow());
    }

    public JobResponse updateJob(Long id, JobRequest req, Long recruiterId){
        Job job = repo.findById(id).orElseThrow();
        if (!job.getRecruiterId().equals(recruiterId)) {
            throw new RuntimeException("没有权限修改此职位 / No permission to alter");
        }
        job.setTitle(req.title);
        job.setDescription(req.description);
        job.setLocation(req.location);
        return toResponse(repo.save(job));
    }

    public void deleteJob(Long id, Long recruiterId){
        Job job = repo.findById(id).orElseThrow();
        if (!job.getRecruiterId().equals(recruiterId)) {
            throw new RuntimeException("没有权限删除此职位 / No permission to delete");
        }
        repo.delete(job);
    }

    private JobResponse toResponse(Job job) {
        JobResponse res = new JobResponse();
        res.id = job.getId();
        res.title = job.getTitle();
        res.description = job.getDescription();
        res.location = job.getLocation();
        res.status = job.getStatus().toString();
        res.createdAt = job.getCreatedAt().toString();
        return res;
    }
}
