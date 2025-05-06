package io.github.han9912.zipin.job.controller;

import io.github.han9912.zipin.common.dto.Result;
import io.github.han9912.zipin.common.util.AuthUtil;
import io.github.han9912.zipin.job.dto.JobRequest;
import io.github.han9912.zipin.job.dto.JobResponse;
import io.github.han9912.zipin.job.service.JobService;
import io.github.han9912.zipin.user.service.AuthService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/jobs")
public class JobController {
    @Autowired
    JobService jobService;
    @Autowired
    AuthService authService;
    @Autowired
    AuthUtil authUtil;


    @PostMapping
    public Result<JobResponse> postJob(@RequestBody JobRequest req, @RequestHeader("Authorization") String auth) {
        return Result.ok(jobService.createJob(req, authUtil.resolveUid(auth)));
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
    public Result<JobResponse> update(@PathVariable Long id, @RequestBody JobRequest req, @RequestHeader("Authorization") String auth) {
        return Result.ok(jobService.updateJob(id, req, authUtil.resolveUid(auth)));
    }

    @DeleteMapping("/{id}")
    public Result<JobResponse> delete(@PathVariable Long id, @RequestHeader("Authorization") String auth) {
        jobService.deleteJob(id, authUtil.resolveUid(auth));
        return Result.ok(null);
    }
}
