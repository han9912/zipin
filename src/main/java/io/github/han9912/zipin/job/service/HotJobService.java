package io.github.han9912.zipin.job.service;

import io.github.han9912.zipin.job.dto.JobResponse;
import io.github.han9912.zipin.job.entity.Job;
import io.github.han9912.zipin.job.repository.JobRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;

import java.util.Collections;
import java.util.List;
import java.util.Set;

@Service
public class HotJobService {
    private static final String HOT_JOB_KEY = "hot_jobs";
    @Autowired
    StringRedisTemplate redisTemplate;
    @Autowired
    JobRepository jobRepo;

    public void incrementHotScore(Long jobId, int score) {
        redisTemplate.opsForZSet().incrementScore(HOT_JOB_KEY, jobId.toString(), score);
    }

    public void incrementHotScore(List<JobResponse> responses, int score) {
        if(responses == null) return;
        responses.forEach(response -> {
            Long jobId = response.id;
            incrementHotScore(jobId, score);
        });
    }

    public List<Job> getTopJobs(int topN) {
        Set<String> jobIds = redisTemplate.opsForZSet().reverseRange(HOT_JOB_KEY, 0, topN - 1);
        if(jobIds == null || jobIds.isEmpty()) return Collections.emptyList();
        List<Long> ids = jobIds.stream().map(Long::valueOf).toList();
        return jobRepo.findAllById(ids);
    }
}
