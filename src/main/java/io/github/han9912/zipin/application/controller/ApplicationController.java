package io.github.han9912.zipin.application.controller;

import io.github.han9912.zipin.application.dto.ApplicationRequest;
import io.github.han9912.zipin.application.entity.Application;
import io.github.han9912.zipin.application.entity.ApplicationStatus;
import io.github.han9912.zipin.application.service.ApplicationService;
import io.github.han9912.zipin.common.aspect.annotation.CheckRole;
import io.github.han9912.zipin.common.dto.Result;
import io.github.han9912.zipin.common.util.AuthUtil;
import io.github.han9912.zipin.user.entity.Role;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

// Controller
@RestController
@RequestMapping("/api/applications")
public class ApplicationController {
    @Autowired
    ApplicationService service;
    @Autowired
    AuthUtil authUtil;

    @CheckRole(Role.JOB_SEEKER)
    @PostMapping
    public Result<Application> apply(@RequestBody ApplicationRequest req,
                                     @RequestHeader("Authorization") String auth) {
        return Result.ok(service.apply(authUtil.resolveUid(auth), req));
    }

    @CheckRole(Role.JOB_SEEKER)
    @GetMapping("/me")
    public Result<List<Application>> my(@RequestHeader("Authorization") String auth) {
        Long uid = authUtil.resolveUid(auth);
        return Result.ok(service.getMyApplications(uid));
    }

    @GetMapping("/job/{jobId}")
    public Result<List<Application>> job(@PathVariable Long jobId) {
        return Result.ok(service.getByJob(jobId));
    }

    @CheckRole(Role.RECRUITER)
    @PutMapping("/{id}")
    public Result<Application> update(@PathVariable Long id,
                                      @RequestParam ApplicationStatus status) {
        return Result.ok(service.updateStatus(id, status));
    }
}
