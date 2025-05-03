package io.github.han9912.zipin.resume.controller;

import io.github.han9912.zipin.common.dto.Result;
import io.github.han9912.zipin.resume.entity.Resume;
import io.github.han9912.zipin.resume.service.ResumeService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

@RestController
@RequestMapping("/api/resumes")
public class ResumeController {
    @Autowired
    private ResumeService service;

    @PostMapping("/upload")
    public Result<Resume> upload(@RequestParam("file") MultipartFile file,
                                 @RequestHeader("X-User-Id") Long uid) {
        return Result.ok(service.upload(file, uid));
    }

    @GetMapping("/me")
    public Result<List<Resume>> myResumes(@RequestHeader("X-User-Id") Long uid) {
        return Result.ok(service.list(uid));
    }

    @DeleteMapping("/{id}")
    public Result<Void> delete(@PathVariable Long id, @RequestHeader("X-User-Id") Long uid) {
        service.delete(id, uid);
        return Result.ok(null);
    }
}
