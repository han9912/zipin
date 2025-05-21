package io.github.han9912.zipin.resume.service;

import io.github.han9912.zipin.common.util.MinioUtil;
import io.github.han9912.zipin.resume.entity.Resume;
import io.github.han9912.zipin.resume.mapper.ResumeMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;
import java.util.Optional;

@Service
public class ResumeServiceImpl implements ResumeService {
    @Autowired private ResumeMapper mapper;
    @Autowired private MinioUtil minioUtil;

    public Resume upload(MultipartFile file, Long userId) {
        String url = minioUtil.upload("resumes", file);
        Resume resume = new Resume();
        resume.setUserId(userId);
        resume.setFileUrl(url);
        resume.setOriginalName(file.getOriginalFilename());
        mapper.insert(resume);
        return resume;
    }

    public List<Resume> list(Long userId) {
        return mapper.findByUserId(userId);
    }

    public void delete(Long id, Long userId) {
        Resume resume = Optional.ofNullable(mapper.findById(id)).orElseThrow();
        if (!resume.getUserId().equals(userId)) throw new RuntimeException("无权删除");
        mapper.delete(id);
    }
}
