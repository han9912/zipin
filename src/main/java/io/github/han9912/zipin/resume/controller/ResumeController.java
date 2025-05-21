package io.github.han9912.zipin.resume.controller;

import io.github.han9912.zipin.common.aspect.annotation.CheckRole;
import io.github.han9912.zipin.common.dto.Result;
import io.github.han9912.zipin.common.util.AuthUtil;
import io.github.han9912.zipin.resume.entity.Resume;
import io.github.han9912.zipin.resume.service.ResumeService;
import io.github.han9912.zipin.user.entity.Role;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

@RestController
@RequestMapping("/api/resumes")
public class ResumeController {
    @Autowired
    private ResumeService service;
    @Autowired
    AuthUtil authUtil;

    @CheckRole(Role.JOB_SEEKER)
    @PostMapping(value = "/upload", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public Result<Resume> upload(@RequestParam("file") MultipartFile file,
                                 @RequestHeader("Authorization") String auth) {
        return Result.ok(service.upload(file, authUtil.resolveUid(auth)));
    }

    @CheckRole(Role.JOB_SEEKER)
    @GetMapping("/me")
    public Result<List<Resume>> myResumes(@RequestHeader("Authorization") String auth) {
        return Result.ok(service.list(authUtil.resolveUid(auth)));
    }

    @CheckRole(Role.JOB_SEEKER)
    @DeleteMapping("/{id}")
    public Result<Void> delete(@PathVariable Long id, @RequestHeader("Authorization") String auth) {
        service.delete(id, authUtil.resolveUid(auth));
        return Result.ok(null);
    }
}
