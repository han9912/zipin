package io.github.han9912.zipin.job.controller;

import io.github.han9912.zipin.common.dto.Result;
import io.github.han9912.zipin.job.dto.JobRequest;
import io.github.han9912.zipin.job.dto.JobResponse;
import io.github.han9912.zipin.job.service.JobService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/jobs")
public class JobController {
    @Autowired
    JobService jobService;

    @PostMapping
    public Result<JobResponse> postJob(@RequestBody JobRequest req, @RequestHeader("X-User-Id") Long uid) {
        return Result.ok(jobService.createJob(req, uid));
    }

    @GetMapping
    public Result<List<JobResponse>> search(@RequestParam String keyword) {
        return Result.ok(jobService.searchJobs(keyword));
    }

    @GetMapping("/{id}")
    public Result<JobResponse> get(@PathVariable Long id) {
        return Result.ok(jobService.getJob(id));
    }

    @PutMapping("/{id}")
    public Result<JobResponse> update(@PathVariable Long id, @RequestBody JobRequest req, @RequestHeader("X-User-Id") Long uid) {
        return Result.ok(jobService.updateJob(id, req, uid));
    }

    @DeleteMapping("/{id}")
    public Result<JobResponse> delete(@PathVariable Long id, @RequestHeader("X-User-Id") Long uid) {
        jobService.deleteJob(id, uid);
        return Result.ok(null);
    }
}
