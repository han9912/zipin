package io.github.han9912.zipin.resumeprofile.controller;

import io.github.han9912.zipin.common.aspect.annotation.CheckRole;
import io.github.han9912.zipin.common.dto.Result;
import io.github.han9912.zipin.common.util.AuthUtil;
import io.github.han9912.zipin.resumeprofile.dto.ResumeProfileRequest;
import io.github.han9912.zipin.resumeprofile.entity.ResumeProfile;
import io.github.han9912.zipin.resumeprofile.service.ResumeProfileService;
import io.github.han9912.zipin.user.entity.Role;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/resume-profiles")
public class ResumeProfileController {
    @Autowired
    ResumeProfileService service;
    @Autowired
    AuthUtil authUtil;

    @CheckRole(Role.JOB_SEEKER)
    @PostMapping
    public Result<ResumeProfile> create(@RequestBody ResumeProfileRequest req,
                                        @RequestHeader("Authorization") String auth) {
        return Result.ok(service.save(authUtil.resolveUid(auth), req));
    }

    @CheckRole(Role.JOB_SEEKER)
    @GetMapping("/me")
    public Result<ResumeProfile> me(@RequestHeader("Authorization") String auth) {
        return Result.ok(service.getByUser(authUtil.resolveUid(auth)));
    }

    @CheckRole(Role.JOB_SEEKER)
    @PutMapping("/me")
    public Result<ResumeProfile> update(@RequestBody ResumeProfileRequest req,
                                        @RequestHeader("Authorization") String auth) {
        return Result.ok(service.save(authUtil.resolveUid(auth), req));
    }
}
