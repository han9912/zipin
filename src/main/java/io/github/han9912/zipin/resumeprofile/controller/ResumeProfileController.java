package io.github.han9912.zipin.resumeprofile.controller;

import io.github.han9912.zipin.common.dto.Result;
import io.github.han9912.zipin.resumeprofile.dto.ResumeProfileRequest;
import io.github.han9912.zipin.resumeprofile.entity.ResumeProfile;
import io.github.han9912.zipin.resumeprofile.service.ResumeProfileService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/resume-profiles")
public class ResumeProfileController {
    @Autowired
    ResumeProfileService service;

    @PostMapping
    public Result<ResumeProfile> create(@RequestBody ResumeProfileRequest req,
                                        @RequestHeader("X-User-Id") Long uid) {
        return Result.ok(service.save(uid, req));
    }

    @GetMapping("/me")
    public Result<ResumeProfile> me(@RequestHeader("X-User-Id") Long uid) {
        return Result.ok(service.getByUser(uid));
    }

    @PutMapping("/me")
    public Result<ResumeProfile> update(@RequestBody ResumeProfileRequest req,
                                        @RequestHeader("X-User-Id") Long uid) {
        return Result.ok(service.save(uid, req));
    }
}
