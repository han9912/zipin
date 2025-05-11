package io.github.han9912.zipin.job.controller;

import io.github.han9912.zipin.common.dto.Result;
import io.github.han9912.zipin.job.entity.Job;
import io.github.han9912.zipin.job.service.HotJobService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/hot-jobs")
public class HotJobController {
    @Autowired
    private HotJobService service;

    @GetMapping
    public Result<List<Job>> getHotJobs(@RequestParam int topN) {
        return Result.ok(service.getTopJobs(topN));
    }
}
