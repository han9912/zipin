package io.github.han9912.zipin.resume.service;

import io.github.han9912.zipin.common.util.MinioUtil;
import io.github.han9912.zipin.resume.entity.Resume;
import io.github.han9912.zipin.resume.repository.ResumeRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

@Service
public class ResumeServiceImpl implements ResumeService {
    @Autowired private ResumeRepository repo;
    @Autowired private MinioUtil minioUtil;

    public Resume upload(MultipartFile file, Long userId) {
        String url = minioUtil.upload("resumes", file);
        Resume resume = new Resume();
        resume.setUserId(userId);
        resume.setFileUrl(url);
        resume.setOriginalName(file.getOriginalFilename());
        return repo.save(resume);
    }

    public List<Resume> list(Long userId) {
        return repo.findByUserId(userId);
    }

    public void delete(Long id, Long userId) {
        Resume resume = repo.findById(id).orElseThrow();
        if (!resume.getUserId().equals(userId)) throw new RuntimeException("无权删除");
        repo.delete(resume);
    }
}
