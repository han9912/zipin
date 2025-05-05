package io.github.han9912.zipin.application.controller;

import io.github.han9912.zipin.application.dto.ApplicationRequest;
import io.github.han9912.zipin.application.entity.Application;
import io.github.han9912.zipin.application.entity.ApplicationStatus;
import io.github.han9912.zipin.application.service.ApplicationService;
import io.github.han9912.zipin.common.dto.Result;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

// Controller
@RestController
@RequestMapping("/api/applications")
public class ApplicationController {
    @Autowired
    ApplicationService service;

    @PostMapping
    public Result<Application> apply(@RequestBody ApplicationRequest req,
                                     @RequestHeader("X-User-Id") Long uid) {
        return Result.ok(service.apply(uid, req));
    }

    @GetMapping("/me")
    public Result<List<Application>> my(@RequestHeader("X-User-Id") Long uid) {
        return Result.ok(service.getMyApplications(uid));
    }

    @GetMapping("/job/{jobId}")
    public Result<List<Application>> job(@PathVariable Long jobId) {
        return Result.ok(service.getByJob(jobId));
    }

    @PutMapping("/{id}")
    public Result<Application> update(@PathVariable Long id,
                                      @RequestParam ApplicationStatus status) {
        return Result.ok(service.updateStatus(id, status));
    }
}
