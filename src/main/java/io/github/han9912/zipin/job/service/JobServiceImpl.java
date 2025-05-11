package io.github.han9912.zipin.job.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import io.github.han9912.zipin.common.service.NotificationService;
import io.github.han9912.zipin.common.util.CacheWithLockHelper;
import io.github.han9912.zipin.job.dto.JobRequest;
import io.github.han9912.zipin.job.dto.JobResponse;
import io.github.han9912.zipin.job.entity.Job;
import io.github.han9912.zipin.job.repository.JobRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.ValueOperations;
import org.springframework.stereotype.Service;

import java.util.Collections;
import java.util.List;
import java.util.concurrent.TimeUnit;

@Service
public class JobServiceImpl implements JobService{
    private static final Logger logger = LoggerFactory.getLogger(JobServiceImpl.class);
    @Autowired
    JobRepository repo;
    @Autowired
    RedisTemplate<String, String> redisTemplate;
    @Autowired
    CacheWithLockHelper cacheWithLockHelper;
    @Autowired
    NotificationService notificationService;
    @Autowired
    HotJobService hotJobService;
    private final ObjectMapper mapper = new ObjectMapper();

    public JobResponse createJob(JobRequest req, Long recruiterId) {
        Job job = Job.fromRequest(req, recruiterId);
        logger.info("新建职位：{} 创建人：{}", job.getTitle(), recruiterId);
        return toResponse(repo.save(job));
    }

    public void recordSearchKeyword(String keyword) {
        redisTemplate.opsForZSet().incrementScore("hot_keywords", keyword, 1);
    }

    public List<JobResponse> searchJobs(String keyword){
        try {
            recordSearchKeyword(keyword);
            String redisKey = "hot_jobs:" + keyword;
            ValueOperations<String, String> ops = redisTemplate.opsForValue();
            String cached = ops.get(redisKey);
            if (cached != null) {
                logger.info("Got from redis: {}", keyword);
                List<JobResponse> responses = Collections.singletonList(mapper.readValue(cached, JobResponse.class));
                hotJobService.incrementHotScore(responses, 1);
                return responses;
            }

            List<JobResponse> result = repo.search(keyword).stream().map(this::toResponse).toList();
            ops.set(redisKey, mapper.writeValueAsString(result), 10, TimeUnit.MINUTES);
            logger.info("Cached to redis: {} Entries: {}", keyword, result.size());
            hotJobService.incrementHotScore(result, 1);
            return result;
        } catch (Exception e) {
            logger.error("Search jobs error", e);
            return repo.search(keyword).stream().map(this::toResponse).toList();
        }
    }

    public JobResponse getJob(Long id){
        Job job = cacheWithLockHelper.getWithLogicalLock(
                "job:detail:" + id,
                () -> repo.findById(id).orElse(null),
                1800
        );
        return toResponse(job);
    }

    public JobResponse updateJob(Long id, JobRequest req, Long recruiterId){
        Job job = repo.findById(id).orElseThrow();
        if (!job.getRecruiterId().equals(recruiterId)) {
            throw new RuntimeException("没有权限修改此职位 / No permission to alter");
        }
        job.setTitle(req.title);
        job.setDescription(req.description);
        job.setLocation(req.location);
        return toResponse(repo.save(job));
    }

    public void deleteJob(Long id, Long recruiterId){
        Job job = repo.findById(id).orElseThrow();
        if (!job.getRecruiterId().equals(recruiterId)) {
            throw new RuntimeException("没有权限删除此职位 / No permission to delete");
        }
        repo.delete(job);
        logger.info("删除职位 {} by recruiter {}", id, recruiterId);
        notificationService.sendAsyncDeleteNotice(id, recruiterId);
    }

    public List<JobResponse> getTopJobs(int topN) {
        try {
            return hotJobService.getTopJobs(topN).stream().map(this::toResponse).toList();
        } catch (Exception e) {
            logger.error("Get top jobs error", e);
            return hotJobService.getTopJobs(topN).stream().map(this::toResponse).toList();
        }
    }

    private JobResponse toResponse(Job job) {
        JobResponse res = new JobResponse();
        res.id = job.getId();
        res.title = job.getTitle();
        res.description = job.getDescription();
        res.location = job.getLocation();
        res.status = job.getStatus().toString();
        res.createdAt = job.getCreatedAt().toString();
        return res;
    }
}
